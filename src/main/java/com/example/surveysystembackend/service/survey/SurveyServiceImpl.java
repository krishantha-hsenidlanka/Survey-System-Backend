package com.example.surveysystembackend.service.survey;

import com.example.surveysystembackend.DTO.Survey.SurveyDTO;
import com.example.surveysystembackend.exception.CustomRuntimeException;
import com.example.surveysystembackend.model.Element;
import com.example.surveysystembackend.model.Page;
import com.example.surveysystembackend.model.Survey;
import com.example.surveysystembackend.repository.SurveyRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.nio.file.AccessDeniedException;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
public class SurveyServiceImpl implements SurveyService {

    private final SurveyRepository surveyRepository;
    private final ModelMapper modelMapper;

    @Autowired
    public SurveyServiceImpl(SurveyRepository surveyRepository, ModelMapper modelMapper, RestTemplate restTemplate) {
        this.surveyRepository = surveyRepository;
        this.modelMapper = modelMapper;
    }

    @Override
    public SurveyDTO createSurvey(SurveyDTO surveyDTO) {
        log.info("Creating survey: {}", surveyDTO);
        Survey survey = modelMapper.map(surveyDTO, Survey.class);

        // Map Questions to Element entities
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
                            return null; // elementDTO is null
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

        String ownerId = SecurityContextHolder.getContext().getAuthentication().getName();
        survey.setOwnerId(ownerId);

        // Initialize the list if it's not provided
        if (elements != null) {
            survey.setPages(surveyDTO.getPages().stream()
                    .map(pageDTO -> modelMapper.map(pageDTO, Page.class))
                    .collect(Collectors.toList()));
        } else {
            survey.setPages(new ArrayList<>());
        }

        survey.setId(null);
        survey = surveyRepository.save(survey);
        log.info("Survey created: {}", survey);

        return modelMapper.map(survey, SurveyDTO.class);
    }

    @Override
    public SurveyDTO editSurvey(String surveyId, SurveyDTO updatedSurveyDTO) throws AccessDeniedException {
        // Check survey exists
        Optional<Survey> optionalSurvey = surveyRepository.findById(surveyId);
        if (optionalSurvey.isEmpty()) {
            return null;
        }

        Survey existingSurvey = optionalSurvey.get();

        String ownerId = existingSurvey.getOwnerId();
        Set<String> editAccessUserIds = existingSurvey.getEditAccessUserIds();

        // Authenticated username
        String authenticatedUserId = SecurityContextHolder.getContext().getAuthentication().getName();

        // Check permission
        if (!ownerId.equals(authenticatedUserId) && !editAccessUserIds.contains(authenticatedUserId)) {
            log.warn("Permission denied to make changes of the survey");
            throw new AccessDeniedException("Permission denied to make changes of the survey");
        }

        updatedSurveyDTO.setOwnerId(ownerId);

        modelMapper.map(updatedSurveyDTO, existingSurvey);

        // Map updated Questions
        List<Page> updatedPages = updatedSurveyDTO.getPages().stream()
                .map(pageDTO -> {
                    Page page = modelMapper.map(pageDTO, Page.class);

                    List<Element> updatedElements = pageDTO.getElements().stream()
                            .filter(elementDTO -> elementDTO != null) // Filter null
                            .map(elementDTO -> {
                                Element element = modelMapper.map(elementDTO, Element.class);
                                if (elementDTO.getChoices() != null) {
                                    element.setChoices(elementDTO.getChoices());
                                }
                                element.setId(UUID.randomUUID().toString());
                                return element;
                            })
                            .collect(Collectors.toList());

                    page.setElements(updatedElements);
                    return page;
                })
                .collect(Collectors.toList());

        existingSurvey.setPages(updatedPages);

        existingSurvey.setId(surveyId);
        existingSurvey = surveyRepository.save(existingSurvey);

        return modelMapper.map(existingSurvey, SurveyDTO.class);
    }

    @Override
    public SurveyDTO getSurveyById(String surveyId) {
        Optional<Survey> optionalSurvey = surveyRepository.findById(surveyId);
        return optionalSurvey.map(survey -> {
            String authenticatedUserId = SecurityContextHolder.getContext().getAuthentication().getName();
            boolean isOwnerOrHasEditAccess = authenticatedUserId.equals(survey.getOwnerId()) ||
                    survey.getEditAccessUserIds().contains(authenticatedUserId);

            Collection<SimpleGrantedAuthority> authorities = (Collection<SimpleGrantedAuthority>)
                    SecurityContextHolder.getContext().getAuthentication().getAuthorities();
            boolean isAdmin = authorities.stream()
                    .anyMatch(authority -> authority.getAuthority().equals("ROLE_ADMIN"));

            if (survey.isPublic() || isOwnerOrHasEditAccess || isAdmin) {
                return modelMapper.map(survey, SurveyDTO.class);
            } else {
                log.warn("Permission denied to view the survey");
                throw new CustomRuntimeException("Permission denied to view the survey", HttpStatus.FORBIDDEN);
            }
        }).orElse(null);
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
        // Survey status to "deleted"
        survey.setDeleted(true);

        surveyRepository.save(survey);

        return true;
    }

}