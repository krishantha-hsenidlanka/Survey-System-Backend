package com.example.surveysystembackend.service.response;

import com.example.surveysystembackend.DTO.Response.ResponseDTO;

import java.util.List;

public interface ResponseService {
    ResponseDTO createResponse(ResponseDTO responseDTO);
    ResponseDTO getResponseById(String responseId);
    List<ResponseDTO> getResponsesBySurveyId(String surveyId);
    List<ResponseDTO> getResponsesByUserId(String userId);
    public String getCurrentUserId();
}
