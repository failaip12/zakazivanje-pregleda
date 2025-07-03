package com.ambulanta.zakazivanje_pregleda.repository;

import com.ambulanta.zakazivanje_pregleda.model.Appointment;
import com.ambulanta.zakazivanje_pregleda.model.AppointmentStatus;
import com.ambulanta.zakazivanje_pregleda.model.Doctor;
import com.ambulanta.zakazivanje_pregleda.model.Patient;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface AppointmentRepository extends JpaRepository<Appointment, Long> {
    Optional<Appointment> findByDoctorAndAppointmentTimeAndStatus(Doctor doctor, LocalDateTime appointmentTime, AppointmentStatus status);

    @Query("SELECT a FROM Appointment a " +
            "JOIN FETCH a.patient p JOIN FETCH p.user " +
            "JOIN FETCH a.doctor d JOIN FETCH d.user")
    @Override
    List<Appointment> findAll();

    @Query("SELECT a FROM Appointment a " +
            "JOIN FETCH a.patient p JOIN FETCH p.user " +
            "JOIN FETCH a.doctor d JOIN FETCH d.user " +
            "WHERE a.status = :status")
    List<Appointment> findByStatus(AppointmentStatus status);

    @Query("SELECT a FROM Appointment a " +
            "JOIN FETCH a.patient p JOIN FETCH p.user " +
            "JOIN FETCH a.doctor d JOIN FETCH d.user " +
            "WHERE a.doctor = :doctor")
    List<Appointment> findByDoctor(Doctor doctor);

    @Query("SELECT a FROM Appointment a " +
            "JOIN FETCH a.patient p JOIN FETCH p.user " +
            "JOIN FETCH a.doctor d JOIN FETCH d.user " +
            "WHERE a.doctor = :doctor AND a.status = :status")
    List<Appointment> findByDoctorAndStatus(Doctor doctor, AppointmentStatus status);

    @Query("SELECT a FROM Appointment a " +
            "JOIN FETCH a.patient p JOIN FETCH p.user " +
            "JOIN FETCH a.doctor d JOIN FETCH d.user " +
            "WHERE a.patient = :patient")
    List<Appointment> findByPatient(Patient patient);

    @Query("SELECT a FROM Appointment a " +
            "JOIN FETCH a.patient p JOIN FETCH p.user " +
            "JOIN FETCH a.doctor d JOIN FETCH d.user " +
            "WHERE a.patient = :patient AND a.status = :status")
    List<Appointment> findByPatientAndStatus(Patient patient, AppointmentStatus status);

    @Query("SELECT a FROM Appointment a WHERE a.doctor = :doctor " +
            "AND a.status = 'CONFIRMED' " +
            "AND a.appointmentTime > :lowerBound " +
            "AND a.appointmentTime < :upperBound")
    List<Appointment> findConflictingAppointments(
            @Param("doctor") Doctor doctor,
            @Param("newAppointmentTime") LocalDateTime newAppointmentTime,
            @Param("lowerBound") LocalDateTime lowerBound,
            @Param("upperBound") LocalDateTime upperBound
    );
}