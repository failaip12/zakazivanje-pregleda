package com.ambulanta.zakazivanje_pregleda.repository;

import com.ambulanta.zakazivanje_pregleda.model.Appointment;
import com.ambulanta.zakazivanje_pregleda.model.AppointmentStatus;
import com.ambulanta.zakazivanje_pregleda.model.Doctor;
import org.springframework.data.jpa.repository.JpaRepository;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface AppointmentRepository extends JpaRepository<Appointment, Long> {
    Optional<Appointment> findByDoctorAndAppointmentTimeAndStatus(Doctor doctor, LocalDateTime appointmentTime, AppointmentStatus status);
    List<Appointment> findByStatus(AppointmentStatus status);
}