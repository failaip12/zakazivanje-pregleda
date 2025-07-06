package com.ambulanta.zakazivanje_pregleda.service;

import com.ambulanta.zakazivanje_pregleda.dto.AppointmentRequestDTO;
import com.ambulanta.zakazivanje_pregleda.dto.AppointmentResponseDTO;
import com.ambulanta.zakazivanje_pregleda.exception.AppointmentNotFoundException;
import com.ambulanta.zakazivanje_pregleda.exception.DoctorNotFoundException;
import com.ambulanta.zakazivanje_pregleda.exception.InvalidAppointmentTimeException;
import com.ambulanta.zakazivanje_pregleda.exception.UserNotFoundException;
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
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AppointmentServiceTest {
    @Mock
    private AppointmentRepository appointmentRepository;
    @Mock
    private UserRepository  userRepository;
    @Mock
    private DoctorRepository doctorRepository;
    @Mock
    private AppointmentRequestProducer producer;

    @InjectMocks
    private AppointmentService appointmentService;

    private AppointmentRequestDTO requestDTO;
    private User patientUser;
    private User doctorUser;
    private Doctor doctor;
    private Patient patient;

    @BeforeEach
    void setUp() {
        requestDTO = new AppointmentRequestDTO();
        requestDTO.setDoctorId(1L);
        requestDTO.setAppointmentTime(LocalDateTime.now().plusDays(1).with(LocalTime.of(11, 45)));

        patient = new Patient();
        patient.setId(1L);
        patient.setJmbg("1111111111111");

        patientUser = new User();
        patientUser.setId(1L);
        patientUser.setUsername(patient.getJmbg());
        patientUser.setFirstName("Marko");
        patientUser.setLastName("Marković");
        patientUser.setRole(Role.ROLE_PATIENT);
        patientUser.setPatient(patient);
        patient.setUser(patientUser);

        doctor = new Doctor();
        doctor.setId(1L);
        doctor.setSpecialization("Opšta praksa");

        doctorUser = new User();
        doctorUser.setId(2L);
        doctorUser.setUsername("2222222222222");
        doctorUser.setFirstName("Petar");
        doctorUser.setLastName("Petrović");
        doctorUser.setRole(Role.ROLE_DOCTOR);
        doctorUser.setDoctor(doctor);
        doctor.setUser(doctorUser);
    }

    @Test
    void whenCreateAppointment_withValidData_shouldReturnPendingAppointment() {
        when(doctorRepository.findById(1L)).thenReturn(Optional.of(doctor));
        when(userRepository.findByUsername("1111111111111")).thenReturn(Optional.of(patientUser));

        when(appointmentRepository.save(any(Appointment.class))).thenAnswer(invocation -> {
            Appointment app = invocation.getArgument(0);
            app.setId(1L);
            return app;
        });

        Appointment result = appointmentService.createAppointmentRequest(requestDTO, "1111111111111");

        assertThat(result).isNotNull();
        assertThat(result.getStatus()).isEqualTo(AppointmentStatus.PENDING);
        assertThat(result.getDoctor().getId()).isEqualTo(1L);
        assertThat(result.getPatient().getJmbg()).isEqualTo("1111111111111");
        assertThat(result.getId()).isEqualTo(1L);

        verify(producer, times(1)).send(1L);
    }

    @Test
    void whenGetAppointmentsForUser_withNonExistentUsername_shouldThrowException() {
        String nonExistentUsername = "nepostojeci";
        when(userRepository.findByUsername(nonExistentUsername)).thenReturn(Optional.empty());
        assertThrows(UserNotFoundException.class, () -> {
            appointmentService.getAppointmentsForUser(nonExistentUsername, null);
        });
    }

    @Test
    void whenGetAppointmentsForUser_forPatientUserWithoutPatientEntity_shouldThrowIllegalStateException() {
        String username = "patient.bez.entiteta";
        User patientWithoutEntity = new User();
        patientWithoutEntity.setRole(Role.ROLE_PATIENT);
        patientWithoutEntity.setPatient(null);

        when(userRepository.findByUsername(username)).thenReturn(Optional.of(patientWithoutEntity));

        IllegalStateException thrown = assertThrows(IllegalStateException.class, () -> {
            appointmentService.getAppointmentsForUser(username, null);
        });

        assertThat(thrown.getMessage()).isEqualTo("Korisnik sa ulogom pacijenta nema povezane podatke o pacijentu.");
    }

    @Test
    void whenGetAppointmentsForUser_forDoctorUserWithoutDoctorEntity_shouldThrowIllegalStateException() {
        String username = "doctor.bez.entiteta";
        User doctorWithoutEntity = new User();
        doctorWithoutEntity.setRole(Role.ROLE_DOCTOR);
        doctorWithoutEntity.setDoctor(null);

        when(userRepository.findByUsername(username)).thenReturn(Optional.of(doctorWithoutEntity));

        IllegalStateException thrown = assertThrows(IllegalStateException.class, () -> {
            appointmentService.getAppointmentsForUser(username, null);
        });

        assertThat(thrown.getMessage()).isEqualTo("Korisnik sa ulogom doktora nema povezane podatke o doktoru.");
    }

    @Test
    void whenGetAppointmentsForUser_asAdminWithStatus_shouldCallFindByStatus() {
        String adminUsername = "admin";
        User adminUser = new User();
        adminUser.setRole(Role.ROLE_ADMIN);

        when(userRepository.findByUsername(adminUsername)).thenReturn(Optional.of(adminUser));

        appointmentService.getAppointmentsForUser(adminUsername, AppointmentStatus.CONFIRMED);

        verify(appointmentRepository).findByStatus(AppointmentStatus.CONFIRMED);
        verify(appointmentRepository, never()).findAll();
    }
    @Test
    void whenCreateAppointment_withTimeOutsideWorkingHours_shouldThrowInvalidAppointmentTimeException() {
        requestDTO.setAppointmentTime(LocalDateTime.of(2025, 1, 1, 8, 0));

        InvalidAppointmentTimeException thrown = assertThrows(
                InvalidAppointmentTimeException.class,
                () -> appointmentService.createAppointmentRequest(requestDTO, "1111111111111")
        );
        assertThat(thrown.getMessage()).contains("Zakazivanje je moguće samo u toku radnog vremena");
    }

    @Test
    void whenCreateAppointment_withTimeAtEndOfWorkingHours_shouldThrowInvalidAppointmentTimeException() {
        requestDTO.setAppointmentTime(LocalDateTime.of(2025, 1, 1, 17, 0));

        InvalidAppointmentTimeException thrown = assertThrows(
                InvalidAppointmentTimeException.class,
                () -> appointmentService.createAppointmentRequest(requestDTO, "1111111111111")
        );
        assertThat(thrown.getMessage()).contains("Zakazivanje je moguće samo u toku radnog vremena");
    }

    @Test
    void whenCreateAppointment_withInvalidMinuteInterval_shouldThrowInvalidAppointmentTimeException() {
        requestDTO.setAppointmentTime(LocalDateTime.of(2025, 1, 1, 9, 5));

        InvalidAppointmentTimeException thrown = assertThrows(
                InvalidAppointmentTimeException.class,
                () -> appointmentService.createAppointmentRequest(requestDTO, "1111111111111")
        );
        assertThat(thrown.getMessage()).contains("Zakazivanje je moguće samo u intervalima od 15 minuta");
    }

    @Test
    void whenCreateAppointment_withSecondsSet_shouldThrowInvalidAppointmentTimeException() {
        requestDTO.setAppointmentTime(LocalDateTime.of(2025, 1, 1, 9, 15, 30));

        InvalidAppointmentTimeException thrown = assertThrows(
                InvalidAppointmentTimeException.class,
                () -> appointmentService.createAppointmentRequest(requestDTO, "1111111111111")
        );
        assertThat(thrown.getMessage()).contains("ne sme sadržati sekunde");
    }

    @Test
    void whenCreateAppointment_withValidTime_shouldProceedWithoutException() {
        requestDTO.setAppointmentTime(LocalDateTime.of(2025, 1, 1, 16, 45));

        when(doctorRepository.findById(anyLong())).thenReturn(Optional.of(doctor));
        when(userRepository.findByUsername(anyString())).thenReturn(Optional.of(patientUser));
        when(appointmentRepository.save(any(Appointment.class))).thenReturn(new Appointment());

        assertDoesNotThrow(() -> {
            appointmentService.createAppointmentRequest(requestDTO, "1111111111111");
        });

        verify(appointmentRepository).save(any(Appointment.class));
    }
    @Test
    void whenProcessAppointment_andNoConflictingAppointmentsExist_shouldConfirmAppointment() {
        LocalDateTime appointmentTime = LocalDateTime.of(2025, 5, 20, 10, 0);
        Appointment appointmentToProcess = new Appointment(1L, patient, doctor, appointmentTime, AppointmentStatus.PENDING);

        when(appointmentRepository.findById(1L)).thenReturn(Optional.of(appointmentToProcess));

        when(appointmentRepository.findConflictingAppointments(any(), any(), any(), any())).thenReturn(List.of());

        appointmentService.processAppointment(1L);

        ArgumentCaptor<Appointment> captor = ArgumentCaptor.forClass(Appointment.class);
        verify(appointmentRepository).save(captor.capture());
        assertThat(captor.getValue().getStatus()).isEqualTo(AppointmentStatus.CONFIRMED);
    }

    @Test
    void whenProcessAppointment_andConflictingAppointmentExists_shouldRejectAppointment() {
        LocalDateTime newAppointmentTime = LocalDateTime.of(2025, 5, 20, 10, 15);
        Appointment appointmentToProcess = new Appointment(1L, patient, doctor, newAppointmentTime, AppointmentStatus.PENDING);

        Appointment existingAppointment = new Appointment(2L, new Patient(), doctor, LocalDateTime.of(2025, 5, 20, 10, 0), AppointmentStatus.CONFIRMED);

        when(appointmentRepository.findById(1L)).thenReturn(Optional.of(appointmentToProcess));

        when(appointmentRepository.findConflictingAppointments(any(), any(), any(), any())).thenReturn(List.of(existingAppointment));

        appointmentService.processAppointment(1L);

        ArgumentCaptor<Appointment> captor = ArgumentCaptor.forClass(Appointment.class);
        verify(appointmentRepository).save(captor.capture());
        assertThat(captor.getValue().getStatus()).isEqualTo(AppointmentStatus.REJECTED);
    }

    @Test
    void whenProcessAppointment_withSameStartTime_shouldRejectAppointment() {
        LocalDateTime appointmentTime = LocalDateTime.of(2025, 5, 20, 11, 0);
        Appointment appointmentToProcess = new Appointment(1L, patient, doctor, appointmentTime, AppointmentStatus.PENDING);
        Appointment existingAppointment = new Appointment(2L, new Patient(), doctor, appointmentTime, AppointmentStatus.CONFIRMED);

        when(appointmentRepository.findById(1L)).thenReturn(Optional.of(appointmentToProcess));
        when(appointmentRepository.findConflictingAppointments(any(), any(), any(), any())).thenReturn(List.of(existingAppointment));

        appointmentService.processAppointment(1L);

        ArgumentCaptor<Appointment> captor = ArgumentCaptor.forClass(Appointment.class);
        verify(appointmentRepository).save(captor.capture());
        assertThat(captor.getValue().getStatus()).isEqualTo(AppointmentStatus.REJECTED);
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
    void whenGetAppointmentsByStatus_shouldReturnFilteredDtoList() {
        Appointment appointment1 = new Appointment(1L, patient, doctor, LocalDateTime.now(), AppointmentStatus.CONFIRMED);
        appointment1.getPatient().setUser(patientUser);
        appointment1.getDoctor().setUser(doctorUser);

        List<Appointment> entityList = List.of(appointment1);
        when(appointmentRepository.findByStatus(AppointmentStatus.CONFIRMED)).thenReturn(entityList);

        List<AppointmentResponseDTO> dtoList = appointmentService.getAppointmentsByStatus(AppointmentStatus.CONFIRMED);

        assertThat(dtoList).hasSize(1);
        assertThat(dtoList).allMatch(dto -> dto.getStatus() == AppointmentStatus.CONFIRMED);
        assertThat(dtoList.get(0).getPatient().getLastName()).isEqualTo("Marković");
    }
    @Test
    void whenGetAllAppointments_shouldReturnDtoList() {
        Appointment appointment1 = new Appointment(1L, patient, doctor, LocalDateTime.now(), AppointmentStatus.PENDING);
        appointment1.getPatient().setUser(patientUser);
        appointment1.getDoctor().setUser(doctorUser);

        List<Appointment> entityList = List.of(appointment1);
        when(appointmentRepository.findAll()).thenReturn(entityList);

        List<AppointmentResponseDTO> dtoList = appointmentService.getAllAppointments();

        assertThat(dtoList).hasSize(1);
        AppointmentResponseDTO dto = dtoList.get(0);
        assertThat(dto.getId()).isEqualTo(1L);
        assertThat(dto.getPatient().getFirstName()).isEqualTo("Marko");
        assertThat(dto.getDoctor().getFirstName()).isEqualTo("Petar");
        assertThat(dto.getDoctor().getSpecialization()).isEqualTo("Opšta praksa");
    }
    @Test
    void whenCreateAppointment_withNonExistentDoctor_shouldThrowDoctorNotFoundException() {
        when(doctorRepository.findById(anyLong())).thenReturn(Optional.empty());

        assertThrows(DoctorNotFoundException.class, () -> {
            appointmentService.createAppointmentRequest(requestDTO, "1111111111111");
        });

        verify(userRepository, never()).findByUsername(anyString());
        verify(appointmentRepository, never()).save(any());
        verify(producer, never()).send(anyLong());
    }
}