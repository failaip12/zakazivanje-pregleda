package com.ambulanta.zakazivanje_pregleda.dto;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class AppointmentRequestDTO {
    private String patientFirstName;
    private String patientLastName;
    private String patientJmbg;

    private String doctorFirstName;
    private String doctorLastName;

    private LocalDateTime appointmentTime;
}