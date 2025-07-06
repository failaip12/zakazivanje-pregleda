document.addEventListener("DOMContentLoaded", () => {
  const config = {
    api: {
      baseUrl: "/api",
      routes: {
        login: "/auth/login",
        register: "/auth/register",
        doctors: "/doctors",
        appointments: "/appointments",
      },
    },
    roles: {
      PATIENT: "ROLE_PATIENT",
      DOCTOR: "ROLE_DOCTOR",
      ADMIN: "ROLE_ADMIN",
      GUEST: "GUEST",
    },
    selectors: {
      views: {
        auth: "#authView",
        patient: "#patientView",
        doctor: "#doctorView",
        admin: "#adminView",
      },
      userInfo: "#userInfo",
      welcomeMessage: "#welcomeMessage",
      messageArea: "#messageArea",
    },
  };

  const App = {
    state: {
      token: null,
      user: null,
      selectedSlotTime: null,
    },

    elements: {
      views: {
        auth: document.querySelector(config.selectors.views.auth),
        patient: document.querySelector(config.selectors.views.patient),
        doctor: document.querySelector(config.selectors.views.doctor),
        admin: document.querySelector(config.selectors.views.admin),
        all: document.querySelectorAll(".view"),
      },
      userInfo: document.querySelector(config.selectors.userInfo),
      welcomeMessage: document.querySelector(config.selectors.welcomeMessage),
      messageArea: document.querySelector(config.selectors.messageArea),

      loginForm: document.getElementById("loginForm"),
      registerForm: document.getElementById("registerForm"),
      addDoctorForm: document.getElementById("addDoctorForm"),
      bookingForm: document.getElementById("bookingForm"),

      logoutButton: document.getElementById("logoutButton"),
      bookAppointmentBtn: document.getElementById("bookAppointmentBtn"),

      doctorSelect: document.getElementById("doctorSelect"),
      appointmentDate: document.getElementById("appointmentDate"),
      patientAppointmentFilter: document.getElementById("appointmentFilter"),
      doctorAppointmentFilter: document.getElementById(
        "doctorAppointmentFilter",
      ),

      patientAppointmentsContainer:
        document.getElementById("patientAppointments"),
      doctorAppointmentsContainer: document.getElementById("doctorAppointments"),
      timetableContainer: document.getElementById("timetableContainer"),
    },

    init() {
      this.bindEvents();
      this.checkAuthState();
    },

    bindEvents() {
      this.elements.loginForm.addEventListener("submit", (e) =>
        this.handleLogin(e),
      );
      this.elements.registerForm.addEventListener("submit", (e) =>
        this.handleRegister(e),
      );
      this.elements.logoutButton.addEventListener("click", () =>
        this.handleLogout(),
      );
      this.elements.addDoctorForm.addEventListener("submit", (e) =>
        this.handleAddDoctor(e),
      );
      this.elements.bookingForm.addEventListener("submit", (e) =>
        this.handleBookingSubmit(e),
      );

      this.elements.patientAppointmentFilter.addEventListener("change", () =>
        this.loadAppointments("patient"),
      );
      this.elements.doctorAppointmentFilter.addEventListener("change", () =>
        this.loadAppointments("doctor"),
      );
      this.elements.doctorSelect.addEventListener("change", () =>
        this.renderTimetable(),
      );
      this.elements.appointmentDate.addEventListener("change", () =>
        this.renderTimetable(),
      );
    },

    async apiFetch(endpoint, options = {}) {
      const headers = {
        "Content-Type": "application/json",
        ...options.headers,
      };

      if (this.state.token) {
        headers["Authorization"] = `Bearer ${this.state.token}`;
      }

      try {
        const response = await fetch(`${config.api.baseUrl}${endpoint}`, {
          ...options,
          headers,
        });

        if (!response.ok) {
          const errorData = await response
            .json()
            .catch(() => ({ message: `Error: ${response.statusText}` }));
          throw errorData;
        }

        if (response.status === 204 || response.status === 201) {
          return null;
        }
        return response.json();
      } catch (error) {
        console.error("API Fetch Error:", error);
        throw error;
      }
    },

    checkAuthState() {
      const token = localStorage.getItem("jwtToken");
      if (token) {
        try {
          const payload = this.parseJwt(token);
          if (payload.exp * 1000 < Date.now()) {
            throw new Error("Token expired");
          }
          this.state.token = token;
          this.state.user = payload;
          this.showView(payload.roles[0]);
          this.elements.userInfo.classList.remove("hidden");
          this.elements.welcomeMessage.textContent = `Prijavljeni ste kao: ${payload.sub}`;
        } catch (error) {
          console.warn(error.message);
          this.handleLogout();
        }
      } else {
        this.showView(config.roles.GUEST);
      }
    },

    async handleLogin(e) {
      e.preventDefault();
      this.clearErrors();
      const formData = new FormData(e.target);
      const data = Object.fromEntries(formData.entries());

      try {
        const result = await this.apiFetch(config.api.routes.login, {
          method: "POST",
          body: JSON.stringify(data),
        });
        localStorage.setItem("jwtToken", result.token);
        this.showMessage("Uspešna prijava!", "success");
        this.checkAuthState();
      } catch (error) {
        this.displayErrors(error, e.target);
      }
    },

    async handleRegister(e) {
      e.preventDefault();
      this.clearErrors();
      const formData = new FormData(e.target);
      const data = Object.fromEntries(formData.entries());

      try {
        await this.apiFetch(config.api.routes.register, {
          method: "POST",
          body: JSON.stringify(data),
        });
        this.showMessage(
          "Uspešna registracija! Sada se možete prijaviti.",
          "success",
        );
        e.target.reset();
      } catch (error) {
        this.displayErrors(error, e.target);
      }
    },

    handleLogout() {
      localStorage.removeItem("jwtToken");
      this.state.token = null;
      this.state.user = null;
      this.showMessage("Uspešno ste se odjavili.", "success");
      this.showView(config.roles.GUEST);
    },

    showView(role) {
      this.elements.views.all.forEach((v) => v.classList.add("hidden"));
      this.elements.views.auth.classList.add("hidden");
      this.elements.userInfo.classList.add("hidden");

      switch (role) {
        case config.roles.PATIENT:
          this.elements.views.patient.classList.remove("hidden");
          this.initializePatientView();
          break;
        case config.roles.DOCTOR:
          this.elements.views.doctor.classList.remove("hidden");
          this.loadAppointments("doctor");
          break;
        case config.roles.ADMIN:
          this.elements.views.admin.classList.remove("hidden");
          break;
        default:
          this.elements.views.auth.classList.remove("hidden");
      }
    },

    initializePatientView() {
      this.loadDoctorsForPatient();
      this.loadAppointments("patient");
      this.setupBookingForm();
    },

    async loadAppointments(viewType) {
      const isPatientView = viewType === "patient";
      const container = isPatientView
        ? this.elements.patientAppointmentsContainer
        : this.elements.doctorAppointmentsContainer;
      const filter = isPatientView
        ? this.elements.patientAppointmentFilter.value
        : this.elements.doctorAppointmentFilter.value;

      container.innerHTML = "Učitavanje termina...";

      try {
        let URL = config.api.routes.appointments;
        if(!isPatientView) URL+="?status=CONFIRMED";

        let appointments = await this.apiFetch(URL);
        const now = new Date();
        if (filter === "future") {
          appointments = appointments.filter(
            (app) => new Date(app.appointmentTime) >= now,
          );
        } else if (filter === "past") {
          appointments = appointments.filter(
            (app) => new Date(app.appointmentTime) < now,
          );
        }
        appointments.sort((a, b) => {
          const dateA = new Date(a.appointmentTime);
          const dateB = new Date(b.appointmentTime);
          return filter === "past" ? dateB - dateA : dateA - dateB;
        });

        this.renderAppointmentsTable(appointments, container, viewType);
      } catch (error) {
        this.showMessage("Nije moguće učitati listu termina.", "error");
        container.innerHTML = "<p>Greška pri učitavanju termina.</p>";
      }
    },

    renderAppointmentsTable(appointments, container, viewType) {
      container.innerHTML = "";
      if (appointments.length === 0) {
        container.innerHTML =
          "<p>Nema termina koji odgovaraju izabranom filteru.</p>";
        return;
      }

      const table = document.createElement("table");
      const headerRow =
        viewType === "doctor"
          ? "<th>Pacijent</th><th>JMBG</th>"
          : "<th>Lekar</th><th>Specijalizacija</th>";

      table.innerHTML = `
        <thead>
          <tr>
            ${headerRow}
            <th>Datum i Vreme</th>
            <th>Status</th>
          </tr>
        </thead>
        <tbody>
          ${appointments
            .map((app) => this.createAppointmentRow(app, viewType))
            .join("")}
        </tbody>
      `;
      container.appendChild(table);
    },

    createAppointmentRow(app, viewType) {
      const formattedDateTime = new Date(app.appointmentTime).toLocaleString(
        "sr-RS",
        {
          year: "numeric",
          month: "long",
          day: "numeric",
          hour: "2-digit",
          minute: "2-digit",
        },
      );

      const statusMap = {
        PENDING: { text: "Na čekanju", class: "status-pending" },
        CONFIRMED: { text: "Potvrđen", class: "status-confirmed" },
        REJECTED: { text: "Odbijen", class: "status-rejected" },
      };
      const statusInfo = statusMap[app.status] || {
        text: app.status,
        class: "",
      };

      const mainCell =
        viewType === "doctor"
          ? `<td>${app.patient.firstName} ${app.patient.lastName}</td><td>${
              app.patient.jmbg || "N/A"
            }</td>`
          : `<td>Dr. ${app.doctor.firstName} ${app.doctor.lastName}</td><td>${app.doctor.specialization}</td>`;

      return `
        <tr>
          ${mainCell}
          <td>${formattedDateTime}</td>
          <td><span class="status ${statusInfo.class}">${statusInfo.text}</span></td>
        </tr>
      `;
    },

    setupBookingForm() {
      const today = new Date().toISOString().split("T")[0];
      this.elements.appointmentDate.value = today;
      this.elements.appointmentDate.min = today;
      this.renderTimetable();
    },

    async renderTimetable() {
      const doctorId = this.elements.doctorSelect.value;
      const selectedDate = this.elements.appointmentDate.value;
      const container = this.elements.timetableContainer;

      this.elements.bookAppointmentBtn.disabled = true;
      this.state.selectedSlotTime = null;

      if (!doctorId || !selectedDate) {
        container.innerHTML =
          "<p>Izaberite lekara i datum da vidite raspored.</p>";
        return;
      }
      container.innerHTML = "Učitavanje rasporeda...";

      try {
        const appointments = await this.apiFetch(
          `${config.api.routes.doctors}/${doctorId}/appointments`,
        );
        const bookedTimes = new Set(
          appointments.map((app) => new Date(app.appointmentTime).getTime()),
        );

        const timetableGrid = document.createElement("div");
        timetableGrid.className = "timetable";

        const [year, month, day] = selectedDate.split("-").map(Number);

        for (let i = 0; i < 32; i++) {
          const hour = 9 + Math.floor((i * 15) / 60);
          const minute = (i * 15) % 60;

          if (hour >= 17) continue;

          const slotTimeObject = new Date(year, month - 1, day, hour, minute);

          const timeSlotDiv = document.createElement("div");
          timeSlotDiv.className = "time-slot";
          timeSlotDiv.textContent = slotTimeObject.toLocaleTimeString("sr-RS", {
            hour: "2-digit",
            minute: "2-digit",
          });

          const localTimeString = `${selectedDate}T${String(hour).padStart(
            2,
            "0",
          )}:${String(minute).padStart(2, "0")}`;
          timeSlotDiv.dataset.time = localTimeString;

          if (bookedTimes.has(slotTimeObject.getTime())) {
            timeSlotDiv.classList.add("unavailable");
          } else {
            timeSlotDiv.classList.add("available");
          }
          timetableGrid.appendChild(timeSlotDiv);
        }
        container.innerHTML = "";
        container.appendChild(timetableGrid);

        timetableGrid.addEventListener("click", (e) => {
          if (e.target.classList.contains("available")) {
            timetableGrid
              .querySelectorAll(".time-slot.selected")
              .forEach((s) => s.classList.remove("selected"));
            e.target.classList.add("selected");

            this.state.selectedSlotTime = e.target.dataset.time;

            this.elements.bookAppointmentBtn.disabled = false;
          }
        });
      } catch (error) {
        container.innerHTML = `<p>Greška pri učitavanju termina lekara.</p>`;
      }
    },

    async handleBookingSubmit(e) {
      e.preventDefault();
      if (!this.state.selectedSlotTime) {
        this.showMessage("Molimo vas da prvo izaberete termin.", "error");
        return;
      }

      const doctorId = this.elements.doctorSelect.value;
      const appointmentTime = this.state.selectedSlotTime;

      try {
        await this.apiFetch(config.api.routes.appointments, {
          method: "POST",
          body: JSON.stringify({ doctorId, appointmentTime }),
        });
        this.showMessage(
          "Zahtev za termin je uspešno poslat i čeka obradu.",
          "success",
        );
        this.renderTimetable();
        this.loadAppointments("patient");
      } catch (error) {
        this.displayErrors(error, e.target);
      }
    },

    async handleAddDoctor(e) {
      e.preventDefault();
      this.clearErrors();
      const formData = new FormData(e.target);
      const data = Object.fromEntries(formData.entries());

      try {
        await this.apiFetch(config.api.routes.doctors, {
          method: "POST",
          body: JSON.stringify(data),
        });
        this.showMessage("Novi doktor je uspešno dodat!", "success");
        e.target.reset();
      } catch (error) {
        this.displayErrors(error, e.target);
      }
    },

    async loadDoctorsForPatient() {
      const select = this.elements.doctorSelect;
      select.innerHTML = '<option value="">-- Izaberite lekara --</option>';
      try {
        const doctors = await this.apiFetch(config.api.routes.doctors);
        doctors.forEach((doctor) => {
          const option = document.createElement("option");
          option.value = doctor.id;
          option.textContent = `Dr. ${doctor.firstName} ${doctor.lastName} (${doctor.specialization})`;
          select.appendChild(option);
        });
      } catch (error) {
        this.showMessage("Nije moguće učitati listu lekara.", "error");
      }
    },

    parseJwt(token) {
      const base64Url = token.split(".")[1];
      const base64 = base64Url.replace(/-/g, "+").replace(/_/g, "/");
      const jsonPayload = decodeURIComponent(
        atob(base64)
          .split("")
          .map((c) => "%" + ("00" + c.charCodeAt(0).toString(16)).slice(-2))
          .join(""),
      );
      const payload = JSON.parse(jsonPayload);
      payload.roles =
        payload.roles || (payload.authorities || []).map((a) => a.authority);
      return payload;
    },

    showMessage(msg, type = "info") {
      this.elements.messageArea.textContent = msg;
      this.elements.messageArea.className = type;
      setTimeout(() => {
        this.elements.messageArea.textContent = "";
        this.elements.messageArea.className = "";
      }, 4000);
    },

    displayErrors(errorData, formElement = null) {
      if (errorData.message) {
        this.showMessage(errorData.message, "error");
      }
      let message = "";
      if (errorData.errors && formElement) {
        for (const field in errorData.errors) {
          const inputElement = formElement.querySelector(`[name="${field}"]`);
          if (inputElement) {
            const errorSpan = document.createElement("span");
            errorSpan.className = "field-error";
            errorSpan.textContent = errorData.errors[field];
            inputElement.insertAdjacentElement("afterend", errorSpan);
          }
          else {
            message+=errorData.errors[field];
            message+=" ";
          }
        }
        if(message !== "") {
          this.showMessage(message, "error");
        }
      }
    },

    clearErrors() {
      document
        .querySelectorAll(".field-error")
        .forEach((el) => el.remove());
      this.elements.messageArea.textContent = "";
      this.elements.messageArea.className = "";
    },
  };
  App.init();
});