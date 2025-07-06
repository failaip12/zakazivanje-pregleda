package com.ambulanta.zakazivanje_pregleda;

import com.ambulanta.zakazivanje_pregleda.model.*;
import com.ambulanta.zakazivanje_pregleda.repository.AppointmentRepository;
import com.ambulanta.zakazivanje_pregleda.repository.DoctorRepository;
import com.ambulanta.zakazivanje_pregleda.repository.PatientRepository;
import com.ambulanta.zakazivanje_pregleda.repository.UserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.LocalTime;

@Component
public class InitialLoad implements CommandLineRunner {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final AppointmentRepository appointmentRepository;

    public InitialLoad(AppointmentRepository appointmentRepository, UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.appointmentRepository = appointmentRepository;
    }

    @Override
    public void run(String... args) throws Exception {
        if (userRepository.count() == 0) {
            System.out.println("Učitavanje početnih podataka za korisnike...");
            loadAdminData();
            loadDoctorData();
            loadPatientData();
            loadAppointmentData();
            System.out.println("Svi početni korisnici uspešno učitani.");
        }
    }
    private void loadAppointmentData() {
        Appointment appointment = new Appointment();
        appointment.setAppointmentTime(LocalDateTime.now().plusDays(1).with(LocalTime.of(11, 45)));
        appointment.setStatus(AppointmentStatus.CONFIRMED);
        appointment.setDoctor(userRepository.findByUsername("PetPet").orElseThrow().getDoctor());
        appointment.setPatient(userRepository.findByUsername("MarMar").orElseThrow().getPatient());
        appointmentRepository.save(appointment);

        Appointment appointment2 = new Appointment();
        appointment2.setAppointmentTime(LocalDateTime.now().plusDays(1).with(LocalTime.of(11, 45)));
        appointment2.setStatus(AppointmentStatus.REJECTED);
        appointment2.setDoctor(userRepository.findByUsername("PetPet").orElseThrow().getDoctor());
        appointment2.setPatient(userRepository.findByUsername("MarMar").orElseThrow().getPatient());
        appointmentRepository.save(appointment2);

        Appointment appointment3 = new Appointment();
        appointment3.setAppointmentTime(LocalDateTime.now().minusDays(1).with(LocalTime.of(11, 45)));
        appointment3.setStatus(AppointmentStatus.CONFIRMED);
        appointment3.setDoctor(userRepository.findByUsername("PetPet").orElseThrow().getDoctor());
        appointment3.setPatient(userRepository.findByUsername("MarMar").orElseThrow().getPatient());
        appointmentRepository.save(appointment3);

        Appointment appointment4 = new Appointment();
        appointment4.setAppointmentTime(LocalDateTime.now().minusDays(1).with(LocalTime.of(11, 45)));
        appointment4.setStatus(AppointmentStatus.REJECTED);
        appointment4.setDoctor(userRepository.findByUsername("PetPet").orElseThrow().getDoctor());
        appointment4.setPatient(userRepository.findByUsername("MarMar").orElseThrow().getPatient());
        appointmentRepository.save(appointment4);

        Appointment appointment5 = new Appointment();
        appointment5.setAppointmentTime(LocalDateTime.now().plusDays(1).with(LocalTime.of(9, 45)));
        appointment5.setStatus(AppointmentStatus.CONFIRMED);
        appointment5.setDoctor(userRepository.findByUsername("JovJov").orElseThrow().getDoctor());
        appointment5.setPatient(userRepository.findByUsername("MarMar").orElseThrow().getPatient());
        appointmentRepository.save(appointment5);

        Appointment appointment6 = new Appointment();
        appointment6.setAppointmentTime(LocalDateTime.now().plusDays(1).with(LocalTime.of(9, 45)));
        appointment6.setStatus(AppointmentStatus.REJECTED);
        appointment6.setDoctor(userRepository.findByUsername("JovJov").orElseThrow().getDoctor());
        appointment6.setPatient(userRepository.findByUsername("MarMar").orElseThrow().getPatient());
        appointmentRepository.save(appointment6);

        Appointment appointment7 = new Appointment();
        appointment7.setAppointmentTime(LocalDateTime.now().minusDays(1).with(LocalTime.of(9, 45)));
        appointment7.setStatus(AppointmentStatus.CONFIRMED);
        appointment7.setDoctor(userRepository.findByUsername("JovJov").orElseThrow().getDoctor());
        appointment7.setPatient(userRepository.findByUsername("MarMar").orElseThrow().getPatient());
        appointmentRepository.save(appointment7);

        Appointment appointment8 = new Appointment();
        appointment8.setAppointmentTime(LocalDateTime.now().minusDays(1).with(LocalTime.of(9, 45)));
        appointment8.setStatus(AppointmentStatus.REJECTED);
        appointment8.setDoctor(userRepository.findByUsername("JovJov").orElseThrow().getDoctor());
        appointment8.setPatient(userRepository.findByUsername("MarMar").orElseThrow().getPatient());
        appointmentRepository.save(appointment8);
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