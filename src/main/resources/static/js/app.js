document.addEventListener('DOMContentLoaded', () => {
    const authView = document.getElementById('authView');
    const patientView = document.getElementById('patientView');
    const doctorView = document.getElementById('doctorView');
    const adminView = document.getElementById('adminView');
    const userInfo = document.getElementById('userInfo');
    const welcomeMessage = document.getElementById('welcomeMessage');
    const messageArea = document.getElementById('messageArea');

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
            const userRole = payload.roles[0];
            showView(userRole);
            userInfo.classList.remove('hidden');
            welcomeMessage.textContent = `Prijavljeni ste kao: ${payload.sub}`;
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

            if (!response.ok) {
                throw new Error('Pogrešno korisničko ime ili lozinka.');
            }

            const data = await response.json();
            localStorage.setItem('jwtToken', data.token);
            showMessage('Uspešna prijava!', 'success');
            checkAuthState();

        } catch (error) {
            showMessage(error.message, 'error');
        }
    }

    async function handleRegister(e) {
        e.preventDefault();
        const firstName = document.getElementById('registerFirstName').value;
        const lastName = document.getElementById('registerLastName').value;
        const username = document.getElementById('registerUsername').value;
        const password = document.getElementById('registerPassword').value;

        try {
            const response = await fetch('/api/auth/register', {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json'
                },
                body: JSON.stringify({
                    username,
                    password,
                    firstName,
                    lastName
                })
            });

            if (!response.ok) {
                throw new Error('Greska u registraciji.');
            }

            const data = await response.json();
            localStorage.setItem('jwtToken', data.token);
            showMessage('Uspešna registracija!', 'success');
            checkAuthState();

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

            if (response.status === 202) {
                showMessage('Zahtev za termin je uspešno poslat i čeka obradu.', 'success');
                loadPatientAppointments();
            } else {
                const errorData = await response.json();
                throw new Error(errorData.message || 'Greška pri zakazivanju.');
            }
        } catch (error) {
            showMessage(error.message, 'error');
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

            console.log(appointments);

        } catch (error) {
            showMessage(error.message, 'error');
        }
    }
    async function loadDoctorAppointments() {
        const token = localStorage.getItem('jwtToken');

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

            console.log(appointments);

        } catch (error) {
            showMessage(error.message, 'error');
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

            if (!response.ok) {
                throw new Error('Greska pri dodavanju novog doktora.');
            }

            const data = await response.json();
            showMessage('Uspešno dodavanje novog doktora!', 'success');

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
            return JSON.parse(atob(token.split('.')[1]));
        } catch (e) {
            return null;
        }
    }
});