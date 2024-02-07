package com.example.surveysystembackend.controller;

import com.example.surveysystembackend.DTO.SurveyDTO;
import com.example.surveysystembackend.service.survey.SurveyService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/surveys")
public class SurveyController {

    private final SurveyService surveyService;

    public SurveyController(SurveyService surveyService) {
        this.surveyService = surveyService;
    }

    @PostMapping
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    public ResponseEntity<SurveyDTO> createSurvey(@RequestBody SurveyDTO surveyDTO) {
        SurveyDTO createdSurvey = surveyService.createSurvey(surveyDTO);
        return ResponseEntity.ok(createdSurvey);
    }

    @GetMapping("/{surveyId}")
    public ResponseEntity<SurveyDTO> getSurveyById(@PathVariable String surveyId) {
        SurveyDTO surveyDTO = surveyService.getSurveyById(surveyId);
        if (surveyDTO != null) {
            return ResponseEntity.ok(surveyDTO);
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    @PutMapping("/{surveyId}")
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    public ResponseEntity<SurveyDTO> editSurvey(@PathVariable String surveyId, @RequestBody SurveyDTO updatedSurveyDTO) {
        SurveyDTO editedSurvey = surveyService.editSurvey(surveyId, updatedSurveyDTO);
        if (editedSurvey != null) {
            return ResponseEntity.ok(editedSurvey);
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    @GetMapping("/owner/{ownerId}")
    public ResponseEntity<List<SurveyDTO>> getSurveysByOwnerId(@PathVariable String ownerId) {
        List<SurveyDTO> surveysByOwnerId = surveyService.getSurveysByOwnerId(ownerId);
        return ResponseEntity.ok(surveysByOwnerId);
    }


    @GetMapping
    public ResponseEntity<List<SurveyDTO>> getAllSurveys() {
        List<SurveyDTO> allSurveys = surveyService.getAllSurveys();
        return ResponseEntity.ok(allSurveys);
    }

    @DeleteMapping("/{surveyId}")
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    public ResponseEntity<String> deleteSurvey(@PathVariable String surveyId) {
        boolean deleted = surveyService.deleteSurvey(surveyId);

        if (deleted) {
            return ResponseEntity.ok("Survey successfully deleted.");
        } else {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Survey not found or already deleted.");
        }
    }


}
