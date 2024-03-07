package com.example.surveysystembackend.service.survey;

import com.example.surveysystembackend.DTO.Survey.SurveyDTO;

import java.nio.file.AccessDeniedException;
import java.util.List;

public interface SurveyService {
    SurveyDTO createSurvey(SurveyDTO surveyDTO);
    SurveyDTO getSurveyById(String surveyId);
    SurveyDTO editSurvey(String surveyId, SurveyDTO updatedSurveyDTO) throws AccessDeniedException;
    List<SurveyDTO> getAllSurveys();
    List<SurveyDTO> getSurveysByOwnerId(String ownerId);
    List<SurveyDTO> getSurveysForLoggedInUser();
    boolean deleteSurvey(String surveyId);
}
