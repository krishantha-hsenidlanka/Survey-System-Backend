package com.example.surveysystembackend.controller;

import com.example.surveysystembackend.DTO.Survey.SurveyDTO;
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

    public SurveyController(SurveyService surveyService) {
        this.surveyService = surveyService;
    }

    @PostMapping
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    public ResponseEntity<?> createSurvey(@RequestBody @Valid SurveyDTO surveyDTO, BindingResult bindingResult) {
        log.info("Creating survey");

        if (bindingResult.hasErrors()) {
            log.error("Validation errors: {}", bindingResult.getAllErrors());
            throw new ValidationException("Validation errors: " + bindingResult.getAllErrors());
        }

        SurveyDTO createdSurvey = surveyService.createSurvey(surveyDTO);
        log.info("Survey created successfully");
        return ResponseEntity.ok(createdSurvey);
    }

    @GetMapping("/{surveyId}")
    public ResponseEntity<SurveyDTO> getSurveyById(@PathVariable String surveyId) {
        log.info("Fetching survey by ID: {}", surveyId);

        SurveyDTO surveyDTO = surveyService.getSurveyById(surveyId);
        if (surveyDTO != null) {
            log.info("Survey found successfully");
            return ResponseEntity.ok(surveyDTO);
        } else {
            log.warn("Survey not found for ID: {}", surveyId);
            throw new EntityNotFoundException("Survey not found for ID: "+ surveyId);
        }
    }

    @PutMapping("/{surveyId}")
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    public ResponseEntity<SurveyDTO> editSurvey(@PathVariable String surveyId, @RequestBody SurveyDTO updatedSurveyDTO) throws AccessDeniedException {
        log.info("Editing survey with ID: {}", surveyId);

        SurveyDTO editedSurvey = surveyService.editSurvey(surveyId, updatedSurveyDTO);
        if (editedSurvey != null) {
            log.info("Survey edited successfully");
            return ResponseEntity.ok(editedSurvey);
        } else {
            log.warn("Survey not found for ID: {}", surveyId);
            throw new EntityNotFoundException("Survey not found for ID: "+ surveyId);
        }
    }

    @GetMapping("/user")
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    public ResponseEntity<List<SurveyDTO>> getSurveysForLoggedInUser() {
        log.info("Fetching surveys for logged-in user");

        List<SurveyDTO> surveysForLoggedInUser = surveyService.getSurveysForLoggedInUser();

        if (surveysForLoggedInUser.isEmpty()) {
            log.warn("No surveys found for logged-in user");
            throw new EntityNotFoundException("No surveys found for the logged-in user");
        }

        log.info("Surveys fetched successfully for logged-in user");
        return ResponseEntity.ok(surveysForLoggedInUser);
    }


    @GetMapping("/owner/{ownerId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<SurveyDTO>> getSurveysByOwnerId(@PathVariable String ownerId) {
        log.info("Fetching surveys by owner ID: {}", ownerId);

        List<SurveyDTO> surveysByOwnerId = surveyService.getSurveysByOwnerId(ownerId);

        if (surveysByOwnerId.isEmpty()) {
            log.warn("No surveys found for owner ID: {}", ownerId);
            throw new EntityNotFoundException("No surveys found for owner ID: " + ownerId);
        }

        log.info("Surveys fetched successfully by owner ID: {}", ownerId);
        return ResponseEntity.ok(surveysByOwnerId);
    }


    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<SurveyDTO>> getAllSurveys() {
        log.info("Fetching all surveys");

        List<SurveyDTO> allSurveys = surveyService.getAllSurveys();

        if (allSurveys.isEmpty()) {
            log.warn("No surveys found");
            throw new EntityNotFoundException("No surveys found");
        }

        log.info("All surveys fetched successfully");
        return ResponseEntity.ok(allSurveys);
    }


    @DeleteMapping("/{surveyId}")
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    public ResponseEntity<String> deleteSurvey(@PathVariable String surveyId) {
        log.info("Deleting survey with ID: {}", surveyId);

        boolean deleted = surveyService.deleteSurvey(surveyId);

        if (deleted) {
            log.info("Survey deleted successfully");
            return ResponseEntity.ok("{\"message\":\"Survey deleted successfully\"}");
        } else {
            log.warn("Survey not found or already deleted for ID: {}", surveyId);
            throw new EntityNotFoundException("Survey not found or already deleted for ID: " + surveyId);
        }
    }

    @PostMapping("/generate-survey")
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    public ResponseEntity<SurveyDTO> generateSurvey(@RequestBody String userDescription) {
        log.info("Generating survey based on user description");

        // Call the service method to generate a survey
        SurveyDTO generatedSurvey = surveyService.generateSurvey( getDefaultSurveyJson(), userDescription);

        if (generatedSurvey != null) {
            log.info("Survey generated successfully");
            return ResponseEntity.ok(generatedSurvey);
        } else {
            log.error("Error generating survey");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }

    private String getDefaultSurveyJson() {
        // default survey JSON
        return """
                                
                ﻿
                ```{
                 "title": "Survey Title",
                 "description": "Survey Description",
                 "pages": [
                 {
                  "name": "page1",
                  "elements": [
                  {
                   "type": "radiogroup",
                   "name": "question1",
                   "choices": [
                   "Item 1",
                   "Item 2",
                   "Item 3"
                   ]
                  },
                  {
                   "type": "checkbox",
                   "name": "question3",
                   "choices": [
                   "Item 1",
                   "Item 2",
                   "Item 3"
                   ]
                  },
                  {
                   "type": "boolean",
                   "name": "question5"
                  },
                  {
                   "type": "dropdown",
                   "name": "question4",
                   "choices": [
                   "Item 1",
                   "Item 2",
                   "Item 3"
                   ]
                  },
                  {
                   "type": "comment",
                   "name": "question8"
                  }
                  ],
                  "title": "Page 1",
                  "description": "Page 1 Description"
                 },
                 {
                  "name": "page2",
                  "elements": [
                  {
                   "type": "ranking",
                   "name": "question6",
                   "choices": [
                   "Item 1",
                   "Item 2",
                   "Item 3"
                   ]
                  },
                  {
                   "type": "text",
                   "name": "question7"
                  }
                  ],
                  "title": "Page 2",
                  "description": "Page 2 Description"
                 }
                 ]
                }```
                ﻿
                ﻿﻿Generate a new survey for below description in JSON format according to the above provided structure. Ensure that the survey has the specified title, description, pages, elements, titles, and descriptions as per the given sample, and only include the mentioned element types.
                Must remember that should generate according to this DTO models -
                ﻿
                ```publicclassSurveyDTO {
                    private String id;
                    private String title;
                    private String description;
                    private List<PageDTO> pages;
                   \s
                }
                public class PageDTO {
                    private String name;
                    private String title;
                    private String description;
                    private List<ElementDTO> elements;
                }
                                
                public class ElementDTO {
                    private String type;
                    private String name;
                    private String title;
                    private List<Object> choices;
                    private Boolean isRequired;
                    private List<Object> rows;
                    private List<Object> columns;
                }```
                ﻿
                """;
    }

}
