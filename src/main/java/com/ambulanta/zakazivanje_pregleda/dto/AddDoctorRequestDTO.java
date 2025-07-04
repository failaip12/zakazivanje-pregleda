package com.ambulanta.zakazivanje_pregleda.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class AddDoctorRequestDTO {

    @NotBlank(message = "Ime lekara ne sme biti prazno.")
    private String firstName;

    @NotBlank(message = "Prezime lekara ne sme biti prazno.")
    private String lastName;

    @NotBlank(message = "Username lekara ne sme biti prazan.")
    private String username;

    @NotBlank(message = "Lozinka ne sme biti prazna.")
    private String password;

    @NotBlank(message = "Specijalizacija ne sme biti prazna.")
    private String specialization;
}