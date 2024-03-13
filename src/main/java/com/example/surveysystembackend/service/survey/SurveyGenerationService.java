package com.example.surveysystembackend.service.survey;

import com.example.surveysystembackend.DTO.Survey.SurveyDTO;
import com.example.surveysystembackend.exception.CustomRuntimeException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.*;

@Slf4j
@Service
public class SurveyGenerationService {

    @Value("${gemini.api.key}")
    private String apiKey;

    @Value("${gemini.api.url}")
    private String apiUrl;

    private final RestTemplate restTemplate;

    private  final SurveyService surveyService;

    public SurveyGenerationService(RestTemplate restTemplate, SurveyService surveyService) {
        this.restTemplate = restTemplate;
        this.surveyService = surveyService;
    }

    public SurveyDTO generateSurvey(String userDescription) {
        try{
            log.info("Attempting to generate survey, user description: {}", userDescription);
            String combinedJson = combineJsonWithDescription(getDefaultSurveyJson(), userDescription);
            Map<String, Object> requestBody = createRequestBody(combinedJson);
            String requestJson = convertRequestBodyToJson(requestBody);
            ResponseEntity<String> responseEntity = makeHttpPostRequest(requestJson);
            String extractedText = extractTextFromResponse(responseEntity);
            SurveyDTO generatedSurveyDTO = convertJsonToSurveyDTO(extractedText);
            SurveyDTO savedSurveyDTO = saveSurveyToDatabase(generatedSurveyDTO);

            return savedSurveyDTO;
        } catch (Exception e) {
            log.error("Error generating survey: {}", e.getMessage(), e);
            throw new CustomRuntimeException("Error generating survey", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    private String combineJsonWithDescription(String surveyJson, String userDescription) {
        return surveyJson + "\n\nDescription:\n" + userDescription + "\n\nOutout JSON :\n";
    }

    private Map<String, Object> createRequestBody(String combinedJson) {
        Map<String, Object> requestBody = new HashMap<>();

        // Create the request content
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

        return requestBody;
    }


    private String convertRequestBodyToJson(Map<String, Object> requestBody) {
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            return objectMapper.writeValueAsString(requestBody);
        } catch (JsonProcessingException e) {
            handleJsonConversionError(e);
            return null;
        }
    }

    private ResponseEntity<String> makeHttpPostRequest(String requestJson) {
        try {
            HttpHeaders headers = createHttpHeaders();
            HttpEntity<String> requestEntity = new HttpEntity<>(requestJson, headers);
            String urlWithApiKey = apiUrl + "?key=" + apiKey;
            return restTemplate.postForEntity(urlWithApiKey, requestEntity, String.class);
        } catch (Exception e) {
            log.error("Error making HTTP POST request: {}", e.getMessage(), e);
            throw new CustomRuntimeException("Error making HTTP POST request", HttpStatus.BAD_REQUEST);
        }
    }

    private String extractTextFromResponse(ResponseEntity<String> responseEntity) {
        String responseBody = responseEntity.getBody();

        try {
            ObjectMapper objectMapper = new ObjectMapper();
            responseBody = responseBody.replaceAll("```", "");

            JsonNode jsonNode = objectMapper.readTree(responseBody)
                    .get("candidates")
                    .get(0)
                    .get("content")
                    .get("parts")
                    .get(0)
                    .get("text");

            return jsonNode.asText();
        } catch (JsonProcessingException | NullPointerException e) {
            log.error("Error extracting text from response: {}", e.getMessage(), e);
            throw new CustomRuntimeException(e.getMessage(),HttpStatus.BAD_REQUEST);
        }
    }


    private SurveyDTO convertJsonToSurveyDTO(String extractedText) {
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            return objectMapper.readValue(extractedText, SurveyDTO.class);
        } catch (JsonProcessingException e) {
            handleJsonConversionError(e);
            return null;
        }
    }

    private SurveyDTO saveSurveyToDatabase(SurveyDTO generatedSurveyDTO) {
        return  this.surveyService.createSurvey(generatedSurveyDTO);
    }

    private HttpHeaders createHttpHeaders() {
        try{
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            return headers;
        } catch (Exception e) {
            log.error("Error creating HTTP headers: {}", e.getMessage(), e);
            throw new CustomRuntimeException("Error creating HTTP headers", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    private String getDefaultSurveyJson() {
        // default survey JSON
        return """
                                
                ﻿
                ```{
                 "title": "Survey Title",
                 "description": "Survey Description",
                 "pages": [
                 {
                  "name": "page1",
                  "elements": [
                  {
                   "type": "radiogroup",
                   "name": "question1",
                   "choices": [
                   "Item 1",
                   "Item 2",
                   "Item 3"
                   ]
                  },
                  {
                   "type": "checkbox",
                   "name": "question3",
                   "choices": [
                   "Item 1",
                   "Item 2",
                   "Item 3"
                   ]
                  },
                  {
                   "type": "boolean",
                   "name": "question5"
                  },
                  {
                   "type": "dropdown",
                   "name": "question4",
                   "choices": [
                   "Item 1",
                   "Item 2",
                   "Item 3"
                   ]
                  },
                  {
                   "type": "comment",
                   "name": "question8"
                  }
                  ],
                  "title": "Page 1",
                  "description": "Page 1 Description"
                 },
                 {
                  "name": "page2",
                  "elements": [
                  {
                   "type": "ranking",
                   "name": "question6",
                   "choices": [
                   "Item 1",
                   "Item 2",
                   "Item 3"
                   ]
                  },
                  {
                   "type": "text",
                   "name": "question7"
                  }
                  ],
                  "title": "Page 2",
                  "description": "Page 2 Description"
                 }
                 ]
                }```
                ﻿
                ﻿﻿Generate a new survey for below description in JSON format according to the above provided structure. Ensure that the survey has the specified title, description, pages, elements, titles, and descriptions as per the given sample, and only include the mentioned element types.
                Must remember that should generate according to this DTO models -
                ﻿
                ```publicclassSurveyDTO {
                    private String id;
                    private String title;
                    private String description;
                    private List<PageDTO> pages;
                   \s
                }
                public class PageDTO {
                    private String name;
                    private String title;
                    private String description;
                    private List<ElementDTO> elements;
                }
                                
                public class ElementDTO {
                    private String type;
                    private String name;
                    private String title;
                    private List<Object> choices;
                    private Boolean isRequired;
                    private List<Object> rows;
                    private List<Object> columns;
                }```
                ﻿
                """;
    }

    private void handleJsonConversionError(JsonProcessingException e) {
        log.error("Error converting request body to JSON", e);
        throw new CustomRuntimeException("Error converting request body to JSON", HttpStatus.BAD_REQUEST);
    }
}
