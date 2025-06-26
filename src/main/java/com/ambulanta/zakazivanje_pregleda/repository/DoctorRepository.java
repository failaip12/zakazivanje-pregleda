package com.ambulanta.zakazivanje_pregleda.repository;

import com.ambulanta.zakazivanje_pregleda.model.Doctor;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface DoctorRepository extends JpaRepository<Doctor, Long> {
    Optional<Doctor> findByFirstNameAndLastName(String firstName, String lastName);
}
