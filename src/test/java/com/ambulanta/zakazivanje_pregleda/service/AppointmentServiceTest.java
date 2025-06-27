package com.ambulanta.zakazivanje_pregleda.service;

import com.ambulanta.zakazivanje_pregleda.dto.AppointmentRequestDTO;
import com.ambulanta.zakazivanje_pregleda.exception.DoctorNotFoundException;
import com.ambulanta.zakazivanje_pregleda.messaging.AppointmentRequestProducer;
import com.ambulanta.zakazivanje_pregleda.model.*;
import com.ambulanta.zakazivanje_pregleda.repository.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AppointmentServiceTest {

    @Mock
    private AppointmentRepository appointmentRepository;
    @Mock
    private PatientRepository patientRepository;
    @Mock
    private DoctorRepository doctorRepository;
    @Mock
    private AppointmentRequestProducer producer;

    @InjectMocks
    private AppointmentService appointmentService;

    private AppointmentRequestDTO requestDTO;
    private Doctor doctor;
    private Patient patient;

    @BeforeEach
    void setUp() {
        requestDTO = new AppointmentRequestDTO();
        requestDTO.setDoctorFirstName("Petar");
        requestDTO.setDoctorLastName("Petrović");
        requestDTO.setPatientFirstName("Marko");
        requestDTO.setPatientLastName("Marković");
        requestDTO.setPatientJmbg("1234567890123");
        requestDTO.setAppointmentTime(LocalDateTime.now().plusDays(1));

        doctor = new Doctor(1L, "Petar", "Petrović", "Opšta praksa");
        patient = new Patient(1L, "Marko", "Marković", "1234567890123");
    }

    @Test
    void whenCreateAppointment_withValidData_shouldReturnPendingAppointment() {
        when(doctorRepository.findByFirstNameAndLastName("Petar", "Petrović")).thenReturn(Optional.of(doctor));
        when(patientRepository.findByJmbg("1234567890123")).thenReturn(Optional.of(patient));

        when(appointmentRepository.save(any(Appointment.class))).thenAnswer(invocation -> {
            Appointment app = invocation.getArgument(0);
            app.setId(1L);
            return app;
        });

        Appointment result = appointmentService.createAppointmentRequest(requestDTO);

        assertThat(result).isNotNull();
        assertThat(result.getStatus()).isEqualTo(AppointmentStatus.PENDING);
        assertThat(result.getDoctor().getFirstName()).isEqualTo("Petar");
        assertThat(result.getPatient().getJmbg()).isEqualTo("1234567890123");
        assertThat(result.getId()).isEqualTo(1L);

        verify(producer, times(1)).send(1L);
    }

    @Test
    void whenCreateAppointment_withNonExistentDoctor_shouldThrowDoctorNotFoundException() {
        when(doctorRepository.findByFirstNameAndLastName(anyString(), anyString())).thenReturn(Optional.empty());

        assertThrows(DoctorNotFoundException.class, () -> {
            appointmentService.createAppointmentRequest(requestDTO);
        });

        verify(appointmentRepository, never()).save(any());
        verify(producer, never()).send(anyLong());
    }
}