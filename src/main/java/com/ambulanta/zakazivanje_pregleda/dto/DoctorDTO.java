package com.ambulanta.zakazivanje_pregleda.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DoctorDTO {
    private Long id;

    private String firstName;
    private String lastName;

    private String specialization;
}