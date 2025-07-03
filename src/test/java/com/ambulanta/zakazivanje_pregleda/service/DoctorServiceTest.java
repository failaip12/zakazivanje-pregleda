package com.ambulanta.zakazivanje_pregleda.service;

import com.ambulanta.zakazivanje_pregleda.dto.AddDoctorRequestDTO;
import com.ambulanta.zakazivanje_pregleda.dto.DoctorDTO;
import com.ambulanta.zakazivanje_pregleda.model.Doctor;
import com.ambulanta.zakazivanje_pregleda.model.Role;
import com.ambulanta.zakazivanje_pregleda.model.User;
import com.ambulanta.zakazivanje_pregleda.repository.DoctorRepository;
import com.ambulanta.zakazivanje_pregleda.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DoctorServiceTest {

    @Mock
    private DoctorRepository doctorRepository;
    @Mock
    private UserRepository userRepository;
    @Mock
    private PasswordEncoder passwordEncoder;

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

    @Test
    void whenAddDoctor_withValidData_shouldSaveUserAndDoctor() {
        AddDoctorRequestDTO request = new AddDoctorRequestDTO();
        request.setFirstName("Novi");
        request.setLastName("Doktor");
        request.setUsername("9876543210987");
        request.setPassword("sigurnaLozinka");
        request.setSpecialization("Hirurg");

        when(userRepository.findByUsername(request.getUsername())).thenReturn(Optional.empty());
        when(passwordEncoder.encode(request.getPassword())).thenReturn("heshiranaLozinka");

        when(userRepository.save(any(User.class))).thenAnswer(invocation -> {
            User userToSave = invocation.getArgument(0);
            userToSave.setId(10L);
            userToSave.getDoctor().setId(5L);
            return userToSave;
        });

        DoctorDTO result = doctorService.addDoctor(request);

        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(userCaptor.capture());
        User savedUser = userCaptor.getValue();

        assertThat(savedUser.getFirstName()).isEqualTo("Novi");
        assertThat(savedUser.getLastName()).isEqualTo("Doktor");
        assertThat(savedUser.getUsername()).isEqualTo("9876543210987");
        assertThat(savedUser.getPassword()).isEqualTo("heshiranaLozinka");
        assertThat(savedUser.getRole()).isEqualTo(Role.ROLE_DOCTOR);

        assertThat(savedUser.getDoctor()).isNotNull();
        assertThat(savedUser.getDoctor().getSpecialization()).isEqualTo("Hirurg");

        assertThat(result.getSpecialization()).isEqualTo("Hirurg");
        assertThat(result.getId()).isEqualTo(5L);
    }

    @Test
    void whenAddDoctor_withExistingUsername_shouldThrowIllegalStateException() {
        AddDoctorRequestDTO request = new AddDoctorRequestDTO();
        request.setUsername("postojeciJMBG");

        when(userRepository.findByUsername("postojeciJMBG")).thenReturn(Optional.of(new User()));

        IllegalStateException thrown = assertThrows(IllegalStateException.class, () -> {
            doctorService.addDoctor(request);
        });

        assertThat(thrown.getMessage()).contains("već postoji");

        verify(userRepository, never()).save(any());
    }

}