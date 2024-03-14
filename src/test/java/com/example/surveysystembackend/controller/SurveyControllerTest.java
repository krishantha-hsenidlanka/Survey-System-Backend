package com.example.surveysystembackend.controller;

import com.example.surveysystembackend.DTO.Survey.SurveyDTO;
import com.example.surveysystembackend.controller.SurveyController;
import com.example.surveysystembackend.service.survey.SurveyGenerationService;
import com.example.surveysystembackend.service.survey.SurveyService;
import jakarta.validation.Valid;
import jakarta.validation.ValidationException;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;

import javax.persistence.EntityNotFoundException;
import java.nio.file.AccessDeniedException;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

@Slf4j
@SpringBootTest
class SurveyControllerTest {

    @Mock
    private SurveyService surveyService;

    @Mock
    private SurveyGenerationService surveyGenerationService;

    @InjectMocks
    private SurveyController surveyController;


    @Test
    void testCreateSurvey() {
        log.info("Setting up test for Create Survey");
        // Arrange
        SurveyDTO surveyDTO = new SurveyDTO();
        surveyDTO.setTitle("Test Survey");
        surveyDTO.setDescription("Test Survey Description");

        BindingResult bindingResult = mock(BindingResult.class);
        when(bindingResult.hasErrors()).thenReturn(false);

        when(surveyService.createSurvey(surveyDTO)).thenReturn(surveyDTO);
        // Act
        ResponseEntity<?> responseEntity = surveyController.createSurvey(surveyDTO, bindingResult);

        // Assert
        assertEquals(ResponseEntity.ok(surveyDTO), responseEntity);
        log.info("Test Create Survey - SurveyDTO: {}", surveyDTO);
        log.info("Test Create Survey - Response: {}", responseEntity);
        verify(surveyService, times(1)).createSurvey(surveyDTO);
    }


    @Test
    void testGetSurveyById() {
        log.info("Setting up test for Get Survey By Id");
        // Arrange
        String surveyId = "123";
        SurveyDTO surveyDTO = new SurveyDTO();
        surveyDTO.setId(surveyId);

        when(surveyService.getSurveyById(surveyId)).thenReturn(surveyDTO);

        // Act
        log.info("Calling getSurveyById with id: {}", surveyId);
        ResponseEntity<SurveyDTO> responseEntity = surveyController.getSurveyById(surveyId);

        // Assert
        log.info("Asserting response for Get Survey By Id");
        assertEquals(ResponseEntity.ok(surveyDTO), responseEntity);
        verify(surveyService, times(1)).getSurveyById(surveyId);
    }

    @Test
    void testEditSurvey() throws AccessDeniedException {
        log.info("Setting up test for Edit Survey");
        // Arrange
        String surveyId = "123";
        SurveyDTO updatedSurveyDTO = new SurveyDTO();
        updatedSurveyDTO.setId(surveyId);

        when(surveyService.editSurvey(surveyId, updatedSurveyDTO)).thenReturn(updatedSurveyDTO);

        // Act
        log.info("Calling editSurvey with id: {}", surveyId);
        ResponseEntity<SurveyDTO> responseEntity = surveyController.editSurvey(surveyId, updatedSurveyDTO);

        // Assert
        log.info("Asserting response for Edit Survey");
        assertEquals(ResponseEntity.ok(updatedSurveyDTO), responseEntity);
        verify(surveyService, times(1)).editSurvey(surveyId, updatedSurveyDTO);
    }

    @Test
    void testGetSurveysForLoggedInUser() {
        log.info("Setting up test for Get Surveys For Logged In User");
        // Arrange
        List<SurveyDTO> surveysForLoggedInUser = Collections.singletonList(new SurveyDTO());

        Pageable pageable = PageRequest.of(0, 10); // Page 0, size 10
        Page<SurveyDTO> surveysPage = new PageImpl<>(surveysForLoggedInUser, pageable, surveysForLoggedInUser.size());

        when(surveyService.getSurveysForLoggedInUser(eq(pageable))).thenReturn(surveysPage);

        // Act
        log.info("Calling getSurveysForLoggedInUser");
        ResponseEntity<Page<SurveyDTO>> responseEntity = surveyController.getSurveysForLoggedInUser(pageable);

        // Assert
        log.info("Asserting response for Get Surveys For Logged In User");
        assertEquals(ResponseEntity.ok(surveysPage), responseEntity);
        verify(surveyService, times(1)).getSurveysForLoggedInUser(eq(pageable));
    }


    @Test
    void testGetSurveysByOwnerId() {
        log.info("Setting up test for Get Surveys By Owner Id");
        // Arrange
        String ownerId = "456";
        List<SurveyDTO> surveysByOwnerId = Collections.singletonList(new SurveyDTO());

        Pageable pageable = PageRequest.of(0, 10); // Page 0, size 10
        Page<SurveyDTO> surveysPage = new PageImpl<>(surveysByOwnerId, pageable, surveysByOwnerId.size());

        when(surveyService.getSurveysByOwnerId(ownerId, pageable)).thenReturn(surveysPage);

        // Act
        log.info("Calling getSurveysByOwnerId with ownerId: {}", ownerId);
        ResponseEntity<Page<SurveyDTO>> responseEntity = surveyController.getSurveysByOwnerId(ownerId, pageable);

        // Assert
        log.info("Asserting response for Get Surveys By Owner Id");
        assertEquals(ResponseEntity.ok(surveysPage), responseEntity);
        verify(surveyService, times(1)).getSurveysByOwnerId(ownerId, pageable);
    }

    @Test
    void testGetAllSurveys() {
        log.info("Setting up test for Get All Surveys");
        // Arrange
        List<SurveyDTO> allSurveys = Collections.singletonList(new SurveyDTO());

        when(surveyService.getAllSurveys()).thenReturn(allSurveys);

        // Act
        log.info("Calling getAllSurveys");
        ResponseEntity<List<SurveyDTO>> responseEntity = surveyController.getAllSurveys();

        // Assert
        log.info("Asserting response for Get All Surveys");
        assertEquals(ResponseEntity.ok(allSurveys), responseEntity);
        verify(surveyService, times(1)).getAllSurveys();
    }

    @Test
    void testDeleteSurvey() {
        log.info("Setting up test for Delete Survey");
        // Arrange
        String surveyId = "789";

        when(surveyService.deleteSurvey(surveyId)).thenReturn(true);

        // Act
        log.info("Calling deleteSurvey with id: {}", surveyId);
        ResponseEntity<String> responseEntity = surveyController.deleteSurvey(surveyId);

        // Assert
        log.info("Asserting response for Delete Survey");
        assertEquals(ResponseEntity.ok("{\"message\":\"Survey deleted successfully\"}"), responseEntity);
        verify(surveyService, times(1)).deleteSurvey(surveyId);
    }

    @Test
    void testGenerateSurvey() {
        log.info("Setting up test for Generate Survey");
        // Arrange
        String userDescription = "Test User Description";
        SurveyDTO generatedSurvey = new SurveyDTO();
        generatedSurvey.setTitle("Generated Survey");

        when(surveyGenerationService.generateSurvey(eq(userDescription))).thenReturn(generatedSurvey);

        // Act
        log.info("Calling generateSurvey with user description: {}", userDescription);
        ResponseEntity<SurveyDTO> responseEntity = surveyController.generateSurvey(userDescription);

        // Assert
        log.info("Asserting response for Generate Survey");
        assertEquals(ResponseEntity.ok(generatedSurvey), responseEntity);
        verify(surveyGenerationService, times(1)).generateSurvey(eq(userDescription));
    }

}
