package com.ambulanta.zakazivanje_pregleda.dto;

import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class AppointmentRequestDTO {

    @NotBlank(message = "Ime pacijenta ne sme biti prazno.")
    private String patientFirstName;

    @NotBlank(message = "Prezime pacijenta ne sme biti prazno.")
    private String patientLastName;

    @NotBlank(message = "JMBG pacijenta ne sme biti prazan.")
    @Pattern(regexp = "\\d{13}", message = "JMBG mora sadržati tačno 13 cifara.")
    private String patientJmbg;


    @NotBlank(message = "Ime lekara ne sme biti prazno.")
    private String doctorFirstName;

    @NotBlank(message = "Prezime lekara ne sme biti prazno.")
    private String doctorLastName;


    @NotNull(message = "Vreme pregleda mora biti definisano.")
    @Future(message = "Vreme pregleda mora biti u budućnosti.")
    private LocalDateTime appointmentTime;
}