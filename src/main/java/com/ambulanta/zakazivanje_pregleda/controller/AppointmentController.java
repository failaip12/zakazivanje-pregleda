package com.ambulanta.zakazivanje_pregleda.controller;

import com.ambulanta.zakazivanje_pregleda.dto.AppointmentRequestDTO;
import com.ambulanta.zakazivanje_pregleda.dto.AppointmentResponseDTO;
import com.ambulanta.zakazivanje_pregleda.model.Appointment;
import com.ambulanta.zakazivanje_pregleda.model.AppointmentStatus;
import com.ambulanta.zakazivanje_pregleda.model.User;
import com.ambulanta.zakazivanje_pregleda.service.AppointmentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/appointments")
@RequiredArgsConstructor
public class AppointmentController {
    private final AppointmentService appointmentService;

    @PostMapping
    public ResponseEntity<Appointment> createAppointmentRequest(@Valid @RequestBody AppointmentRequestDTO requestDTO, Authentication authentication) {
        String username = authentication.getName();

        Appointment newAppointment = appointmentService.createAppointmentRequest(requestDTO, username);
        return new ResponseEntity<>(newAppointment, HttpStatus.ACCEPTED);
    }

    @GetMapping
    public ResponseEntity<List<AppointmentResponseDTO>> getAllAppointments(@RequestParam(required = false) AppointmentStatus status) {
        List<AppointmentResponseDTO> appointments;
        if (status != null) {
            appointments = appointmentService.getAppointmentsByStatus(status);
        } else {
            appointments = appointmentService.getAllAppointments();
        }
        return ResponseEntity.ok(appointments);
    }
}