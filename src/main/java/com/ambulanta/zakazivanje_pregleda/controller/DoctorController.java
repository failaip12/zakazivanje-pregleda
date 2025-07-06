package com.ambulanta.zakazivanje_pregleda.controller;

import com.ambulanta.zakazivanje_pregleda.dto.AddDoctorRequestDTO;
import com.ambulanta.zakazivanje_pregleda.dto.BookedSlotDTO;
import com.ambulanta.zakazivanje_pregleda.dto.DoctorDTO;
import com.ambulanta.zakazivanje_pregleda.service.DoctorService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/doctors")
@RequiredArgsConstructor
public class DoctorController {
    private final DoctorService doctorService;

    @GetMapping
    public ResponseEntity<List<DoctorDTO>> getAllDoctors() {
        List<DoctorDTO> doctors = doctorService.findAllDoctors();
        return ResponseEntity.ok(doctors);
    }

    @PostMapping
    public ResponseEntity<DoctorDTO> addNewDoctor(@Valid @RequestBody AddDoctorRequestDTO request) {
        DoctorDTO newDoctor = doctorService.addDoctor(request);
        return new ResponseEntity<>(newDoctor, HttpStatus.CREATED);
    }

    @GetMapping("/{doctorId}/appointments")
    public ResponseEntity<List<BookedSlotDTO>> getDoctorAppointments(@PathVariable Long doctorId) {
        List<BookedSlotDTO> appointments = doctorService.getDoctorAppointments(doctorId);
        return ResponseEntity.ok(appointments);
    }
}