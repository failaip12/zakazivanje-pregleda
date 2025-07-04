package com.ambulanta.zakazivanje_pregleda.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Data;
@Data
public class RegisterRequestDTO {
    @NotBlank(message = "Username mora biti definisan.")
    private String username;
    @NotBlank(message = "Password mora biti definisan.")
    private String password;

    @NotBlank(message = "JMBG mora biti definisan.")
    @Pattern(regexp = "\\d{13}", message = "JMBG mora sadržati tačno 13 cifara.")
    private String jmbg;

    @NotBlank(message = "Ime mora biti definisano.")
    private String firstName;
    @NotBlank(message = "Prezime mora biti definisano.")
    private String lastName;
}
