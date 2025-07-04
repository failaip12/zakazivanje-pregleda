package com.ambulanta.zakazivanje_pregleda.service;

import com.ambulanta.zakazivanje_pregleda.dto.AuthRequestDTO;
import com.ambulanta.zakazivanje_pregleda.dto.AuthResponseDTO;
import com.ambulanta.zakazivanje_pregleda.dto.RegisterRequestDTO;
import com.ambulanta.zakazivanje_pregleda.model.Patient;
import com.ambulanta.zakazivanje_pregleda.model.Role;
import com.ambulanta.zakazivanje_pregleda.model.User;
import com.ambulanta.zakazivanje_pregleda.repository.PatientRepository;
import com.ambulanta.zakazivanje_pregleda.repository.UserRepository;
import com.ambulanta.zakazivanje_pregleda.security.JwtService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private UserRepository userRepository;
    @Mock
    private PatientRepository patientRepository;
    @Mock
    private PasswordEncoder passwordEncoder;
    @Mock
    private JwtService jwtService;
    @Mock
    private AuthenticationManager authenticationManager;

    @InjectMocks
    private AuthService authService;

    private RegisterRequestDTO registerRequest;

    @BeforeEach
    void setUp() {
        registerRequest = new RegisterRequestDTO();
        registerRequest.setJmbg("1234567890123");
        registerRequest.setUsername("pera123");
        registerRequest.setPassword("password123");
        registerRequest.setFirstName("Pera");
        registerRequest.setLastName("Peric");
    }

    @Test
    void whenRegister_withValidData_shouldSaveUserAndReturnToken() {
        when(userRepository.findByUsername(registerRequest.getUsername())).thenReturn(Optional.empty());
        when(passwordEncoder.encode(registerRequest.getPassword())).thenReturn("hashedPassword");
        when(jwtService.generateToken(any(User.class))).thenReturn("test-jwt-token");

        AuthResponseDTO response = authService.register(registerRequest);

        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(userCaptor.capture());
        User savedUser = userCaptor.getValue();

        assertThat(savedUser.getUsername()).isEqualTo("pera123");
        assertThat(savedUser.getPassword()).isEqualTo("hashedPassword");
        assertThat(savedUser.getFirstName()).isEqualTo("Pera");
        assertThat(savedUser.getRole()).isEqualTo(Role.ROLE_PATIENT);
        assertThat(savedUser.getPatient()).isNotNull();
        assertThat(savedUser.getPatient().getJmbg()).isEqualTo("1234567890123");

        assertThat(response).isNotNull();
        assertThat(response.getToken()).isEqualTo("test-jwt-token");
    }

    @Test
    void whenRegister_withExistingUsername_shouldThrowException() {
        when(userRepository.findByUsername(registerRequest.getUsername())).thenReturn(Optional.of(new User()));

        IllegalStateException thrown = assertThrows(IllegalStateException.class, () -> {
            authService.register(registerRequest);
        });

        assertThat(thrown.getMessage()).isEqualTo("Korisničko ime već postoji!");
    }

    @Test
    void whenRegister_withExistingJMBG_shouldThrowException() {
        when(patientRepository.findByJmbg(registerRequest.getJmbg())).thenReturn(Optional.of(new Patient()));

        IllegalStateException thrown = assertThrows(IllegalStateException.class, () -> {
            authService.register(registerRequest);
        });

        assertThat(thrown.getMessage()).isEqualTo("JMBG već postoji!");
    }

    @Test
    void whenLogin_withValidCredentials_shouldReturnToken() {
        AuthRequestDTO loginRequest = new AuthRequestDTO();
        loginRequest.setUsername("testuser");
        loginRequest.setPassword("password");

        User user = new User();
        user.setUsername("testuser");

        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(user));
        when(jwtService.generateToken(user)).thenReturn("logged-in-token");

        AuthResponseDTO response = authService.login(loginRequest);

        verify(authenticationManager).authenticate(any());
        assertThat(response.getToken()).isEqualTo("logged-in-token");
    }

    @Test
    void whenLogin_withInvalidCredentials_shouldThrowAuthenticationException() {
        AuthRequestDTO loginRequest = new AuthRequestDTO();
        loginRequest.setUsername("testuser");
        loginRequest.setPassword("wrongpassword");

        when(authenticationManager.authenticate(any())).thenThrow(new BadCredentialsException("Loši kredencijali"));

        assertThrows(BadCredentialsException.class, () -> {
            authService.login(loginRequest);
        });
    }
}