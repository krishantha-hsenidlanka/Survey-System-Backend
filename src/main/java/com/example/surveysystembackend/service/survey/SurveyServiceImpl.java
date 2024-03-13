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

import javax.persistence.EntityNotFoundException;
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
        try{
            log.info("Attempting to create survey: {}", surveyDTO);
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
        } catch (Exception e) {
            log.error("Error creating survey: {}", e.getMessage(), e);
            throw new CustomRuntimeException("Error creating survey", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @Override
    public SurveyDTO editSurvey(String surveyId, SurveyDTO updatedSurveyDTO) throws AccessDeniedException {
        try{
            log.info("Attempting to edit survey with ID: {}", surveyId);
            Survey existingSurvey = surveyRepository.findById(surveyId).orElseThrow(()->{
                throw new EntityNotFoundException("Survey not found, Survey Id: "+ surveyId);
            });


            String ownerId = existingSurvey.getOwnerId();
            Set<String> editAccessUserIds = existingSurvey.getEditAccessUserIds();

            // Authenticated username
            String authenticatedUserId = SecurityContextHolder.getContext().getAuthentication().getName();

            // Check permission
            if (!ownerId.equals(authenticatedUserId) && !editAccessUserIds.contains(authenticatedUserId)) {
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
            log.info("Survey edited successfully: {}", existingSurvey);
            return modelMapper.map(existingSurvey, SurveyDTO.class);
        } catch (EntityNotFoundException e) {
            log.warn("Survey not found, Survey Id: {}", surveyId, e);
            throw e;
        }
        catch (AccessDeniedException e) {
            log.warn("Permission denied to edit survey: {}", surveyId, e);
            throw e;
        } catch (Exception e) {
            log.error("Error editing survey: {}", e.getMessage(), e);
            throw new CustomRuntimeException("Error editing survey", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @Override
    public SurveyDTO getSurveyById(String surveyId) {
        try{
            log.info("Attempting to get survey by ID: {}", surveyId);
            Survey survey = surveyRepository.findById(surveyId).orElseThrow(()->{
                throw new EntityNotFoundException("Survey not found, Survey Id: "+ surveyId);
            });

            String authenticatedUserId = SecurityContextHolder.getContext().getAuthentication().getName();
            boolean isOwnerOrHasEditAccess = authenticatedUserId.equals(survey.getOwnerId()) ||
                    survey.getEditAccessUserIds().contains(authenticatedUserId);

            Collection<SimpleGrantedAuthority> authorities = (Collection<SimpleGrantedAuthority>)
                    SecurityContextHolder.getContext().getAuthentication().getAuthorities();
            boolean isAdmin = authorities.stream()
                    .anyMatch(authority -> authority.getAuthority().equals("ROLE_ADMIN"));

            if (survey.isPublic() || isOwnerOrHasEditAccess || isAdmin) {
                log.info("Survey fetched successfully: {}", survey);
                return modelMapper.map(survey, SurveyDTO.class);
            } else {
                throw new AccessDeniedException("Permission denied to view the survey");
            }
        } catch (EntityNotFoundException e) {
            log.warn("Survey not found, Survey Id: {}", surveyId, e);
            throw e;
        } catch (AccessDeniedException e) {
            log.warn("Permission denied to view the survey: {}", surveyId, e);
            throw new CustomRuntimeException("Permission denied to view the survey", HttpStatus.FORBIDDEN);
        } catch (Exception e) {
            log.error("Error fetching survey: {}", e.getMessage(), e);
            throw new CustomRuntimeException("Error fetching survey", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @Override
    public List<SurveyDTO> getSurveysByOwnerId(String ownerId) {
        try {
            log.info("Attempting to get surveys by owner ID: {}", ownerId);
            List<Survey> surveysByOwnerId = surveyRepository.findByOwnerIdAndDeletedFalse(ownerId);
            if (surveysByOwnerId.isEmpty()) {
                log.warn("No surveys found for owner ID: {}", ownerId);
                throw new EntityNotFoundException("No surveys found for owner ID: " + ownerId);
            }
            log.info("Surveys fetched successfully for owner ID: {}", ownerId);
            return surveysByOwnerId.stream()
                    .map(survey -> modelMapper.map(survey, SurveyDTO.class))
                    .collect(Collectors.toList());
        } catch (EntityNotFoundException e) {
            log.error("No surveys found for owner ID: {}", ownerId, e);
            throw e;
        } catch (Exception e) {
            log.error("Error getting surveys by owner ID: {}", e.getMessage(), e);
            throw new CustomRuntimeException("Error getting surveys by owner ID", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @Override
    public List<SurveyDTO> getSurveysForLoggedInUser() {
        try {
            log.info("Attempting to get surveys for logged-in user");
            String ownerId = SecurityContextHolder.getContext().getAuthentication().getName();
            List<Survey> surveysByOwnerId = surveyRepository.findByOwnerIdAndDeletedFalse(ownerId);
            if (surveysByOwnerId.isEmpty()) {
                log.warn("No surveys found for logged-in user ID: {}", ownerId);
                throw new EntityNotFoundException("No surveys found for logged-in user ID: " + ownerId);
            }
            log.info("Surveys fetched successfully for logged-in user ID: {}", ownerId);
            return surveysByOwnerId.stream()
                    .map(survey -> modelMapper.map(survey, SurveyDTO.class))
                    .collect(Collectors.toList());
        } catch (EntityNotFoundException e) {
            log.error("No surveys found for logged-in user ID: {}", e.getMessage(), e);
            throw e;
        } catch (Exception e) {
            log.error("Error getting surveys for logged-in user: {}", e.getMessage(), e);
            throw new CustomRuntimeException("Error getting surveys for logged-in user", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @Override
    public List<SurveyDTO> getAllSurveys() {
        try {
            log.info("Attempting to get all surveys");
            List<Survey> allSurveys = surveyRepository.findAll();
            if (allSurveys.isEmpty()) {
                log.warn("No surveys found");
                throw new EntityNotFoundException("No surveys found");
            }
            log.info("All surveys fetched successfully");
            return allSurveys.stream()
                    .map(survey -> modelMapper.map(survey, SurveyDTO.class))
                    .collect(Collectors.toList());
        } catch (EntityNotFoundException e) {
            log.error("No surveys found: {}", e.getMessage(), e);
            throw e;
        } catch (Exception e) {
            log.error("Error getting all surveys: {}", e.getMessage(), e);
            throw new CustomRuntimeException("Error getting all surveys", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @Override
    public boolean deleteSurvey(String surveyId) {
        try {
            log.info("Attempting to delete survey with ID: {}", surveyId);
            Survey survey = surveyRepository.findById(surveyId).orElseThrow(
                    () -> {
                        log.warn("Survey not found, Survey Id: {}", surveyId);
                        throw new EntityNotFoundException("Survey not found, Survey Id: " + surveyId);
                    }
            );

            // Survey status to "deleted"
            survey.setDeleted(true);

            surveyRepository.save(survey);
            log.info("Survey deleted successfully: {}", surveyId);
            return true;
        } catch (EntityNotFoundException e) {
            log.error("Survey not found, Survey Id: {}", surveyId, e);
            throw e;
        } catch (Exception e) {
            log.error("Error deleting survey: {}", e.getMessage(), e);
            throw new CustomRuntimeException("Error deleting survey", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}