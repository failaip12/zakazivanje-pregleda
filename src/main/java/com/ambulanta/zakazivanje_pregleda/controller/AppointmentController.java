package com.ambulanta.zakazivanje_pregleda.controller;

import com.ambulanta.zakazivanje_pregleda.dto.AppointmentRequestDTO;
import com.ambulanta.zakazivanje_pregleda.dto.AppointmentResponseDTO;
import com.ambulanta.zakazivanje_pregleda.model.Appointment;
import com.ambulanta.zakazivanje_pregleda.model.AppointmentStatus;
import com.ambulanta.zakazivanje_pregleda.service.AppointmentService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/appointments")
@RequiredArgsConstructor
public class AppointmentController {

    private final AppointmentService appointmentService;

    @PostMapping
    public ResponseEntity<Appointment> createAppointmentRequest(@RequestBody AppointmentRequestDTO requestDTO) {
        Appointment newAppointment = appointmentService.createAppointmentRequest(requestDTO);
        // Vraćamo 202 ACCEPTED jer zahtev nije odmah obrađen, već je primljen na obradu
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