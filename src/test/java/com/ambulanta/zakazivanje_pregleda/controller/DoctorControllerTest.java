package com.ambulanta.zakazivanje_pregleda.controller;

import com.ambulanta.zakazivanje_pregleda.config.SecurityConfig;
import com.ambulanta.zakazivanje_pregleda.dto.DoctorDTO;
import com.ambulanta.zakazivanje_pregleda.security.JwtAuthFilter;
import com.ambulanta.zakazivanje_pregleda.security.JwtService;
import com.ambulanta.zakazivanje_pregleda.service.DoctorService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = DoctorController.class)
@Import({SecurityConfig.class, JwtAuthFilter.class})
class DoctorControllerTest {

    @Autowired
    private MockMvc mockMvc;

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
}