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
        log.error("IllegalArgumentException occurred: {}", e.getMessage(), e);

        ErrorResponseDTO errorResponse = new ErrorResponseDTO();
        errorResponse.setTimestamp(new Date());
        errorResponse.setStatus(HttpStatus.BAD_REQUEST.value());
        errorResponse.setError(HttpStatus.BAD_REQUEST.getReasonPhrase());
        errorResponse.setMessage("Invalid request: " + e.getMessage());
        errorResponse.setPath("/api/error");

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
    }

    @ExceptionHandler(EntityNotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ResponseEntity<ErrorResponseDTO> handleEntityNotFoundException(EntityNotFoundException e) {
        log.error("Entity not found: {}", e.getMessage(), e);

        ErrorResponseDTO errorResponse = new ErrorResponseDTO();
        errorResponse.setTimestamp(new Date());
        errorResponse.setStatus(HttpStatus.NOT_FOUND.value());
        errorResponse.setError(HttpStatus.NOT_FOUND.getReasonPhrase());
        errorResponse.setMessage("Entity not found: " + e.getMessage());
        errorResponse.setPath("/api/error");

        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResponse);
    }


    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ResponseEntity<ErrorResponseDTO> handleValidationException(MethodArgumentNotValidException e) {
        log.error("Validation error: {}", e.getMessage(), e);

        ErrorResponseDTO errorResponse = new ErrorResponseDTO();
        errorResponse.setTimestamp(new Date());
        errorResponse.setStatus(HttpStatus.BAD_REQUEST.value());
        errorResponse.setError(HttpStatus.BAD_REQUEST.getReasonPhrase());
        errorResponse.setMessage("Validation error: " + e.getMessage());
        errorResponse.setPath("/api/error");

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
    }

    @ExceptionHandler(ConstraintViolationException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ResponseEntity<ErrorResponseDTO> handleConstraintViolationException(ConstraintViolationException e) {
        log.error("Constraint violation: {}", e.getMessage(), e);

        ErrorResponseDTO errorResponse = new ErrorResponseDTO();
        errorResponse.setTimestamp(new Date());
        errorResponse.setStatus(HttpStatus.BAD_REQUEST.value());
        errorResponse.setError(HttpStatus.BAD_REQUEST.getReasonPhrase());
        errorResponse.setMessage("Constraint violation: " + e.getMessage());
        errorResponse.setPath("/api/error");

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
    }

    @ExceptionHandler(AccessDeniedException.class)
    @ResponseStatus(HttpStatus.FORBIDDEN)
    public ResponseEntity<ErrorResponseDTO> handleAccessDeniedException(AccessDeniedException e) {
        log.error("Access denied: {}", e.getMessage(), e);

        ErrorResponseDTO errorResponse = new ErrorResponseDTO();
        errorResponse.setTimestamp(new Date());
        errorResponse.setStatus(HttpStatus.FORBIDDEN.value());
        errorResponse.setError(HttpStatus.FORBIDDEN.getReasonPhrase());
        errorResponse.setMessage("Access denied: " + e.getMessage());
        errorResponse.setPath("/api/error");

        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(errorResponse);
    }

    @ExceptionHandler(CustomRuntimeException.class)
    public ResponseEntity<ErrorResponseDTO> handleCustomRuntimeException(CustomRuntimeException e) {
        log.error("Custom runtime exception: {}", e.getMessage(), e);

        ErrorResponseDTO errorResponse = new ErrorResponseDTO();
        errorResponse.setTimestamp(new Date());
        errorResponse.setStatus(e.getStatus().value());
        errorResponse.setError(e.getStatus().getReasonPhrase());
        errorResponse.setMessage(e.getMessage());
        errorResponse.setPath("/api/error");

        return ResponseEntity.status(e.getStatus()).body(errorResponse);
    }

}
