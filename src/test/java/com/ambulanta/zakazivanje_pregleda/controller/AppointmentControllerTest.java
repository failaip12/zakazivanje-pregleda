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
    @WithMockUser(roles = "DOCTOR")
    void whenGetAllAppointments_asDoctor_shouldReturnAllAppointments() throws Exception {
        PatientDTO patientDTO = new PatientDTO();
        patientDTO.setId(1L);
        patientDTO.setFirstName("Marko");
        patientDTO.setLastName("Marković");

        DoctorDTO doctorDTO = new DoctorDTO();
        doctorDTO.setId(1L);
        doctorDTO.setFirstName("Petar");
        doctorDTO.setLastName("Petrović");

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

        List<AppointmentResponseDTO> allAppointments = List.of(appointment1, appointment2);

        given(appointmentService.getAllAppointments()).willReturn(allAppointments);

        mockMvc.perform(get("/api/appointments"))
                .andExpect(status().isOk())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].id", is(1)))
                .andExpect(jsonPath("$[0].status", is("PENDING")))
                .andExpect(jsonPath("$[1].id", is(2)))
                .andExpect(jsonPath("$[1].status", is("CONFIRMED")));
    }

    @Test
    @WithMockUser(roles = "PATIENT")
    void whenGetAllAppointments_asPatient_shouldReturnForbidden() throws Exception {
        mockMvc.perform(get("/api/appointments"))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "DOCTOR")
    void whenGetAllAppointments_withStatusFilter_shouldReturnFilteredAppointments() throws Exception {
        AppointmentResponseDTO confirmedAppointment = new AppointmentResponseDTO();
        confirmedAppointment.setId(1L);
        confirmedAppointment.setStatus(AppointmentStatus.CONFIRMED);

        List<AppointmentResponseDTO> filteredAppointments = List.of(confirmedAppointment);

        given(appointmentService.getAppointmentsByStatus(AppointmentStatus.CONFIRMED))
                .willReturn(filteredAppointments);

        mockMvc.perform(get("/api/appointments?status=CONFIRMED"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].status", is("CONFIRMED")));
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
    void whenPostAppointment_withInvalidBody_shouldReturnBadRequest() throws Exception {
        AppointmentRequestDTO invalidRequestDTO = new AppointmentRequestDTO();

        mockMvc.perform(post("/api/appointments")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidRequestDTO)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.doctorId").exists())
                .andExpect(jsonPath("$.appointmentTime").exists());
    }
}