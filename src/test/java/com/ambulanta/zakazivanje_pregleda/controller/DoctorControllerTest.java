package com.ambulanta.zakazivanje_pregleda.controller;

import com.ambulanta.zakazivanje_pregleda.config.SecurityConfig;
import com.ambulanta.zakazivanje_pregleda.dto.AddDoctorRequestDTO;
import com.ambulanta.zakazivanje_pregleda.dto.BookedSlotDTO;
import com.ambulanta.zakazivanje_pregleda.dto.DoctorDTO;
import com.ambulanta.zakazivanje_pregleda.security.JwtAuthFilter;
import com.ambulanta.zakazivanje_pregleda.security.JwtService;
import com.ambulanta.zakazivanje_pregleda.service.DoctorService;
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
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = DoctorController.class)
@Import({SecurityConfig.class, JwtAuthFilter.class})
class DoctorControllerTest {
    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private DoctorService doctorService;

    @MockitoBean
    private JwtService jwtService;

    @MockitoBean
    private UserDetailsService userDetailsService;

    @Test
    @WithMockUser(roles = "PATIENT")
    void whenGetAllDoctors_asPatient_shouldReturnDoctorList() throws Exception {
        DoctorDTO doctor1 = new DoctorDTO(1L, "Petar", "Petrović", "Opšta praksa");
        DoctorDTO doctor2 = new DoctorDTO(2L, "Jovana", "Jovanović", "Kardiolog");

        List<DoctorDTO> doctors = List.of(doctor1, doctor2);

        given(doctorService.findAllDoctors()).willReturn(doctors);

        mockMvc.perform(get("/api/doctors")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].firstName", is("Petar")))
                .andExpect(jsonPath("$[1].specialization", is("Kardiolog")));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void whenGetAllDoctors_asAdmin_shouldReturnDoctorList() throws Exception {
        DoctorDTO doctor1 = new DoctorDTO(1L, "Petar", "Petrović", "Opšta praksa");
        List<DoctorDTO> doctors = List.of(doctor1);
        given(doctorService.findAllDoctors()).willReturn(doctors);

        mockMvc.perform(get("/api/doctors"))
                .andExpect(status().isOk());
    }

    @Test
    void whenGetAllDoctors_withoutAuthentication_shouldReturnUnauthorized() throws Exception {
        DoctorDTO doctor1 = new DoctorDTO(1L, "Petar", "Petrović", "Opšta praksa");
        given(doctorService.findAllDoctors()).willReturn(List.of(doctor1));

        mockMvc.perform(get("/api/doctors"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void whenAddNewDoctor_asAdmin_withValidData_shouldReturnCreated() throws Exception {
        AddDoctorRequestDTO request = new AddDoctorRequestDTO();
        request.setFirstName("Novi");
        request.setLastName("Doktor");
        request.setUsername("9876543210987");
        request.setPassword("sigurnaLozinka");
        request.setSpecialization("Hirurg");

        DoctorDTO createdDoctorDto = new DoctorDTO(5L, "Novi", "Doktor", "Hirurg");
        given(doctorService.addDoctor(any(AddDoctorRequestDTO.class))).willReturn(createdDoctorDto);

        mockMvc.perform(post("/api/doctors")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id", is(5)))
                .andExpect(jsonPath("$.firstName", is("Novi")));
    }

    @Test
    @WithMockUser(roles = "PATIENT")
    void whenAddNewDoctor_asPatient_shouldReturnForbidden() throws Exception {
        AddDoctorRequestDTO request = new AddDoctorRequestDTO();
        request.setFirstName("Novi");
        request.setLastName("Doktor");
        request.setUsername("9876543210987");
        request.setPassword("sigurnaLozinka");
        request.setSpecialization("Hirurg");

        mockMvc.perform(post("/api/doctors")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void whenAddNewDoctor_withInvalidData_shouldReturnBadRequest() throws Exception {
        AddDoctorRequestDTO request = new AddDoctorRequestDTO();
        request.setFirstName("Novi");
        request.setLastName("Doktor");
        request.setUsername("123");
        request.setPassword("lozinka");

        mockMvc.perform(post("/api/doctors")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message", is("Podaci nisu validni.")))
                .andExpect(jsonPath("$.status", is(400)))
                .andExpect(jsonPath("$.errors.specialization", is("Specijalizacija ne sme biti prazna.")));
    }

    @Test
    @WithMockUser(roles = "PATIENT")
    void whenGetDoctorAppointments_asPatient_shouldReturnBookedSlots() throws Exception {
        Long doctorId = 1L;
        LocalDateTime time1 = LocalDateTime.of(2025, 1, 1, 10, 0);
        LocalDateTime time2 = LocalDateTime.of(2025, 1, 1, 10, 15);
        List<BookedSlotDTO> bookedSlots = List.of(
                new BookedSlotDTO(time1),
                new BookedSlotDTO(time2)
        );

        given(doctorService.getDoctorAppointments(doctorId)).willReturn(bookedSlots);

        mockMvc.perform(get("/api/doctors/{doctorId}/appointments", doctorId)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].appointmentTime", is(time1.toString() + ":00")))
                .andExpect(jsonPath("$[1].appointmentTime", is(time2.toString() + ":00")));
    }
}
