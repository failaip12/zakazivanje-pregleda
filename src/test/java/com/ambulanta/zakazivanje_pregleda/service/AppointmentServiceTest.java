package com.ambulanta.zakazivanje_pregleda.service;

import com.ambulanta.zakazivanje_pregleda.dto.AppointmentRequestDTO;
import com.ambulanta.zakazivanje_pregleda.dto.AppointmentResponseDTO;
import com.ambulanta.zakazivanje_pregleda.exception.AppointmentNotFoundException;
import com.ambulanta.zakazivanje_pregleda.exception.DoctorNotFoundException;
import com.ambulanta.zakazivanje_pregleda.messaging.AppointmentRequestProducer;
import com.ambulanta.zakazivanje_pregleda.model.*;
import com.ambulanta.zakazivanje_pregleda.repository.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
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
    void whenProcessAppointment_andTerminIsTaken_shouldRejectAppointment() {
        Long appointmentId = 1L;
        LocalDateTime conflictingTime = LocalDateTime.now().plusDays(2);

        Appointment pendingAppointment = new Appointment(
                appointmentId,
                patient,
                doctor,
                conflictingTime,
                AppointmentStatus.PENDING
        );

        Appointment conflictingAppointment = new Appointment(
                appointmentId+1,
                new Patient(99L, "Drugi", "Pacijent", "9999999999999"),
                doctor,
                conflictingTime,
                AppointmentStatus.CONFIRMED
        );

        when(appointmentRepository.findById(appointmentId)).thenReturn(Optional.of(pendingAppointment));

        when(appointmentRepository.findByDoctorAndAppointmentTimeAndStatus(
                doctor,
                conflictingTime,
                AppointmentStatus.CONFIRMED
        )).thenReturn(Optional.of(conflictingAppointment));

        appointmentService.processAppointment(appointmentId);

        ArgumentCaptor<Appointment> appointmentCaptor = ArgumentCaptor.forClass(Appointment.class);

        verify(appointmentRepository, times(1)).save(appointmentCaptor.capture());

        Appointment savedAppointment = appointmentCaptor.getValue();

        assertThat(savedAppointment.getStatus()).isEqualTo(AppointmentStatus.REJECTED);

        assertThat(savedAppointment.getId()).isEqualTo(appointmentId);
    }
    @Test
    void whenProcessAppointment_withNonExistentAppointmentId_shouldThrowException() {
        Long nonExistentId = 999L;

        when(appointmentRepository.findById(nonExistentId)).thenReturn(Optional.empty());

        AppointmentNotFoundException thrownException = assertThrows(
                AppointmentNotFoundException.class,
                () -> {
                    appointmentService.processAppointment(nonExistentId);
                }
        );
        assertThat(thrownException.getMessage()).isEqualTo("Zahtev za termin sa ID: " + nonExistentId + " nije pronađen.");

        verify(appointmentRepository, never()).save(any(Appointment.class));
    }
    @Test
    void whenGetAppointmentsByStatus_withConfirmedStatus_shouldReturnOnlyConfirmed() {
        AppointmentStatus statusToFilter = AppointmentStatus.CONFIRMED;

        Appointment confirmedAppointment1 = new Appointment(1L, patient, doctor,
                LocalDateTime.now().plusDays(1), statusToFilter);

        Appointment confirmedAppointment2 = new Appointment(2L, patient, doctor,
                LocalDateTime.now().plusDays(2), statusToFilter);

        List<Appointment> confirmedEntityList = List.of(confirmedAppointment1, confirmedAppointment2);

        when(appointmentRepository.findByStatus(statusToFilter)).thenReturn(confirmedEntityList);

        List<AppointmentResponseDTO> resultDtoList = appointmentService.getAppointmentsByStatus(statusToFilter);

        assertThat(resultDtoList).isNotNull();
        assertThat(resultDtoList).hasSize(2);

        assertThat(resultDtoList).allMatch(dto -> dto.getStatus() == statusToFilter);

        AppointmentResponseDTO firstDto = resultDtoList.get(0);
        assertThat(firstDto.getId()).isEqualTo(confirmedAppointment1.getId());
        assertThat(firstDto.getPatient().getFirstName()).isEqualTo(patient.getFirstName());

        verify(appointmentRepository, times(1)).findByStatus(statusToFilter);
        verify(appointmentRepository, never()).findAll();
    }
    @Test
    void whenGetAllAppointments_shouldReturnDtoList() {
        Appointment appointment1 = new Appointment(1L, patient, doctor,
                LocalDateTime.now().plusHours(1), AppointmentStatus.PENDING);

        Doctor anotherDoctor = new Doctor(2L, "Jovana", "Jovanović", "Kardiolog");
        Appointment appointment2 = new Appointment(2L, patient, anotherDoctor,
                LocalDateTime.now().plusDays(1), AppointmentStatus.CONFIRMED);

        List<Appointment> appointmentEntityList = List.of(appointment1, appointment2);

        when(appointmentRepository.findAll()).thenReturn(appointmentEntityList);

        List<AppointmentResponseDTO> resultDtoList = appointmentService.getAllAppointments();

        assertThat(resultDtoList).isNotNull();
        assertThat(resultDtoList).isNotEmpty();
        assertThat(resultDtoList).hasSize(2);

        AppointmentResponseDTO firstDto = resultDtoList.get(0);
        assertThat(firstDto.getId()).isEqualTo(appointment1.getId());
        assertThat(firstDto.getStatus()).isEqualTo(appointment1.getStatus());
        assertThat(firstDto.getAppointmentTime()).isEqualTo(appointment1.getAppointmentTime());

        assertThat(firstDto.getPatient()).isNotNull();
        assertThat(firstDto.getPatient().getId()).isEqualTo(patient.getId());
        assertThat(firstDto.getPatient().getFirstName()).isEqualTo(patient.getFirstName());
        assertThat(firstDto.getPatient().getLastName()).isEqualTo(patient.getLastName());

        assertThat(firstDto.getDoctor()).isNotNull();
        assertThat(firstDto.getDoctor().getId()).isEqualTo(doctor.getId());
        assertThat(firstDto.getDoctor().getFirstName()).isEqualTo(doctor.getFirstName());
        assertThat(firstDto.getDoctor().getSpecialization()).isEqualTo(doctor.getSpecialization());

        AppointmentResponseDTO secondDto = resultDtoList.get(1);
        assertThat(secondDto.getId()).isEqualTo(appointment2.getId());
        assertThat(secondDto.getStatus()).isEqualTo(AppointmentStatus.CONFIRMED);
        assertThat(secondDto.getDoctor().getFirstName()).isEqualTo("Jovana");
    }
    @Test
    void whenProcessAppointment_andTerminIsFree_shouldConfirmAppointment() {
        Long appointmentId = 1L;

        Appointment pendingAppointment = new Appointment(
                appointmentId,
                patient,
                doctor,
                LocalDateTime.now().plusDays(1),
                AppointmentStatus.PENDING
        );

        when(appointmentRepository.findById(appointmentId)).thenReturn(Optional.of(pendingAppointment));

        when(appointmentRepository.findByDoctorAndAppointmentTimeAndStatus(
                doctor,
                pendingAppointment.getAppointmentTime(),
                AppointmentStatus.CONFIRMED
        )).thenReturn(Optional.empty());

        appointmentService.processAppointment(appointmentId);

        ArgumentCaptor<Appointment> appointmentCaptor = ArgumentCaptor.forClass(Appointment.class);

        verify(appointmentRepository, times(1)).save(appointmentCaptor.capture());

        Appointment savedAppointment = appointmentCaptor.getValue();

        assertThat(savedAppointment.getStatus()).isEqualTo(AppointmentStatus.CONFIRMED);

        assertThat(savedAppointment.getId()).isEqualTo(appointmentId);
        assertThat(savedAppointment.getDoctor()).isEqualTo(doctor);
    }
    @Test
    void whenCreateAppointment_andPatientDoesNotExist_shouldCreateNewPatient() {

        when(doctorRepository.findByFirstNameAndLastName(requestDTO.getDoctorFirstName(), requestDTO.getDoctorLastName()))
                .thenReturn(Optional.of(doctor));

        when(patientRepository.findByJmbg(requestDTO.getPatientJmbg()))
                .thenReturn(Optional.empty());

        Patient newPatient = new Patient(2L,
                requestDTO.getPatientFirstName(),
                requestDTO.getPatientLastName(),
                requestDTO.getPatientJmbg()
        );
        when(patientRepository.save(any(Patient.class))).thenReturn(newPatient);

        when(appointmentRepository.save(any(Appointment.class))).thenAnswer(invocation -> {
            Appointment app = invocation.getArgument(0);
            app.setId(1L);
            return app;
        });

        Appointment result = appointmentService.createAppointmentRequest(requestDTO);

        verify(patientRepository, times(1)).save(any(Patient.class));

        verify(appointmentRepository, times(1)).save(any(Appointment.class));

        verify(producer, times(1)).send(anyLong());

        assertThat(result).isNotNull();
        assertThat(result.getPatient()).isNotNull();
        assertThat(result.getPatient().getId()).isEqualTo(2L);
        assertThat(result.getPatient().getJmbg()).isEqualTo(requestDTO.getPatientJmbg());
        assertThat(result.getStatus()).isEqualTo(AppointmentStatus.PENDING);
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