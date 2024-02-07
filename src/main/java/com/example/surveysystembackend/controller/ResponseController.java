package com.example.surveysystembackend.controller;

import com.example.surveysystembackend.DTO.ErrorResponseDTO;
import com.example.surveysystembackend.DTO.ResponseDTO;
import com.example.surveysystembackend.service.response.ResponseService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Date;
import java.util.List;

@RestController
@RequestMapping("/api/responses")
public class ResponseController {

    private final ResponseService responseService;

    public ResponseController(ResponseService responseService) {
        this.responseService = responseService;
    }

    @PostMapping
    public ResponseEntity<Object> createResponse(@RequestBody ResponseDTO responseDTO) {
        try {
            ResponseDTO createdResponse = responseService.createResponse(responseDTO);
            return ResponseEntity.ok(createdResponse);
        } catch (IllegalArgumentException e) {
            ErrorResponseDTO errorResponse = new ErrorResponseDTO();
            errorResponse.setTimestamp(new Date());
            errorResponse.setStatus(HttpStatus.BAD_REQUEST.value());
            errorResponse.setError(HttpStatus.BAD_REQUEST.getReasonPhrase());
            errorResponse.setMessage("Invalid survey or question IDs: " + e.getMessage());
            errorResponse.setPath("/api/responses");

            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
        }
    }

    @GetMapping("/{responseId}")
    public ResponseEntity<ResponseDTO> getResponseById(@PathVariable String responseId) {
        ResponseDTO responseDTO = responseService.getResponseById(responseId);
        if (responseDTO != null) {
            return ResponseEntity.ok(responseDTO);
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    @GetMapping("/bySurvey/{surveyId}")
    public ResponseEntity<List<ResponseDTO>> getResponsesBySurveyId(@PathVariable String surveyId) {
        List<ResponseDTO> responseDTOs = responseService.getResponsesBySurveyId(surveyId);

        if (!responseDTOs.isEmpty()) {
            return ResponseEntity.ok(responseDTOs);
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    @GetMapping("/byUser/{userId}")
    public ResponseEntity<List<ResponseDTO>> getResponsesByUserId(@PathVariable String userId) {
        List<ResponseDTO> responsesByUserId = responseService.getResponsesByUserId(userId);
        return ResponseEntity.ok(responsesByUserId);
    }
}
