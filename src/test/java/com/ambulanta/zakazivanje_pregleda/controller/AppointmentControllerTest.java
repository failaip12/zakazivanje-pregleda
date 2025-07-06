package com.ambulanta.zakazivanje_pregleda.controller;

import com.ambulanta.zakazivanje_pregleda.config.SecurityConfig;
import com.ambulanta.zakazivanje_pregleda.dto.*;
import com.ambulanta.zakazivanje_pregleda.exception.DoctorNotFoundException;
import com.ambulanta.zakazivanje_pregleda.model.Appointment;
import com.ambulanta.zakazivanje_pregleda.model.AppointmentStatus;
import com.ambulanta.zakazivanje_pregleda.security.JwtService;
import com.ambulanta.zakazivanje_pregleda.service.AppointmentService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.List;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = AppointmentController.class)
@Import({SecurityConfig.class})
class AppointmentControllerTest {
    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private AppointmentService appointmentService;

    @MockitoBean
    private JwtService jwtService;

    @MockitoBean
    private UserDetailsService userDetailsService;
    @Test
    @WithMockUser(username = "admin", roles = "ADMIN")
    void whenGetAppointments_asAdmin_shouldCallCorrectServiceMethod() throws Exception {
        PatientDTO patientDTO = new PatientDTO();
        patientDTO.setId(1L);
        patientDTO.setFirstName("Marko");
        patientDTO.setLastName("Marković");

        DoctorDTO doctorDTO = new DoctorDTO();
        doctorDTO.setId(1L);
        doctorDTO.setFirstName("Petar");
        doctorDTO.setLastName("Petrović");

        DoctorDTO doctorDTO2 = new DoctorDTO();
        doctorDTO.setId(2L);
        doctorDTO.setFirstName("Milan");
        doctorDTO.setLastName("Milovanovic");

        AppointmentResponseDTO appointment1 = new AppointmentResponseDTO();
        appointment1.setId(1L);
        appointment1.setStatus(AppointmentStatus.PENDING);
        appointment1.setPatient(patientDTO);
        appointment1.setDoctor(doctorDTO);

        AppointmentResponseDTO appointment2 = new AppointmentResponseDTO();
        appointment2.setId(2L);
        appointment2.setStatus(AppointmentStatus.CONFIRMED);
        appointment2.setPatient(patientDTO);
        appointment2.setDoctor(doctorDTO);

        AppointmentResponseDTO appointment3 = new AppointmentResponseDTO();
        appointment3.setId(3L);
        appointment3.setStatus(AppointmentStatus.PENDING);
        appointment3.setPatient(patientDTO);
        appointment3.setDoctor(doctorDTO2);

        AppointmentResponseDTO appointment4 = new AppointmentResponseDTO();
        appointment4.setId(4L);
        appointment4.setStatus(AppointmentStatus.CONFIRMED);
        appointment4.setPatient(patientDTO);
        appointment4.setDoctor(doctorDTO2);

        List<AppointmentResponseDTO> allAppointments = List.of(appointment1, appointment2, appointment3, appointment4);
        given(appointmentService.getAppointmentsForUser("admin", null)).willReturn(allAppointments);

        mockMvc.perform(get("/api/appointments"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$", hasSize(4)));

        verify(appointmentService).getAppointmentsForUser("admin", null);
    }
    @Test
    @WithMockUser(username = "doctor.petrovic", roles = "DOCTOR")
    void whenGetAppointments_asDoctor_withoutFilter_shouldReturnTheirAppointments() throws Exception {
        PatientDTO patientDTO = new PatientDTO();
        patientDTO.setId(1L);
        patientDTO.setFirstName("Marko");
        patientDTO.setLastName("Marković");

        DoctorDTO doctorDTO = new DoctorDTO();
        doctorDTO.setId(1L);
        doctorDTO.setFirstName("Petar");
        doctorDTO.setLastName("Petrović");

        DoctorDTO doctorDTO2 = new DoctorDTO();
        doctorDTO.setId(2L);
        doctorDTO.setFirstName("Milan");
        doctorDTO.setLastName("Milovanovic");

        AppointmentResponseDTO appointment1 = new AppointmentResponseDTO();
        appointment1.setId(1L);
        appointment1.setStatus(AppointmentStatus.PENDING);
        appointment1.setPatient(patientDTO);
        appointment1.setDoctor(doctorDTO);

        AppointmentResponseDTO appointment2 = new AppointmentResponseDTO();
        appointment2.setId(2L);
        appointment2.setStatus(AppointmentStatus.CONFIRMED);
        appointment2.setPatient(patientDTO);
        appointment2.setDoctor(doctorDTO);

        AppointmentResponseDTO appointment3 = new AppointmentResponseDTO();
        appointment3.setId(3L);
        appointment3.setStatus(AppointmentStatus.PENDING);
        appointment3.setPatient(patientDTO);
        appointment3.setDoctor(doctorDTO2);

        AppointmentResponseDTO appointment4 = new AppointmentResponseDTO();
        appointment4.setId(4L);
        appointment4.setStatus(AppointmentStatus.CONFIRMED);
        appointment4.setPatient(patientDTO);
        appointment4.setDoctor(doctorDTO2);

        List<AppointmentResponseDTO> doctorAppointments = List.of(appointment1, appointment2);

        given(appointmentService.getAppointmentsForUser(eq("doctor.petrovic"), isNull(AppointmentStatus.class)))
                .willReturn(doctorAppointments);

        mockMvc.perform(get("/api/appointments"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].id", is(1)));

        verify(appointmentService).getAppointmentsForUser("doctor.petrovic", null);
    }

    @Test
    @WithMockUser(username = "patient.markovic", roles = "PATIENT")
    void whenGetAppointments_asPatient_shouldReturnTheirOwnAppointments() throws Exception {
        PatientDTO patientDTO = new PatientDTO();
        patientDTO.setFirstName("Marko");

        AppointmentResponseDTO patientAppointment = new AppointmentResponseDTO();
        patientAppointment.setId(10L);
        patientAppointment.setStatus(AppointmentStatus.CONFIRMED);
        patientAppointment.setPatient(patientDTO);

        List<AppointmentResponseDTO> patientAppointments = List.of(patientAppointment);

        given(appointmentService.getAppointmentsForUser(eq("patient.markovic"), isNull(AppointmentStatus.class)))
                .willReturn(patientAppointments);

        mockMvc.perform(get("/api/appointments"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].id", is(10)));

        verify(appointmentService).getAppointmentsForUser("patient.markovic", null);
    }

    @Test
    @WithMockUser(username = "doctor.petrovic", roles = "DOCTOR")
    void whenGetAppointments_asDoctor_withStatusFilter_shouldReturnFilteredAppointments() throws Exception {
        AppointmentResponseDTO confirmedAppointment = new AppointmentResponseDTO();
        confirmedAppointment.setId(1L);
        confirmedAppointment.setStatus(AppointmentStatus.CONFIRMED);

        List<AppointmentResponseDTO> filteredAppointments = List.of(confirmedAppointment);

        given(appointmentService.getAppointmentsForUser(eq("doctor.petrovic"), eq(AppointmentStatus.CONFIRMED)))
                .willReturn(filteredAppointments);

        mockMvc.perform(get("/api/appointments?status=CONFIRMED"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].status", is("CONFIRMED")));

        verify(appointmentService).getAppointmentsForUser("doctor.petrovic", AppointmentStatus.CONFIRMED);

        verify(appointmentService, never()).getAllAppointments();
    }
    @Test
    void whenGetAppointments_withoutAuthentication_shouldReturnUnauthorized() throws Exception {
        mockMvc.perform(get("/api/appointments"))
                .andExpect(status().isUnauthorized());
    }
    @Test
    @WithMockUser(roles = "DOCTOR")
    void whenGetAllAppointments_withInvalidStatusFilter_shouldReturnBadRequest() throws Exception {
        mockMvc.perform(get("/api/appointments?status=INVALID_STATUS")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser(username = "MarMar", roles = "PATIENT")
    void whenPostAppointment_withValidRequest_asPatient_shouldReturnAccepted() throws Exception {
        AppointmentRequestDTO requestDTO = new AppointmentRequestDTO();
        requestDTO.setDoctorId(1L);
        requestDTO.setAppointmentTime(LocalDateTime.now().plusDays(10));

        Appointment createdAppointment = new Appointment();
        createdAppointment.setId(1L);
        createdAppointment.setStatus(AppointmentStatus.PENDING);

        given(appointmentService.createAppointmentRequest(any(AppointmentRequestDTO.class), anyString()))
                .willReturn(createdAppointment);

        mockMvc.perform(post("/api/appointments")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDTO)))
                .andExpect(status().isAccepted())
                .andExpect(jsonPath("$.id", is(1)))
                .andExpect(jsonPath("$.status", is("PENDING")));
    }

    @Test
    @WithMockUser(roles = "DOCTOR")
    void whenPostAppointment_asDoctor_shouldReturnForbidden() throws Exception {
        AppointmentRequestDTO requestDTO = new AppointmentRequestDTO();
        requestDTO.setDoctorId(1L);
        requestDTO.setAppointmentTime(LocalDateTime.now().plusDays(10));

        mockMvc.perform(post("/api/appointments")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDTO)))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(username = "MarMar", roles = "PATIENT")
    void whenPostAppointment_andDoctorNotFound_shouldReturnNotFound() throws Exception {
        AppointmentRequestDTO requestDTO = new AppointmentRequestDTO();
        requestDTO.setDoctorId(99L);
        requestDTO.setAppointmentTime(LocalDateTime.now().plusDays(5));

        String errorMessage = "Lekar sa ID: 99 nije pronađen.";

        given(appointmentService.createAppointmentRequest(any(AppointmentRequestDTO.class), anyString()))
                .willThrow(new DoctorNotFoundException(errorMessage));

        mockMvc.perform(post("/api/appointments")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDTO)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message", is(errorMessage)))
                .andExpect(jsonPath("$.status", is(404)));
    }

    @Test
    @WithMockUser(roles = "PATIENT")
    void whenPostAppointment_withMalformedJson_shouldReturnBadRequest() throws Exception {
        String malformedJson = "{\"doctorId\": 1 \"appointmentTime\": \"2025-12-01T10:00:00\"}";

        mockMvc.perform(post("/api/appointments")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(malformedJson))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser(roles = "PATIENT")
    void whenPostAppointment_withInvalidBody_shouldReturnBadRequest() throws Exception {
        AppointmentRequestDTO invalidRequestDTO = new AppointmentRequestDTO();

        mockMvc.perform(post("/api/appointments")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidRequestDTO)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors.doctorId", is("Id lekara ne sme biti prazan.")))
                .andExpect(jsonPath("$.errors.appointmentTime", is("Vreme pregleda mora biti definisano.")))
                .andExpect(jsonPath("$.message", is("Podaci nisu validni.")));
    }
}