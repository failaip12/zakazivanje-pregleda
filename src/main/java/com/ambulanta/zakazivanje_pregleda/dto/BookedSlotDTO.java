package com.ambulanta.zakazivanje_pregleda.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class BookedSlotDTO {
    private LocalDateTime appointmentTime;
}
