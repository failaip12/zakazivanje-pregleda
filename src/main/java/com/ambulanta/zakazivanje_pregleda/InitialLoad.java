package com.ambulanta.zakazivanje_pregleda.config;

import com.ambulanta.zakazivanje_pregleda.model.Doctor;
import com.ambulanta.zakazivanje_pregleda.model.Patient;
import com.ambulanta.zakazivanje_pregleda.repository.DoctorRepository;
import com.ambulanta.zakazivanje_pregleda.repository.PatientRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
public class InitialLoad implements CommandLineRunner {

    private final DoctorRepository doctorRepository;
    private final PatientRepository patientRepository;

    public InitialLoad(DoctorRepository doctorRepository, PatientRepository patientRepository) {
        this.doctorRepository = doctorRepository;
        this.patientRepository = patientRepository;
    }

    @Override
    public void run(String... args) throws Exception {
        if (doctorRepository.count() == 0) {
            loadDoctorData();
        }
        if (patientRepository.count() == 0) {
            loadPatientData();
        }
    }

    private void loadDoctorData() {
        System.out.println("Učitavanje početnih podataka za lekare...");

        Doctor doctor1 = new Doctor();
        doctor1.setFirstName("Petar");
        doctor1.setLastName("Petrović");
        doctor1.setSpecialization("Opšta praksa");

        Doctor doctor2 = new Doctor();
        doctor2.setFirstName("Jovana");
        doctor2.setLastName("Jovanović");
        doctor2.setSpecialization("Kardiolog");

        Doctor doctor3 = new Doctor();
        doctor3.setFirstName("Milan");
        doctor3.setLastName("Milanović");
        doctor3.setSpecialization("Pedijatar");

        doctorRepository.save(doctor1);
        doctorRepository.save(doctor2);
        doctorRepository.save(doctor3);

        System.out.println("Lekari uspešno učitani.");
    }

    private void loadPatientData() {
        System.out.println("Učitavanje početnih podataka za pacijente...");

        Patient patient1 = new Patient();
        patient1.setFirstName("Marko");
        patient1.setLastName("Marković");
        patient1.setJmbg("1111111111111");

        Patient patient2 = new Patient();
        patient2.setFirstName("Ana");
        patient2.setLastName("Anić");
        patient2.setJmbg("2222222222222");

        patientRepository.save(patient1);
        patientRepository.save(patient2);

        System.out.println("Pacijenti uspešno učitani.");
    }
}