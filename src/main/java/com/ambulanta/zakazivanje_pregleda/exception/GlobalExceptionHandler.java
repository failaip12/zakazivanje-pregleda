package com.ambulanta.zakazivanje_pregleda.exception;

import com.ambulanta.zakazivanje_pregleda.dto.ApiErrorResponseDTO;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiErrorResponseDTO> handleValidationExceptions(MethodArgumentNotValidException ex) {
        Map<String, String> errors = new HashMap<>();
        ex.getBindingResult().getAllErrors().forEach((error) -> {
            String fieldName = ((FieldError) error).getField();
            String errorMessage = error.getDefaultMessage();
            errors.put(fieldName, errorMessage);
        });
        ApiErrorResponseDTO errorResponse = new ApiErrorResponseDTO(
                HttpStatus.BAD_REQUEST.value(),
                "Bad Request",
                "Podaci nisu validni.",
                errors
        );
        return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(DoctorNotFoundException.class)
    public ResponseEntity<ApiErrorResponseDTO> handleDoctorNotFoundException(DoctorNotFoundException ex) {
        ApiErrorResponseDTO errorResponse = new ApiErrorResponseDTO(
                HttpStatus.NOT_FOUND.value(),
                "Not Found",
                ex.getMessage()
        );
        return new ResponseEntity<>(errorResponse, HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(AppointmentNotFoundException.class)
    public ResponseEntity<ApiErrorResponseDTO> handleAppointmentNotFoundException(AppointmentNotFoundException ex) {
        ApiErrorResponseDTO errorResponse = new ApiErrorResponseDTO(
                HttpStatus.NOT_FOUND.value(),
                "Not Found",
                ex.getMessage()
        );
        return new ResponseEntity<>(errorResponse, HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ApiErrorResponseDTO> handleTypeMismatchException(MethodArgumentTypeMismatchException ex) {
        String name = ex.getName();
        String type = ex.getRequiredType() != null ? ex.getRequiredType().getSimpleName() : null;
        Object value = ex.getValue();
        String message = String.format("Parametar '%s' ima nevalidnu vrednost '%s'. Očekivani tip je '%s'.",
                name, value, type);

        ApiErrorResponseDTO errorResponse = new ApiErrorResponseDTO(
                HttpStatus.BAD_REQUEST.value(),
                "Bad Request",
                message
        );
        return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ApiErrorResponseDTO> handleHttpMessageNotReadableException(HttpMessageNotReadableException ex) {
        String message = "Zahtev ima neispravan format (malformed JSON).";

        ApiErrorResponseDTO errorResponse = new ApiErrorResponseDTO(
                HttpStatus.BAD_REQUEST.value(),
                "Bad Request",
                message
        );
        return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(PatientNotFoundException.class)
    public ResponseEntity<ApiErrorResponseDTO> handlePatientNotFoundException(PatientNotFoundException ex) {
        ApiErrorResponseDTO errorResponse = new ApiErrorResponseDTO(
                HttpStatus.NOT_FOUND.value(),
                "Not Found",
                ex.getMessage()
        );
        return new ResponseEntity<>(errorResponse, HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(InvalidAppointmentTimeException.class)
    public ResponseEntity<ApiErrorResponseDTO> handleInvalidAppointmentTimeException(InvalidAppointmentTimeException ex) {
        ApiErrorResponseDTO errorResponse = new ApiErrorResponseDTO(
                HttpStatus.BAD_REQUEST.value(),
                "Bad Request",
                ex.getMessage()
        );
        return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(UserNotFoundException.class)
    public ResponseEntity<ApiErrorResponseDTO> handleUserNotFoundException(UserNotFoundException ex) {
        ApiErrorResponseDTO errorResponse = new ApiErrorResponseDTO(
                HttpStatus.NOT_FOUND.value(),
                "Not Found",
                ex.getMessage()
        );
        return new ResponseEntity<>(errorResponse, HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiErrorResponseDTO> handleGenericException(Exception ex) {
        ApiErrorResponseDTO errorResponse = new ApiErrorResponseDTO(
                HttpStatus.INTERNAL_SERVER_ERROR.value(),
                "Internal Server Error",
                "Došlo je do nepredviđene greške na serveru: " + ex.getMessage()
        );
        return new ResponseEntity<>(errorResponse, HttpStatus.INTERNAL_SERVER_ERROR);
    }
}