package com.ambulanta.zakazivanje_pregleda.repository;

import com.ambulanta.zakazivanje_pregleda.model.Doctor;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DoctorRepository extends JpaRepository<Doctor, Long> {
}
