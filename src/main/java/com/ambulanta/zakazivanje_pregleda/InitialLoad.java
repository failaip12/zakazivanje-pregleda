package com.ambulanta.zakazivanje_pregleda;

import com.ambulanta.zakazivanje_pregleda.model.Doctor;
import com.ambulanta.zakazivanje_pregleda.model.Patient;
import com.ambulanta.zakazivanje_pregleda.model.Role;
import com.ambulanta.zakazivanje_pregleda.model.User;
import com.ambulanta.zakazivanje_pregleda.repository.UserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
public class InitialLoad implements CommandLineRunner {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public InitialLoad(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(String... args) throws Exception {
        if (userRepository.count() == 0) {
            System.out.println("Učitavanje početnih podataka za korisnike...");
            loadAdminData();
            loadDoctorData();
            loadPatientData();
            System.out.println("Svi početni korisnici uspešno učitani.");
        }
    }

    private void loadAdminData() {
        User admin = new User();
        admin.setUsername("admin");
        admin.setPassword(passwordEncoder.encode("admin123"));
        admin.setRole(Role.ROLE_ADMIN);

        userRepository.save(admin);
        System.out.println("Admin korisnik kreiran. (Username: admin, Password: admin123)");
    }

    private void loadDoctorData() {
        User doctorUser1 = new User();
        doctorUser1.setUsername("PetPet");
        doctorUser1.setPassword(passwordEncoder.encode("doctor1pass"));
        doctorUser1.setFirstName("Petar");
        doctorUser1.setLastName("Petrović");
        doctorUser1.setRole(Role.ROLE_DOCTOR);

        Doctor doctor1 = new Doctor();
        doctor1.setSpecialization("Opšta praksa");
        doctorUser1.setDoctor(doctor1);

        userRepository.save(doctorUser1);
        System.out.println("Lekar Petar Petrović kreiran. (Username: PetPet, Password: doctor1pass)");

        User doctorUser2 = new User();
        doctorUser2.setUsername("JovJov");
        doctorUser2.setPassword(passwordEncoder.encode("doctor2pass"));
        doctorUser2.setFirstName("Jovana");
        doctorUser2.setLastName("Jovanović");
        doctorUser2.setRole(Role.ROLE_DOCTOR);

        Doctor doctor2 = new Doctor();
        doctor2.setSpecialization("Kardiolog");
        doctorUser2.setDoctor(doctor2);

        userRepository.save(doctorUser2);
        System.out.println("Lekar Jovana Jovanović kreirana. (Username: JovJov, Password: doctor2pass)");
    }

    private void loadPatientData() {
        User patientUser1 = new User();
        patientUser1.setUsername("MarMar");
        patientUser1.setPassword(passwordEncoder.encode("patient1pass"));
        patientUser1.setFirstName("Marko");
        patientUser1.setLastName("Marković");
        patientUser1.setRole(Role.ROLE_PATIENT);

        Patient patient1 = new Patient();
        patient1.setJmbg("1111111111111");

        patientUser1.setPatient(patient1);

        userRepository.save(patientUser1);
        System.out.println("Pacijent Marko Marković kreiran. (Username: MarMar, Password: patient1pass)");
    }
}