package com.example.surveysystembackend.controller;

import com.example.surveysystembackend.DTO.Common.ErrorResponseDTO;
import com.example.surveysystembackend.DTO.Response.ResponseDTO;
import com.example.surveysystembackend.service.response.ResponseService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
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
    public ResponseEntity<ResponseDTO> createResponse(@RequestBody ResponseDTO responseDTO) {
        log.info("API hit: POST /api/responses, resposeDTO: {}", responseDTO);
        ResponseDTO createdResponse = responseService.createResponse(responseDTO);
        return ResponseEntity.ok(createdResponse);
    }

    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    @GetMapping("/{responseId}")
    public ResponseEntity<ResponseDTO> getResponseById(@PathVariable String responseId) {
        log.info("API hit: GET /api/responses/{}", responseId);
        ResponseDTO responseDTO = responseService.getResponseById(responseId);
        return ResponseEntity.ok(responseDTO);
    }

    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    @GetMapping("/bySurvey/{surveyId}")
    public ResponseEntity<List<ResponseDTO>> getResponsesBySurveyId(@PathVariable String surveyId) {
        log.info("API hit: GET /api/responses/bySurvey/{}", surveyId);
        List<ResponseDTO> responseDTOs = responseService.getResponsesBySurveyId(surveyId);
        return ResponseEntity.ok(responseDTOs);
    }

    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    @GetMapping("/byUser/{userId}")
    public ResponseEntity<Page<ResponseDTO>> getResponsesByUserId(@PathVariable String userId,
                                                                  @RequestParam(defaultValue = "0") int page,
                                                                  @RequestParam(defaultValue = "10") int size) {
        log.info("API hit: GET /api/responses/byUser/{}", userId);
        Pageable pageable = PageRequest.of(page, size);
        Page<ResponseDTO> responsesByUserId = responseService.getResponsesByUserId(userId, pageable);
        return ResponseEntity.ok(responsesByUserId);
    }

    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    @GetMapping("/byCurrentUser")
    public ResponseEntity<Page<ResponseDTO>> getResponsesByCurrentUser(@RequestParam(defaultValue = "0") int page,
                                                                       @RequestParam(defaultValue = "10") int size) {
        log.info("API hit: GET /api/responses/byCurrentUser");
        String currentUserId = responseService.getCurrentUserId();
        Pageable pageable = PageRequest.of(page, size);
        Page<ResponseDTO> responsesByCurrentUser = responseService.getResponsesByUserId(currentUserId, pageable);
        return ResponseEntity.ok(responsesByCurrentUser);
    }
}
