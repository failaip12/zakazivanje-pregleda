package com.ambulanta.zakazivanje_pregleda.repository;


import com.ambulanta.zakazivanje_pregleda.model.Patient;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface PatientRepository extends JpaRepository<Patient, Long> {
    Optional<Patient> findByJmbg(String jmbg);
}