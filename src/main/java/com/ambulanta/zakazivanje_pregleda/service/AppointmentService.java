package com.ambulanta.zakazivanje_pregleda.service;

import com.ambulanta.zakazivanje_pregleda.dto.AppointmentRequestDTO;
import com.ambulanta.zakazivanje_pregleda.dto.AppointmentResponseDTO;
import com.ambulanta.zakazivanje_pregleda.dto.DoctorDTO;
import com.ambulanta.zakazivanje_pregleda.dto.PatientDTO;
import com.ambulanta.zakazivanje_pregleda.exception.AppointmentNotFoundException;
import com.ambulanta.zakazivanje_pregleda.exception.DoctorNotFoundException;
import com.ambulanta.zakazivanje_pregleda.exception.PatientNotFoundException;
import com.ambulanta.zakazivanje_pregleda.messaging.AppointmentRequestProducer;
import com.ambulanta.zakazivanje_pregleda.model.*;
import com.ambulanta.zakazivanje_pregleda.repository.*;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AppointmentService {

    private final AppointmentRepository appointmentRepository;
    private final PatientRepository patientRepository;
    private final DoctorRepository doctorRepository;
    private final AppointmentRequestProducer producer;

    @Transactional
    public Appointment createAppointmentRequest(AppointmentRequestDTO requestDTO, Long patientId) {
        Doctor doctor = doctorRepository.findByFirstNameAndLastName(requestDTO.getDoctorFirstName(), requestDTO.getDoctorLastName())
                .orElseThrow(() -> new DoctorNotFoundException(
                        String.format("Lekar sa imenom '%s %s' nije pronađen.", requestDTO.getDoctorFirstName(), requestDTO.getDoctorLastName())
                ));

        Patient patient = patientRepository.findById(patientId)
                .orElseThrow(() -> new PatientNotFoundException("Pacijent sa ID: " + patientId + " nije pronađen."));

        Appointment appointment = new Appointment();
        appointment.setPatient(patient);
        appointment.setDoctor(doctor);
        appointment.setAppointmentTime(requestDTO.getAppointmentTime());
        appointment.setStatus(AppointmentStatus.PENDING);

        Appointment savedAppointment = appointmentRepository.save(appointment);

        producer.send(savedAppointment.getId());

        return savedAppointment;
    }

    @Transactional
    public void processAppointment(Long appointmentId) {
        System.out.println("Processing appointment ID: " + appointmentId);
        Appointment appointment = appointmentRepository.findById(appointmentId)
                .orElseThrow(() -> new AppointmentNotFoundException("Zahtev za termin sa ID: " + appointmentId + " nije pronađen."));

        Optional<Appointment> conflict = appointmentRepository.findByDoctorAndAppointmentTimeAndStatus(
                appointment.getDoctor(),
                appointment.getAppointmentTime(),
                AppointmentStatus.CONFIRMED
        );

        if (conflict.isPresent()) {
            appointment.setStatus(AppointmentStatus.REJECTED);
            System.out.println("Appointment " + appointmentId + " REJECTED due to conflict.");
        } else {
            appointment.setStatus(AppointmentStatus.CONFIRMED);
            System.out.println("Appointment " + appointmentId + " CONFIRMED.");
        }

        appointmentRepository.save(appointment);
    }

    @Transactional
    public List<AppointmentResponseDTO> getAllAppointments() {
        return appointmentRepository.findAll().stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    public List<AppointmentResponseDTO> getAppointmentsByStatus(AppointmentStatus status) {
        return appointmentRepository.findByStatus(status).stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }
    private AppointmentResponseDTO convertToDto(Appointment appointment) {
        PatientDTO patientDTO = new PatientDTO();
        patientDTO.setId(appointment.getPatient().getId());
        patientDTO.setFirstName(appointment.getPatient().getFirstName());
        patientDTO.setLastName(appointment.getPatient().getLastName());

        DoctorDTO doctorDTO = new DoctorDTO();
        doctorDTO.setId(appointment.getDoctor().getId());
        doctorDTO.setFirstName(appointment.getDoctor().getFirstName());
        doctorDTO.setLastName(appointment.getDoctor().getLastName());
        doctorDTO.setSpecialization(appointment.getDoctor().getSpecialization());

        AppointmentResponseDTO dto = new AppointmentResponseDTO();
        dto.setId(appointment.getId());
        dto.setAppointmentTime(appointment.getAppointmentTime());
        dto.setStatus(appointment.getStatus());
        dto.setPatient(patientDTO);
        dto.setDoctor(doctorDTO);

        return dto;
    }
}