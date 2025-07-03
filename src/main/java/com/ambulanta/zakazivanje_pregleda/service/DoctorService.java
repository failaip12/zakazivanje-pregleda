package com.ambulanta.zakazivanje_pregleda.service;

import com.ambulanta.zakazivanje_pregleda.dto.AddDoctorRequestDTO;
import com.ambulanta.zakazivanje_pregleda.dto.DoctorDTO;
import com.ambulanta.zakazivanje_pregleda.model.Doctor;
import com.ambulanta.zakazivanje_pregleda.model.Role;
import com.ambulanta.zakazivanje_pregleda.model.User;
import com.ambulanta.zakazivanje_pregleda.repository.DoctorRepository;
import com.ambulanta.zakazivanje_pregleda.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class DoctorService {

    private final DoctorRepository doctorRepository;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Transactional(readOnly = true)
    public List<DoctorDTO> findAllDoctors() {
        return doctorRepository.findAll().stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    @Transactional
    public DoctorDTO addDoctor(AddDoctorRequestDTO request) {
        if (userRepository.findByUsername(request.getUsername()).isPresent()) {
            throw new IllegalStateException("Korisnik sa JMBG-om " + request.getUsername() + " već postoji.");
        }

        User doctorUser = new User();
        doctorUser.setFirstName(request.getFirstName());
        doctorUser.setLastName(request.getLastName());
        doctorUser.setUsername(request.getUsername());
        doctorUser.setPassword(passwordEncoder.encode(request.getPassword()));
        doctorUser.setRole(Role.ROLE_DOCTOR);

        Doctor doctor = new Doctor();
        doctor.setSpecialization(request.getSpecialization());

        doctorUser.setDoctor(doctor);

        User savedUser = userRepository.save(doctorUser);

        return convertToDto(savedUser.getDoctor());
    }

    private DoctorDTO convertToDto(Doctor doctor) {
        User doctorUser = doctor.getUser();
        if (doctorUser == null) {
            return new DoctorDTO(doctor.getId(), "N/A", "N/A", doctor.getSpecialization());
        }

        return new DoctorDTO(
                doctor.getId(),
                doctorUser.getFirstName(),
                doctorUser.getLastName(),
                doctor.getSpecialization()
        );
    }
}