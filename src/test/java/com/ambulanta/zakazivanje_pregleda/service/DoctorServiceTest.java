package com.ambulanta.zakazivanje_pregleda.service;

import com.ambulanta.zakazivanje_pregleda.dto.DoctorDTO;
import com.ambulanta.zakazivanje_pregleda.model.Doctor;
import com.ambulanta.zakazivanje_pregleda.model.User;
import com.ambulanta.zakazivanje_pregleda.repository.DoctorRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DoctorServiceTest {

    @Mock
    private DoctorRepository doctorRepository;

    @InjectMocks
    private DoctorService doctorService;

    @Test
    void whenFindAllDoctors_shouldReturnCorrectDtoList() {
        Doctor doctor1 = new Doctor();
        doctor1.setId(1L);
        doctor1.setSpecialization("Opšta praksa");

        User user1 = new User();
        user1.setFirstName("Petar");
        user1.setLastName("Petrović");
        doctor1.setUser(user1);

        Doctor doctor2 = new Doctor();
        doctor2.setId(2L);
        doctor2.setSpecialization("Kardiolog");

        User user2 = new User();
        user2.setFirstName("Jovana");
        user2.setLastName("Jovanović");
        doctor2.setUser(user2);

        List<Doctor> doctorEntities = List.of(doctor1, doctor2);

        when(doctorRepository.findAll()).thenReturn(doctorEntities);

        List<DoctorDTO> result = doctorService.findAllDoctors();

        assertThat(result).isNotNull();
        assertThat(result).hasSize(2);

        DoctorDTO firstDoctorDto = result.get(0);
        assertThat(firstDoctorDto.getId()).isEqualTo(1L);
        assertThat(firstDoctorDto.getFirstName()).isEqualTo("Petar");
        assertThat(firstDoctorDto.getLastName()).isEqualTo("Petrović");
        assertThat(firstDoctorDto.getSpecialization()).isEqualTo("Opšta praksa");
    }

    @Test
    void whenFindAllDoctors_andNoDoctorsExist_shouldReturnEmptyList() {
        when(doctorRepository.findAll()).thenReturn(List.of());

        List<DoctorDTO> result = doctorService.findAllDoctors();

        assertThat(result).isNotNull();
        assertThat(result).isEmpty();
    }
}