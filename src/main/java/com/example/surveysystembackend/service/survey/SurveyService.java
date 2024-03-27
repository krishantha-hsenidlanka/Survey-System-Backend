package com.example.surveysystembackend.service.survey;

import com.example.surveysystembackend.DTO.Survey.SurveyDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.nio.file.AccessDeniedException;
import java.util.List;

public interface SurveyService {
    SurveyDTO createSurvey(SurveyDTO surveyDTO);
    SurveyDTO getSurveyById(String surveyId);
    SurveyDTO editSurvey(String surveyId, SurveyDTO updatedSurveyDTO) throws AccessDeniedException;
    List<SurveyDTO> getAllSurveys();
    Page<SurveyDTO> getSurveysByOwnerId(String ownerId, Pageable pageable);
    Page<SurveyDTO> getSurveysForLoggedInUser(Pageable pageable);
    boolean deleteSurvey(String surveyId);
}
