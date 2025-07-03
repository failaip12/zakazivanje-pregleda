package com.ambulanta.zakazivanje_pregleda.service;

import com.ambulanta.zakazivanje_pregleda.dto.DoctorDTO;
import com.ambulanta.zakazivanje_pregleda.model.Doctor;
import com.ambulanta.zakazivanje_pregleda.model.User;
import com.ambulanta.zakazivanje_pregleda.repository.DoctorRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class DoctorService {

    private final DoctorRepository doctorRepository;

    @Transactional(readOnly = true)
    public List<DoctorDTO> findAllDoctors() {
        return doctorRepository.findAll().stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    private DoctorDTO convertToDto(Doctor doctor) {
        User doctorUser = doctor.getUser();
        if (doctorUser == null) {
            throw new IllegalStateException("Doktor ID: " + doctor.getId() + " nema povezane korisnicke podatke.");
        }

        return new DoctorDTO(
                doctor.getId(),
                doctorUser.getFirstName(),
                doctorUser.getLastName(),
                doctor.getSpecialization()
        );
    }
}