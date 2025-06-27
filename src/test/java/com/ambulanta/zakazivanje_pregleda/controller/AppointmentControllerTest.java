package com.ambulanta.zakazivanje_pregleda.controller;

import com.ambulanta.zakazivanje_pregleda.dto.AppointmentRequestDTO;
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

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
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