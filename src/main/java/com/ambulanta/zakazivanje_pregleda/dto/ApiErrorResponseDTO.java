package com.ambulanta.zakazivanje_pregleda.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;
import java.time.LocalDateTime;
import java.util.Map;

@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ApiErrorResponseDTO {
    private LocalDateTime timestamp;
    private int status;
    private String error;
    private String message;
    private Map<String, String> errors;

    public ApiErrorResponseDTO(int status, String error, String message) {
        this.timestamp = LocalDateTime.now();
        this.status = status;
        this.error = error;
        this.message = message;
    }

    public ApiErrorResponseDTO(int status, String error, String message, Map<String, String> errors) {
        this(status, error, message);
        this.errors = errors;
    }
}