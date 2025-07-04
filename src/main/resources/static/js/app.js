document.addEventListener('DOMContentLoaded', () => {
    const authView = document.getElementById('authView');
    const patientView = document.getElementById('patientView');
    const doctorView = document.getElementById('doctorView');
    const adminView = document.getElementById('adminView');
    const userInfo = document.getElementById('userInfo');
    const welcomeMessage = document.getElementById('welcomeMessage');
    const doctorAppointments = document.getElementById('doctorAppointments');

    document.getElementById('loginForm').addEventListener('submit', handleLogin);
    document.getElementById('registerForm').addEventListener('submit', handleRegister);
    document.getElementById('logoutButton').addEventListener('click', handleLogout);
    document.getElementById('appointmentForm').addEventListener('submit', handleCreateAppointment);
    document.getElementById('addDoctorForm').addEventListener('submit', handleAddDoctor);

    checkAuthState();

    function checkAuthState() {
        const token = localStorage.getItem('jwtToken');
        if (token) {
            const payload = parseJwt(token);
            if (payload) {
                const userRole = payload.roles[0];
                showView(userRole);
                userInfo.classList.remove('hidden');
                welcomeMessage.textContent = `Prijavljeni ste kao: ${payload.sub}`;
            } else {
                handleLogout();
            }
        } else {
            showView('GUEST');
        }
    }

    function showView(role) {
        document.querySelectorAll('.view').forEach(v => v.classList.add('hidden'));
        authView.classList.add('hidden');
        userInfo.classList.add('hidden');

        if (role === 'ROLE_PATIENT') {
            patientView.classList.remove('hidden');
            loadDoctorsForPatient();
            loadPatientAppointments();
        } else if (role === 'ROLE_DOCTOR') {
            doctorView.classList.remove('hidden');
            loadDoctorAppointments();
        } else if (role === 'ROLE_ADMIN') {
            adminView.classList.remove('hidden');
        } else {
            authView.classList.remove('hidden');
        }
    }

    async function handleLogin(e) {
        e.preventDefault();
        const username = document.getElementById('loginUsername').value;
        const password = document.getElementById('loginPassword').value;

        clearErrorMessages();

        try {
            const response = await fetch('/api/auth/login', {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json'
                },
                body: JSON.stringify({
                    username,
                    password
                })
            });

            if (response.ok) {
                const data = await response.json();
                localStorage.setItem('jwtToken', data.token);
                showMessage('Uspešna prijava!', 'success');
                checkAuthState();
            } else {
                const errorData = await response.json().catch(() => ({ message: 'Pogrešno korisničko ime ili lozinka.' }));
                displayErrors(errorData);
            }

        } catch (error) {
            //showMessage(error.message, 'error');
            showMessage('Mrežna greška. Proverite konekciju.', 'error');
        }
    }

    async function handleRegister(e) {
        e.preventDefault();
        const firstName = document.getElementById('registerFirstName').value;
        const lastName = document.getElementById('registerLastName').value;
        const username = document.getElementById('registerUsername').value;
        const jmbg = document.getElementById('registerJMBG').value;
        const password = document.getElementById('registerPassword').value;

        clearErrorMessages();

        try {
            const response = await fetch('/api/auth/register', {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json'
                },
                body: JSON.stringify({
                    username,
                    password,
                    jmbg,
                    firstName,
                    lastName
                })
            });

            if (response.ok) {
                const data = await response.json();
                localStorage.setItem('jwtToken', data.token);
                showMessage('Uspešna registracija! Sada se možete prijaviti.', 'success');
                e.target.reset();
            } else {
                const errorData = await response.json();
                displayErrors(errorData);
            }

        } catch (error) {
            showMessage(error.message, 'error');
        }
    }

    function handleLogout() {
        localStorage.removeItem('jwtToken');
        showMessage('Uspešno ste se odjavili.', 'success');
        checkAuthState();
    }

    async function handleCreateAppointment(e) {
        e.preventDefault();

        const token = localStorage.getItem('jwtToken');
        const doctorId = document.getElementById('doctorId').value;
        const appointmentTime = document.getElementById('appointmentTime').value;

        clearErrorMessages();

        try {
            const response = await fetch('/api/appointments', {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json',
                    'Authorization': `Bearer ${token}`
                },
                body: JSON.stringify({
                    doctorId,
                    appointmentTime
                })
            });

            if (response.ok) {
                showMessage('Zahtev za termin je uspešno poslat i čeka obradu.', 'success');
                //document.getElementById('appointmentForm').reset();
                setTimeout(loadPatientAppointments, 1000);
            } else {
                const errorData = await response.json();
                displayErrors(errorData);
            }
        } catch (networkError) {
            showMessage('Mrežna greška. Proverite konekciju.', 'error');
            console.error('Network error:', networkError);
        }
    }

    async function loadDoctorsForPatient() {
        const token = localStorage.getItem('jwtToken');
        const doctorSelect = document.getElementById('doctorId');

        doctorSelect.innerHTML = '<option value="">-- Izaberite lekara --</option>';

        if (!token) {
            showMessage('Niste prijavljeni.', 'error');
            return;
        }

        try {
            const response = await fetch('/api/doctors', {
                method: 'GET',
                headers: {
                    'Authorization': `Bearer ${token}`
                }
            });

            if (!response.ok) {
                throw new Error('Nije moguće učitati listu lekara.');
            }

            const doctors = await response.json();

            doctors.forEach(doctor => {
                const option = document.createElement('option');
                option.value = doctor.id;
                option.textContent = `Dr. ${doctor.firstName} ${doctor.lastName} (${doctor.specialization})`;
                doctorSelect.appendChild(option);
            });

        } catch (error) {
            showMessage(error.message, 'error');
        }
    }
    async function loadPatientAppointments() {
        const token = localStorage.getItem('jwtToken');

        const appointmentsContainer  = document.getElementById('patientAppointments');
        appointmentsContainer.innerHTML = 'Učitavanje termina...';

        if (!token) {
            showMessage('Niste prijavljeni.', 'error');
            appointmentsContainer.innerHTML = '';
            return;
        }

        try {
            const response = await fetch('/api/appointments?status=CONFIRMED', {
                method: 'GET',
                headers: {
                    'Authorization': `Bearer ${token}`
                }
            });

            if (!response.ok) {
                throw new Error('Nije moguće učitati listu termina.');
            }

            const appointments = await response.json();
            renderAppointmentsTable(appointments, appointmentsContainer, 'patient');

        } catch (error) {
            showMessage(error.message, 'error');
            appointmentsContainer.innerHTML = '<p>Greška pri učitavanju termina.</p>';
        }
    }

    function renderAppointmentsTable(appointments, container, viewType) {
        container.innerHTML = '';

        if (appointments.length === 0) {
            container.innerHTML = '<p>Nemate zakazanih termina.</p>';
            return;
        }

        const table = document.createElement('table');
        const headerRow = viewType === 'doctor'
            ? '<th>Pacijent</th><th>JMBG</th>'
            : '<th>Lekar</th><th>Specijalizacija</th>';

        table.innerHTML = `
            <thead>
                <tr>
                    ${headerRow}
                    <th>Datum i Vreme</th>
                    <th>Status</th>
                </tr>
            </thead>
            <tbody></tbody>
        `;

        const tbody = table.querySelector('tbody');

        appointments.forEach(app => {
            const row = document.createElement('tr');

            const formattedDateTime = new Date(app.appointmentTime).toLocaleString('sr-RS', {
                year: 'numeric',
                month: 'long',
                day: 'numeric',
                hour: '2-digit',
                minute: '2-digit'
            });

            let statusText;
            let statusClass;
            switch (app.status) {
                case 'PENDING':
                    statusText = 'Na čekanju';
                    statusClass = 'status-pending';
                    break;
                case 'CONFIRMED':
                    statusText = 'Potvrđen';
                    statusClass = 'status-confirmed';
                    break;
                case 'REJECTED':
                    statusText = 'Odbijen';
                    statusClass = 'status-rejected';
                    break;
                default:
                    statusText = app.status;
                    statusClass = '';
            }

            const mainCell = viewType === 'doctor'
                ? `<td>${app.patient.firstName} ${app.patient.lastName}</td><td>${app.patient.jmbg || 'N/A'}</td>`
                : `<td>Dr. ${app.doctor.firstName} ${app.doctor.lastName}</td><td>${app.doctor.specialization}</td>`;

            row.innerHTML = `
                ${mainCell}
                <td>${formattedDateTime}</td>
                <td><span class="status ${statusClass}">${statusText}</span></td>
            `;

            tbody.appendChild(row);
        });

        container.appendChild(table);
    }

    async function loadDoctorAppointments() {
        const token = localStorage.getItem('jwtToken');

        const appointmentsContainer = document.getElementById('doctorAppointments');
        appointmentsContainer.innerHTML = 'Učitavanje termina...';

        if (!token) {
            showMessage('Niste prijavljeni.', 'error');
            return;
        }

        try {
            const response = await fetch('/api/appointments?status=CONFIRMED', {
                method: 'GET',
                headers: {
                    'Authorization': `Bearer ${token}`
                }
            });

            if (!response.ok) {
                throw new Error('Nije moguće učitati listu zakazivanja.');
            }

            const appointments = await response.json();
            renderAppointmentsTable(appointments, appointmentsContainer, 'doctor');

        } catch (error) {
            showMessage(error.message, 'error');
            appointmentsContainer.innerHTML = '<p>Greška pri učitavanju termina.</p>';
        }
    }
    async function handleAddDoctor(e) {
        e.preventDefault();
        const token = localStorage.getItem('jwtToken');
        const firstName = document.getElementById('doctorFirstName').value;
        const lastName = document.getElementById('doctorLastName').value;
        const username = document.getElementById('doctorUsername').value;
        const password = document.getElementById('doctorPassword').value;
        const specialization = document.getElementById('doctorSpecialization').value;

        clearErrorMessages();

        try {
            const response = await fetch('/api/doctors', {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json',
                    'Authorization': `Bearer ${token}`
                },
                body: JSON.stringify({
                    username,
                    password,
                    firstName,
                    lastName,
                    specialization
                })
            });

            if (response.ok) {
                showMessage('Novi doktor je uspešno dodat!', 'success');
                e.target.reset();
            } else {
                const errorData = await response.json();
                displayErrors(errorData);
            }

        } catch (error) {
            showMessage(error.message, 'error');
        }
    }

    function showMessage(msg, type) {
        messageArea.textContent = msg;
        messageArea.className = type;
        setTimeout(() => messageArea.textContent = '', 4000);
    }

    function parseJwt(token) {
        try {
            const base64Url = token.split('.')[1];
            const base64 = base64Url.replace(/-/g, '+').replace(/_/g, '/');
            const jsonPayload = decodeURIComponent(atob(base64).split('').map(function(c) {
                return '%' + ('00' + c.charCodeAt(0).toString(16)).slice(-2);
            }).join(''));

            const payload = JSON.parse(jsonPayload);
            if (payload.exp * 1000 < Date.now()) {
                console.warn("JWT token je istekao.");
                return null;
            }
            payload.roles = payload.roles || (payload.authorities || []).map(a => a.authority);
            return payload;

        } catch (e) {
            console.error("Greška pri parsiranju JWT:", e);
            return null;
        }
    }

    function displayErrors(errorData) {
        if (errorData.message) {
            showMessage(errorData.message, 'error');
        }

        if (errorData.errors) {
            for (const field in errorData.errors) {
                const inputElement = document.getElementById(field);
                if (inputElement) {
                    const errorSpan = document.createElement('span');
                    errorSpan.className = 'field-error';
                    errorSpan.textContent = errorData.errors[field];

                    inputElement.insertAdjacentElement('afterend', errorSpan);
                }
            }
        }
    }

    function clearErrorMessages() {
        document.querySelectorAll('.field-error').forEach(el => el.remove());
        messageArea.textContent = '';
        messageArea.className = '';
    }

});