package com.ambulanta.zakazivanje_pregleda.controller;

import com.ambulanta.zakazivanje_pregleda.dto.AppointmentRequestDTO;
import com.ambulanta.zakazivanje_pregleda.dto.AppointmentResponseDTO;
import com.ambulanta.zakazivanje_pregleda.dto.DoctorDTO;
import com.ambulanta.zakazivanje_pregleda.dto.PatientDTO;
import com.ambulanta.zakazivanje_pregleda.exception.DoctorNotFoundException;
import com.ambulanta.zakazivanje_pregleda.model.Appointment;
import com.ambulanta.zakazivanje_pregleda.model.AppointmentStatus;
import com.ambulanta.zakazivanje_pregleda.service.AppointmentService;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.List;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(AppointmentController.class)
class AppointmentControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private AppointmentService appointmentService;

    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
    }

    @Test
    void whenGetAllAppointments_withoutFilter_shouldReturnAllAppointments() throws Exception {
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

        // WHEN & THEN
        mockMvc.perform(get("/api/appointments")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].id", is(1)))
                .andExpect(jsonPath("$[0].status", is("PENDING")))
                .andExpect(jsonPath("$[1].id", is(2)))
                .andExpect(jsonPath("$[1].status", is("CONFIRMED")));
    }
    @Test
    void whenGetAllAppointments_withStatusFilter_shouldReturnFilteredAppointments() throws Exception {
        AppointmentResponseDTO confirmedAppointment = new AppointmentResponseDTO();
        confirmedAppointment.setId(1L);
        confirmedAppointment.setStatus(AppointmentStatus.CONFIRMED);

        List<AppointmentResponseDTO> filteredAppointments = List.of(confirmedAppointment);

        given(appointmentService.getAppointmentsByStatus(AppointmentStatus.CONFIRMED))
                .willReturn(filteredAppointments);

        mockMvc.perform(get("/api/appointments?status=CONFIRMED")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].status", is("CONFIRMED")));

        verify(appointmentService).getAppointmentsByStatus(AppointmentStatus.CONFIRMED);
        verify(appointmentService, never()).getAllAppointments();
    }
    @Test
    void whenGetAllAppointments_withInvalidStatusFilter_shouldReturnBadRequest() throws Exception {
        mockMvc.perform(get("/api/appointments?status=INVALID_STATUS")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }
    @Test
    void whenPostAppointment_andDoctorNotFound_shouldReturnNotFound() throws Exception {
        AppointmentRequestDTO requestDTO = new AppointmentRequestDTO();
        requestDTO.setDoctorFirstName("Nepostojeci");
        requestDTO.setDoctorLastName("Lekar");
        requestDTO.setPatientFirstName("Ana");
        requestDTO.setPatientLastName("Anić");
        requestDTO.setPatientJmbg("1112223334445");
        requestDTO.setAppointmentTime(LocalDateTime.now().plusDays(5));

        String errorMessage = "Lekar sa imenom 'Nepostojeci Lekar' nije pronađen.";

        given(appointmentService.createAppointmentRequest(any(AppointmentRequestDTO.class)))
                .willThrow(new DoctorNotFoundException(errorMessage));

        mockMvc.perform(post("/api/appointments")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDTO)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message", is(errorMessage)))
                .andExpect(jsonPath("$.status", is(404)));
    }
    @Test
    void whenPostAppointment_withValidRequest_shouldReturnAccepted() throws Exception {
        AppointmentRequestDTO requestDTO = new AppointmentRequestDTO();
        requestDTO.setDoctorFirstName("Petar");
        requestDTO.setDoctorLastName("Petrović");
        requestDTO.setPatientFirstName("Marko");
        requestDTO.setPatientLastName("Marković");
        requestDTO.setPatientJmbg("1234567890123");
        requestDTO.setAppointmentTime(LocalDateTime.now().plusDays(10));

        Appointment createdAppointment = new Appointment();
        createdAppointment.setId(1L);
        createdAppointment.setStatus(AppointmentStatus.PENDING);

        when(appointmentService.createAppointmentRequest(any(AppointmentRequestDTO.class)))
                .thenReturn(createdAppointment);

        mockMvc.perform(post("/api/appointments")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDTO)))
                .andExpect(status().isAccepted())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.status").value("PENDING"));
    }

    @Test
    void whenPostAppointment_withInvalidRequest_shouldReturnBadRequest() throws Exception {
        AppointmentRequestDTO invalidRequestDTO = new AppointmentRequestDTO();
        invalidRequestDTO.setDoctorFirstName("");
        invalidRequestDTO.setPatientJmbg("123");

        // WHEN & THEN
        mockMvc.perform(post("/api/appointments")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidRequestDTO)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.doctorFirstName").value("Ime lekara ne sme biti prazno."))
                .andExpect(jsonPath("$.patientJmbg").value("JMBG mora sadržati tačno 13 cifara."));
    }
}