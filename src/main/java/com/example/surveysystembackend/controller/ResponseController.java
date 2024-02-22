package com.example.surveysystembackend.controller;

import com.example.surveysystembackend.DTO.Common.ErrorResponseDTO;
import com.example.surveysystembackend.DTO.Response.ResponseDTO;
import com.example.surveysystembackend.service.response.ResponseService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.persistence.EntityNotFoundException;
import java.util.Date;
import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/responses")
public class ResponseController {

    private final ResponseService responseService;

    public ResponseController(ResponseService responseService) {
        this.responseService = responseService;
    }

    @PostMapping
    public ResponseEntity<Object> createResponse(@RequestBody ResponseDTO responseDTO) {
        log.info("Creating response for survey: {}", responseDTO.getSurveyId());
        ResponseDTO createdResponse = responseService.createResponse(responseDTO);
        log.info("Response created successfully");
        return ResponseEntity.ok(createdResponse);
    }

    @GetMapping("/{responseId}")
    public ResponseEntity<ResponseDTO> getResponseById(@PathVariable String responseId) {
        log.info("Fetching response by ID: {}", responseId);

        ResponseDTO responseDTO = responseService.getResponseById(responseId);
        if (responseDTO != null) {
            log.info("Response found successfully");
            return ResponseEntity.ok(responseDTO);
        } else {
            log.warn("Response not found for ID: {}", responseId);
            throw new EntityNotFoundException("Response not found for ID: " + responseId);
        }
    }

    @GetMapping("/bySurvey/{surveyId}")
    public ResponseEntity<List<ResponseDTO>> getResponsesBySurveyId(@PathVariable String surveyId) {
        log.info("Fetching responses by survey ID: {}", surveyId);

        List<ResponseDTO> responseDTOs = responseService.getResponsesBySurveyId(surveyId);

        if (!responseDTOs.isEmpty()) {
            log.info("Responses found successfully for survey ID: {}", surveyId);
            return ResponseEntity.ok(responseDTOs);
        } else {
            log.warn("No responses found for survey ID: {}", surveyId);
            throw new EntityNotFoundException("Response not found for survey ID: " + surveyId);
        }
    }

    @GetMapping("/byUser/{userId}")
    public ResponseEntity<List<ResponseDTO>> getResponsesByUserId(@PathVariable String userId) {
        log.info("Fetching responses by user ID: {}", userId);

        List<ResponseDTO> responsesByUserId = responseService.getResponsesByUserId(userId);

        if (!responsesByUserId.isEmpty()) {
            log.info("Responses fetched successfully for user ID: {}", userId);
            return ResponseEntity.ok(responsesByUserId);
        } else {
            log.warn("No responses found for user ID: {}", userId);
            throw new EntityNotFoundException("No responses found for user ID: " + userId);
        }

    }
}
