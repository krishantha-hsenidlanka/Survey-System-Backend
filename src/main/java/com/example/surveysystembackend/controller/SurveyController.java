package com.example.surveysystembackend.controller;

import com.example.surveysystembackend.DTO.Survey.SurveyDTO;
import com.example.surveysystembackend.exception.CustomRuntimeException;
import com.example.surveysystembackend.service.survey.SurveyGenerationService;
import com.example.surveysystembackend.service.survey.SurveyService;
import jakarta.validation.Valid;
import jakarta.validation.ValidationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import javax.persistence.EntityNotFoundException;
import java.nio.file.AccessDeniedException;
import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/surveys")
public class SurveyController {

    private final SurveyService surveyService;
    private final SurveyGenerationService surveyGenerationService;

    public SurveyController(SurveyService surveyService, SurveyGenerationService surveyGenerationService) {
        this.surveyService = surveyService;
        this.surveyGenerationService = surveyGenerationService;
    }

    @PostMapping
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    public ResponseEntity<SurveyDTO> createSurvey(@RequestBody @Valid SurveyDTO surveyDTO, BindingResult bindingResult) {
        log.info("API hit: POST /api/surveys");
        if (bindingResult.hasErrors()) {
            throw new ValidationException("Validation errors: " + bindingResult.getAllErrors());
        }
        return ResponseEntity.ok(surveyService.createSurvey(surveyDTO));
    }

    @GetMapping("/{surveyId}")
    public ResponseEntity<SurveyDTO> getSurveyById(@PathVariable String surveyId) {
        log.info("API hit: GET /api/surveys/{}", surveyId);
        return ResponseEntity.ok(surveyService.getSurveyById(surveyId));
    }

    @PutMapping("/{surveyId}")
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    public ResponseEntity<SurveyDTO> editSurvey(@PathVariable String surveyId, @RequestBody SurveyDTO updatedSurveyDTO) throws AccessDeniedException {
        log.info("API hit: PUT /api/surveys/{}", surveyId);
        return ResponseEntity.ok(surveyService.editSurvey(surveyId, updatedSurveyDTO));
    }

    @GetMapping("/user")
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    public ResponseEntity<List<SurveyDTO>> getSurveysForLoggedInUser() {
        log.info("API hit: GET /api/surveys/user");
        return ResponseEntity.ok(surveyService.getSurveysForLoggedInUser());
    }

    @GetMapping("/owner/{ownerId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<SurveyDTO>> getSurveysByOwnerId(@PathVariable String ownerId) {
        log.info("API hit: GET /api/surveys/owner/{}", ownerId);
        return ResponseEntity.ok(surveyService.getSurveysByOwnerId(ownerId));
    }

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<SurveyDTO>> getAllSurveys() {
        log.info("API hit: GET /api/surveys");
        return ResponseEntity.ok(surveyService.getAllSurveys());
    }

    @DeleteMapping("/{surveyId}")
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    public ResponseEntity<String> deleteSurvey(@PathVariable String surveyId) {
        log.info("API hit: DELETE /api/surveys/{}", surveyId);
        boolean deleted = surveyService.deleteSurvey(surveyId);
        if (deleted) {
            return ResponseEntity.ok("{\"message\":\"Survey deleted successfully\"}");
        } else {
            throw new EntityNotFoundException("Survey not found or already deleted for ID: " + surveyId);
        }
    }

    @PostMapping("/generate-survey")
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    public ResponseEntity<SurveyDTO> generateSurvey(@RequestBody String userDescription) {
        log.info("API hit: POST /api/surveys/generate-survey");
        SurveyDTO generatedSurvey = surveyGenerationService.generateSurvey(userDescription);
        if (generatedSurvey != null) {
            log.info("Survey generated successfully");
            return ResponseEntity.ok(generatedSurvey);
        } else {
            log.error("Error generating survey");
            throw new CustomRuntimeException("Error generating survey", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}
