package com.example.surveysystembackend.service.survey;

import com.example.surveysystembackend.DTO.SurveyDTO;

import java.util.List;

public interface SurveyService {
    SurveyDTO createSurvey(SurveyDTO surveyDTO);
    SurveyDTO getSurveyById(String surveyId);

    SurveyDTO editSurvey(String surveyId, SurveyDTO updatedSurveyDTO);

    List<SurveyDTO> getAllSurveys();
    List<SurveyDTO> getSurveysByOwnerId(String ownerId);

    boolean deleteSurvey(String surveyId);

}
