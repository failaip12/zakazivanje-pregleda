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
    private final UserRepository userRepository;
    private final DoctorRepository doctorRepository;
    private final AppointmentRequestProducer producer;

    @Transactional
    public Appointment createAppointmentRequest(AppointmentRequestDTO requestDTO, String username) {
        Doctor doctor = doctorRepository.findById(requestDTO.getDoctorId())
                .orElseThrow(() -> new DoctorNotFoundException(
                        "Lekar sa ID: " + requestDTO.getDoctorId() + " nije pronađen."
                ));

        User patientUser = userRepository.findByUsername(username)
                .orElseThrow(() -> new PatientNotFoundException("Pacijent sa korisničkim imenom: " + username + " nije pronađen."));

        Patient patient = patientUser.getPatient();
        if (patient == null) {
            throw new IllegalStateException("Korisnik sa ulogom pacijenta nema povezane podatke o pacijentu.");
        }
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
    public List<AppointmentResponseDTO> getAppointmentsForDoctor(Long doctorId, AppointmentStatus status) {
        Doctor doctor = doctorRepository.findById(doctorId)
                .orElseThrow(() -> new DoctorNotFoundException("Doktor sa ID: " + doctorId + " nije pronađen."));

        List<Appointment> appointments;
        if (status != null) {
            appointments = appointmentRepository.findByDoctorAndStatus(doctor, status);
        } else {
            appointments = appointmentRepository.findByDoctor(doctor);
        }

        return appointments.stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }
    @Transactional
    public List<AppointmentResponseDTO> getAppointmentsForUser(String username, AppointmentStatus status) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new DoctorNotFoundException("Korisnik sa username: " + username + " nije pronađen."));
        Role role = user.getRole();
        switch (role) {
            case ROLE_DOCTOR -> {
                if (user.getDoctor() == null) {
                    throw new IllegalStateException("Korisnik sa ulogom doktora nema povezane podatke o doktoru.");
                }

                return getAppointmentsForDoctor(user.getDoctor().getId(), status);
            }
            case ROLE_PATIENT -> {
                if (user.getPatient() == null) {
                    throw new IllegalStateException("Korisnik sa ulogom pacijenta nema povezane podatke o pacijentu.");
                }

                return getAppointmentsForPatient(user.getPatient().getId(), status);
            }
            case ROLE_ADMIN -> {
                if (status != null) {
                    return getAppointmentsByStatus(status);
                } else {
                    return getAllAppointments();
                }
            }

            default -> throw new EnumConstantNotPresentException(Role.class, role.name());
        }
    }
    @Transactional
    public List<AppointmentResponseDTO> getAppointmentsForPatient(Long patientId, AppointmentStatus status) {
        Patient patient = patientRepository.findById(patientId)
                .orElseThrow(() -> new PatientNotFoundException("Pacijent sa ID: " + patientId + " nije pronađen."));

        List<Appointment> appointments;
        if (status != null) {
            appointments = appointmentRepository.findByPatientAndStatus(patient, status);
        } else {
            appointments = appointmentRepository.findByPatient(patient);
        }

        return appointments.stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
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
        User patientUser = appointment.getPatient().getUser();
        User doctorUser = appointment.getDoctor().getUser();

        PatientDTO patientDTO = new PatientDTO();
        patientDTO.setId(appointment.getPatient().getId());
        patientDTO.setFirstName(patientUser.getFirstName());
        patientDTO.setLastName(patientUser.getLastName());

        DoctorDTO doctorDTO = new DoctorDTO();
        doctorDTO.setId(appointment.getDoctor().getId());
        doctorDTO.setSpecialization(appointment.getDoctor().getSpecialization());
        doctorDTO.setFirstName(doctorUser.getFirstName());
        doctorDTO.setLastName(doctorUser.getLastName());

        AppointmentResponseDTO dto = new AppointmentResponseDTO();
        dto.setId(appointment.getId());
        dto.setAppointmentTime(appointment.getAppointmentTime());
        dto.setStatus(appointment.getStatus());
        dto.setPatient(patientDTO);
        dto.setDoctor(doctorDTO);

        return dto;
    }
}