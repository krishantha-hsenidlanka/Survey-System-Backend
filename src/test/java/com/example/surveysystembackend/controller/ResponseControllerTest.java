package com.example.surveysystembackend.controller;

import com.example.surveysystembackend.DTO.Response.ResponseDTO;
import com.example.surveysystembackend.service.response.ResponseService;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import javax.persistence.EntityNotFoundException;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@Slf4j
@SpringBootTest
class ResponseControllerTest {

    @Mock
    private ResponseService responseService;

    @InjectMocks
    private ResponseController responseController;

    @Test
    void testCreateResponse() {
        log.info("Setting up test for Create Response");
        // Arrange
        ResponseDTO responseDTO = new ResponseDTO();
        responseDTO.setSurveyId("123");

        when(responseService.createResponse(any())).thenReturn(responseDTO);

        // Act
        ResponseEntity<Object> responseEntity = responseController.createResponse(responseDTO);

        // Assert
        assertEquals(ResponseEntity.ok(responseDTO), responseEntity);
        log.info("Test Create Response - ResponseDTO: {}", responseDTO);
        log.info("Test Create Response - Response: {}", responseEntity);
        verify(responseService, times(1)).createResponse(responseDTO);
    }

    @Test
    void testGetResponseById() {
        log.info("Setting up test for Get Response By ID");
        // Arrange
        String responseId = "456";
        ResponseDTO responseDTO = new ResponseDTO();
        responseDTO.setId(responseId);

        when(responseService.getResponseById(responseId)).thenReturn(responseDTO);

        // Act
        ResponseEntity<ResponseDTO> responseEntity = responseController.getResponseById(responseId);

        // Assert
        assertEquals(ResponseEntity.ok(responseDTO), responseEntity);
        log.info("Test Get Response By ID - ResponseDTO: {}", responseDTO);
        log.info("Test Get Response By ID - Response: {}", responseEntity);
        verify(responseService, times(1)).getResponseById(responseId);
    }

    @Test
    void testGetResponsesBySurveyId() {
        log.info("Setting up test for Get Responses By Survey ID");
        // Arrange
        String surveyId = "789";
        List<ResponseDTO> responseDTOs = Collections.singletonList(new ResponseDTO());

        when(responseService.getResponsesBySurveyId(surveyId)).thenReturn(responseDTOs);

        // Act
        ResponseEntity<List<ResponseDTO>> responseEntity = responseController.getResponsesBySurveyId(surveyId);

        // Assert
        assertEquals(ResponseEntity.ok(responseDTOs), responseEntity);
        log.info("Test Get Responses By Survey ID - ResponseDTOs: {}", responseDTOs);
        log.info("Test Get Responses By Survey ID - Response: {}", responseEntity);
        verify(responseService, times(1)).getResponsesBySurveyId(surveyId);
    }

    @Test
    void testGetResponsesByUserId() {
        log.info("Setting up test for Get Responses By User ID");
        // Arrange
        String userId = "101";
        List<ResponseDTO> responseDTOs = Collections.singletonList(new ResponseDTO());

        when(responseService.getResponsesByUserId(userId)).thenReturn(responseDTOs);

        // Act
        ResponseEntity<List<ResponseDTO>> responseEntity = responseController.getResponsesByUserId(userId);

        // Assert
        assertEquals(ResponseEntity.ok(responseDTOs), responseEntity);
        log.info("Test Get Responses By User ID - ResponseDTOs: {}", responseDTOs);
        log.info("Test Get Responses By User ID - Response: {}", responseEntity);
        verify(responseService, times(1)).getResponsesByUserId(userId);
    }

    @Test
    void testGetResponsesByUserId_NoResponsesFound() {
        log.info("Setting up test for Get Responses By User ID - No Responses Found");
        // Arrange
        String userId = "102";

        when(responseService.getResponsesByUserId(userId)).thenReturn(Collections.emptyList());

        // Act and Assert
        EntityNotFoundException exception = assertThrows(EntityNotFoundException.class,
                () -> responseController.getResponsesByUserId(userId));
        assertEquals("No responses found for user ID: " + userId, exception.getMessage());

        log.info("Test Get Responses By User ID - No Responses Found - Exception Message: {}", exception.getMessage());
        verify(responseService, times(1)).getResponsesByUserId(userId);
    }
}
