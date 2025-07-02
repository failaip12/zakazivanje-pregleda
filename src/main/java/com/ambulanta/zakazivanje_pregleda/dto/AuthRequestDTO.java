package com.ambulanta.zakazivanje_pregleda.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;
@Data
public class AuthRequestDTO {
    @NotBlank(message = "Username mora biti definisan.")
    private String username;
    @NotBlank(message = "Password mora biti definisan.")
    private String password;
}