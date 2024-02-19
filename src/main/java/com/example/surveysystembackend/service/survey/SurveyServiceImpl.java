package com.example.surveysystembackend.service.survey;

import com.example.surveysystembackend.DTO.ChoiceDTO;
import com.example.surveysystembackend.DTO.SurveyDTO;
import com.example.surveysystembackend.model.Choice;
import com.example.surveysystembackend.model.Element;
import com.example.surveysystembackend.model.Page;
import com.example.surveysystembackend.model.Survey;
import com.example.surveysystembackend.repository.SurveyRepository;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.modelmapper.TypeToken;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
public class SurveyServiceImpl implements SurveyService {

    private final SurveyRepository surveyRepository;
    private final ModelMapper modelMapper;

    @Autowired
    public SurveyServiceImpl(SurveyRepository surveyRepository, ModelMapper modelMapper) {
        this.surveyRepository = surveyRepository;
        this.modelMapper = modelMapper;
    }

    @Override
    public SurveyDTO createSurvey(SurveyDTO surveyDTO) {
        log.info("Creating survey: {}", surveyDTO);
        // Map SurveyDTO to Survey entity
        Survey survey = modelMapper.map(surveyDTO, Survey.class);

        // Map QuestionDTOs to Element entities with unique identifiers
        List<Element> elements = surveyDTO.getPages().stream()
                .flatMap(pageDTO -> pageDTO.getElements().stream()
                        .map(elementDTO -> {
                            if (elementDTO != null) {
                                Element element = modelMapper.map(elementDTO, Element.class);
                                if (elementDTO.getChoices() != null) {
                                    element.setChoices(elementDTO.getChoices());
                                }


                                element.setId(UUID.randomUUID().toString());
                                return element;
                            }
                            return null; // Handle the case when elementDTO is null
                        })
                )
                .filter(Objects::nonNull) // Remove any null elements
                .collect(Collectors.toList());

        // Set the elements in the survey entity
        survey.setPages(surveyDTO.getPages().stream()
                .map(pageDTO -> {
                    Page page = modelMapper.map(pageDTO, Page.class);
                    page.setElements(elements);
                    return page;
                })
                .collect(Collectors.toList()));

        // Set the owner ID from the currently authenticated user
        String ownerId = SecurityContextHolder.getContext().getAuthentication().getName();
        survey.setOwnerId(ownerId);

        // Initialize the list if it's not provided in the SurveyDTO
        if (elements != null) {
            survey.setPages(surveyDTO.getPages().stream()
                    .map(pageDTO -> modelMapper.map(pageDTO, Page.class))
                    .collect(Collectors.toList()));
        } else {
            survey.setPages(new ArrayList<>());
        }

        // Save the survey to the repository
        survey.setId(null);
        survey = surveyRepository.save(survey);
        log.info("Survey created: {}", survey);

        // Map the saved survey entity back to a SurveyDTO for response
        return modelMapper.map(survey, SurveyDTO.class);
    }


    @Override
    public SurveyDTO editSurvey(String surveyId, SurveyDTO updatedSurveyDTO) {
        // Check if the survey exists
        Optional<Survey> optionalSurvey = surveyRepository.findById(surveyId);
        if (optionalSurvey.isEmpty()) {
            // Survey not found
            return null;
        }

        // Map updated survey data to the existing survey entity
        Survey existingSurvey = optionalSurvey.get();

        // Retrieve the existing owner ID and edit access user IDs
        String ownerId = existingSurvey.getOwnerId();
        Set<String> editAccessUserIds = existingSurvey.getEditAccessUserIds();

        // Get the authenticated user ID
        String authenticatedUserId = SecurityContextHolder.getContext().getAuthentication().getName();

        // Check if the authenticated user has permission to edit the survey
        if (!ownerId.equals(authenticatedUserId) && !editAccessUserIds.contains(authenticatedUserId)) {
            log.warn("Permission denied to make changes of the survey");
            // Unauthorized
            return null;
        }

        // Set the owner ID in the updated survey DTO

        updatedSurveyDTO.setOwnerId(ownerId);

        // Map updated survey DTO to the existing survey entity
        modelMapper.map(updatedSurveyDTO, existingSurvey);

        // Map updated QuestionDTOs to Element entities with unique identifiers
        List<Element> updatedElements = updatedSurveyDTO.getPages().stream()
                .flatMap(pageDTO -> pageDTO.getElements().stream()
                        .filter(elementDTO -> elementDTO != null) // Filter out null elements
                        .map(elementDTO -> {
                            Element element = modelMapper.map(elementDTO, Element.class);
                            if (elementDTO.getChoices() != null) {
                                element.setChoices(elementDTO.getChoices());
                            }
                            element.setId(UUID.randomUUID().toString());
                            return element;
                        })
                        .peek(element -> {
                            if (element.getId() == null) {
                                element.setId(UUID.randomUUID().toString());
                            }
                        })
                )
                .collect(Collectors.toList());


        if (updatedElements != null) {
            existingSurvey.setPages(updatedSurveyDTO.getPages().stream()
                    .map(pageDTO -> {
                        Page page = modelMapper.map(pageDTO, Page.class);
                        page.setElements(updatedElements);
                        return page;
                    })
                    .collect(Collectors.toList()));
        } else {
            // If no questions provided in the updated survey, set it to an empty list
            existingSurvey.setPages(new ArrayList<>());
        }

        // Save the updated survey to the repository
        existingSurvey.setId(surveyId);
        existingSurvey = surveyRepository.save(existingSurvey);

        // Map the updated survey entity back to a SurveyDTO for response
        return modelMapper.map(existingSurvey, SurveyDTO.class);
    }



    @Override
    public SurveyDTO getSurveyById(String surveyId) {

        return surveyRepository.findById(surveyId)
                .map(survey -> modelMapper.map(survey, SurveyDTO.class))
                .orElse(null);
    }


    @Override
    public List<SurveyDTO> getSurveysByOwnerId(String ownerId) {
        List<Survey> surveysByOwnerId = surveyRepository.findByOwnerIdAndDeletedFalse(ownerId);
        return surveysByOwnerId.stream()
                .map(survey -> modelMapper.map(survey, SurveyDTO.class))
                .collect(Collectors.toList());
    }

    @Override
    public List<SurveyDTO> getSurveysForLoggedInUser() {
        // Get the authenticated user ID
        String ownerId = SecurityContextHolder.getContext().getAuthentication().getName();

        List<Survey> surveysByOwnerId = surveyRepository.findByOwnerIdAndDeletedFalse(ownerId);
        return surveysByOwnerId.stream()
                .map(survey -> modelMapper.map(survey, SurveyDTO.class))
                .collect(Collectors.toList());
    }

    @Override
    public List<SurveyDTO> getAllSurveys() {
        List<Survey> allSurveys = surveyRepository.findAll();
        return allSurveys.stream()
                .map(survey -> modelMapper.map(survey, SurveyDTO.class))
                .collect(Collectors.toList());
    }

    @Override
    public boolean deleteSurvey(String surveyId) {
        Optional<Survey> optionalSurvey = surveyRepository.findById(surveyId);
        if (optionalSurvey.isEmpty()) {
            // Survey not found
            return false;
        }

        Survey survey = optionalSurvey.get();
        // Set the survey status to "deleted" or handle deletion logic based on your requirements
        survey.setDeleted(true);

        // Save the updated survey to the repository
        surveyRepository.save(survey);

        return true;
    }

}