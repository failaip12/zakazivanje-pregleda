package com.ambulanta.zakazivanje_pregleda.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

@Data
public class AddDoctorRequestDTO {

    @NotBlank(message = "Ime lekara ne sme biti prazno.")
    private String firstName;

    @NotBlank(message = "Prezime lekara ne sme biti prazno.")
    private String lastName;

    @NotBlank(message = "JMBG lekara ne sme biti prazan.")
    @Pattern(regexp = "\\d{13}", message = "JMBG mora sadržati tačno 13 cifara.")
    private String username;

    @NotBlank(message = "Lozinka ne sme biti prazna.")
    private String password;

    @NotBlank(message = "Specijalizacija ne sme biti prazna.")
    private String specialization;
}