package com.ambulanta.zakazivanje_pregleda.dto;

import com.ambulanta.zakazivanje_pregleda.model.AppointmentStatus;
import lombok.Data;
import java.time.LocalDateTime;

@Data
public class AppointmentResponseDTO {
    private Long id;
    private PatientDTO patient;
    private DoctorDTO doctor;
    private LocalDateTime appointmentTime;
    private AppointmentStatus status;
}