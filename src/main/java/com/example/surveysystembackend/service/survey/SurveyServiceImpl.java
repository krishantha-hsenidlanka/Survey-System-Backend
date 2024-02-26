package com.example.surveysystembackend.service.survey;

import com.example.surveysystembackend.DTO.Survey.SurveyDTO;
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
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
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
    private final RestTemplate restTemplate;

    @Value("${gemini.api.key}")
    private String apiKey;

    @Value("${gemini.api.url}")
    private String apiUrl;


    @Autowired
    public SurveyServiceImpl(SurveyRepository surveyRepository, ModelMapper modelMapper, RestTemplate restTemplate) {
        this.surveyRepository = surveyRepository;
        this.modelMapper = modelMapper;
        this.restTemplate = restTemplate;

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

        // Set the owner ID
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
        // Survey status to "deleted"
        survey.setDeleted(true);

        surveyRepository.save(survey);

        return true;
    }

    @Override
    public SurveyDTO generateSurvey(String surveyJson, String userDescription) {
        // Combine the default survey JSON and user description
        String combinedJson = surveyJson + "\n\nDescription:\n" + userDescription + "\n\nOutout JSON :\n";

        // Create the request content as per the Bash script
        Map<String, Object> requestBody = new HashMap<>();
        List<Map<String, Object>> contents = new ArrayList<>();

        Map<String, Object> content = new HashMap<>();
        List<Map<String, Object>> parts = new ArrayList<>();

        Map<String, Object> part1 = new HashMap<>();
        part1.put("text", "Use the provided sample JSON format(SurveyJS format) as a reference to create a new survey. Ensure that the survey includes only the following element types: \"radiogroup,\" \"boolean,\" \"dropdown,\" \"comment,\" \"text,\" and \"ranking.\"\n\n﻿﻿");
        Map<String, Object> part2 = new HashMap<>();
        part2.put("text", combinedJson);

        parts.add(part1);
        parts.add(part2);

        content.put("parts", parts);
        contents.add(content);

        requestBody.put("contents", contents);

        // Set up generationConfig
        Map<String, Object> generationConfig = new HashMap<>();
        generationConfig.put("temperature", 0.35);
        generationConfig.put("topK", 1);
        generationConfig.put("topP", 1);
        generationConfig.put("maxOutputTokens", 2048);
        generationConfig.put("stopSequences", new ArrayList<>());

        requestBody.put("generationConfig", generationConfig);

        // Set up safetySettings
        List<Map<String, Object>> safetySettings = new ArrayList<>();

        Map<String, Object> harassmentSetting = new HashMap<>();
        harassmentSetting.put("category", "HARM_CATEGORY_HARASSMENT");
        harassmentSetting.put("threshold", "BLOCK_MEDIUM_AND_ABOVE");
        safetySettings.add(harassmentSetting);

        Map<String, Object> hateSpeechSetting = new HashMap<>();
        hateSpeechSetting.put("category", "HARM_CATEGORY_HATE_SPEECH");
        hateSpeechSetting.put("threshold", "BLOCK_MEDIUM_AND_ABOVE");
        safetySettings.add(hateSpeechSetting);

        Map<String, Object> sexuallyExplicitSetting = new HashMap<>();
        sexuallyExplicitSetting.put("category", "HARM_CATEGORY_SEXUALLY_EXPLICIT");
        sexuallyExplicitSetting.put("threshold", "BLOCK_MEDIUM_AND_ABOVE");
        safetySettings.add(sexuallyExplicitSetting);

        Map<String, Object> dangerousContentSetting = new HashMap<>();
        dangerousContentSetting.put("category", "HARM_CATEGORY_DANGEROUS_CONTENT");
        dangerousContentSetting.put("threshold", "BLOCK_MEDIUM_AND_ABOVE");
        safetySettings.add(dangerousContentSetting);

        requestBody.put("safetySettings", safetySettings);

        // Convert the request body to JSON
        String requestJson;
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            requestJson = objectMapper.writeValueAsString(requestBody);
        } catch (JsonProcessingException e) {
            log.error("Error converting request body to JSON", e);
            return null;
        }

        // Set up headers
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        // Set up the request entity with headers and requestJson
        HttpEntity<String> requestEntity = new HttpEntity<>(requestJson, headers);

        // Make the HTTP POST request
        String urlWithApiKey = apiUrl + "?key=" + apiKey;
        ResponseEntity<String> responseEntity = restTemplate.postForEntity(urlWithApiKey, requestEntity, String.class);

        // Extract "text" from the response
        String responseBody = responseEntity.getBody();
        ObjectMapper objectMapper = new ObjectMapper();

        try {
            // Remove triple backticks
            responseBody = responseBody.replaceAll("```", "");

            JsonNode jsonNode = objectMapper.readTree(responseBody).get("candidates").get(0).get("content").get("parts").get(0).get("text");
            String extractedText = jsonNode.asText();

            // Convert the extracted text to SurveyDTO
            SurveyDTO generatedSurveyDTO = convertJsonToSurveyDTO(extractedText);

            // Save the generated survey to the database
            SurveyDTO savedSurveyDTO = createSurvey(generatedSurveyDTO);

            // Return the ID of the saved survey
            return savedSurveyDTO;
        } catch (JsonProcessingException | NullPointerException e) {
            log.error("Error extracting text from JSON response", e);
            return null;
        }
    }


        private SurveyDTO convertJsonToSurveyDTO(String json) {
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            return objectMapper.readValue(json, SurveyDTO.class);
        } catch (JsonProcessingException e) {
            log.error("Error converting JSON to SurveyDTO", e);
            return null;
        }
    }

}