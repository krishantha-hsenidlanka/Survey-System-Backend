package com.example.surveysystembackend.exception;

import com.example.surveysystembackend.DTO.Common.ErrorResponseDTO;
import jakarta.validation.ConstraintViolationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;

import javax.persistence.EntityNotFoundException;
import java.nio.file.AccessDeniedException;
import java.util.Date;

@Slf4j
@ControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ErrorResponseDTO> handleIllegalArgumentException(IllegalArgumentException e) {
        return handleException(e, HttpStatus.BAD_REQUEST, "Invalid request: " + e.getMessage());
    }

    @ExceptionHandler(EntityNotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ResponseEntity<ErrorResponseDTO> handleEntityNotFoundException(EntityNotFoundException e) {
        return handleException(e, HttpStatus.NOT_FOUND, "Entity not found: " + e.getMessage());
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ResponseEntity<ErrorResponseDTO> handleValidationException(MethodArgumentNotValidException e) {
        return handleException(e, HttpStatus.BAD_REQUEST, "Validation error: " + e.getMessage());
    }

    @ExceptionHandler(ConstraintViolationException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ResponseEntity<ErrorResponseDTO> handleConstraintViolationException(ConstraintViolationException e) {
        return handleException(e, HttpStatus.BAD_REQUEST, "Constraint violation: " + e.getMessage());
    }

    @ExceptionHandler(AccessDeniedException.class)
    @ResponseStatus(HttpStatus.FORBIDDEN)
    public ResponseEntity<ErrorResponseDTO> handleAccessDeniedException(AccessDeniedException e) {
        return handleException(e, HttpStatus.FORBIDDEN, "Access denied: " + e.getMessage());
    }

    @ExceptionHandler(CustomRuntimeException.class)
    public ResponseEntity<ErrorResponseDTO> handleCustomRuntimeException(CustomRuntimeException e) {
        return handleException(e, e.getStatus(), e.getMessage());
    }

    private ResponseEntity<ErrorResponseDTO> handleException(Exception e, HttpStatus httpStatus, String message) {
        log.error("Handling Exception: {} occurred: {}", e.getClass().getSimpleName(), e.getMessage());
        ErrorResponseDTO errorResponse = new ErrorResponseDTO(new Date(), httpStatus.value(), httpStatus.getReasonPhrase(), message, "/api/error");
        return ResponseEntity.status(httpStatus).body(errorResponse);
    }
}
