package com.example.surveysystembackend.service.response;

import com.example.surveysystembackend.DTO.Response.ResponseDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface ResponseService {
    ResponseDTO createResponse(ResponseDTO responseDTO);
    ResponseDTO getResponseById(String responseId);
    List<ResponseDTO> getResponsesBySurveyId(String surveyId);
    Page<ResponseDTO> getResponsesByUserId(String userId, Pageable pageable);
    public String getCurrentUserId();
}
