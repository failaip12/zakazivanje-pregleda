package com.ambulanta.zakazivanje_pregleda.dto;

import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotNull;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class AppointmentRequestDTO {

    @NotNull(message = "Id lekara ne sme biti prazan.")
    private Long doctorId;


    @NotNull(message = "Vreme pregleda mora biti definisano.")
    @Future(message = "Vreme pregleda mora biti u budućnosti.")
    private LocalDateTime appointmentTime;
}