package com.ambulanta.zakazivanje_pregleda.repository;

import com.ambulanta.zakazivanje_pregleda.model.Doctor;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface DoctorRepository extends JpaRepository<Doctor, Long> {
    @Query("SELECT d FROM Doctor d JOIN User u ON u.doctor.id = d.id WHERE u.firstName = :firstName AND u.lastName = :lastName")
    Optional<Doctor> findByDoctorUserFirstNameAndLastName(@Param("firstName") String firstName, @Param("lastName") String lastName);
}
