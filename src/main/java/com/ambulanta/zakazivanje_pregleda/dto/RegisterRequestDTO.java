package com.ambulanta.zakazivanje_pregleda.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;
@Data
public class RegisterRequestDTO {
    @NotBlank(message = "Username mora biti definisan.")
    private String username;
    @NotBlank(message = "Password mora biti definisan.")
    private String password;

    @NotBlank(message = "Ime mora biti definisano.")
    private String firstName;
    @NotBlank(message = "Prezime mora biti definisan.")
    private String lastName;
}
