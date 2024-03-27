package com.example.surveysystembackend.service.response;

import com.example.surveysystembackend.DTO.Response.ResponseDTO;
import com.example.surveysystembackend.exception.CustomRuntimeException;
import com.example.surveysystembackend.model.Response;
import com.example.surveysystembackend.model.Survey;
import com.example.surveysystembackend.repository.ResponseRepository;
import com.example.surveysystembackend.repository.SurveyRepository;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import javax.persistence.EntityNotFoundException;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Service
public class ResponseServiceImpl implements ResponseService {

    private final ResponseRepository responseRepository;
    private final ModelMapper modelMapper;
    private final SurveyRepository surveyRepository;

    @Autowired
    public ResponseServiceImpl(
            ResponseRepository responseRepository,
            ModelMapper modelMapper,
            SurveyRepository surveyRepository) {
        this.responseRepository = responseRepository;
        this.modelMapper = modelMapper;
        this.surveyRepository = surveyRepository;
    }

    @Override
    public ResponseDTO createResponse(ResponseDTO responseDTO) {
        try {
            Response response = modelMapper.map(responseDTO, Response.class);
            String userId = getCurrentUserId();
            log.info("Attempting to create response by user: {}",userId);
            response.setUserId(userId);
            response.setId(null);

            if (!isValidSurveyAndQuestions(response.getSurveyId(), response.getAnswers())) {
                log.error("Invalid survey or question IDs or Deleted");
                throw new IllegalArgumentException("Invalid survey or question IDs or Deleted");
            }

            response = responseRepository.save(response);
            log.info("Response created successfully, response: {}", response.toString());
            return modelMapper.map(response, ResponseDTO.class);
        } catch (Exception e) {
            log.error("Error creating response: {}", e.getMessage(), e);
            throw new CustomRuntimeException("Error creating response", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @Override
    public ResponseDTO getResponseById(String responseId) {
        try {
            log.info("Attempting to get response by ID: {}", responseId);
            Response response = responseRepository.findById(responseId).orElseThrow(
                    () -> {
                        throw new EntityNotFoundException("Response not found for ID: " + responseId);
                    }
            );
            log.info("Response found successfully, response: {}", response.toString());
            return modelMapper.map(response, ResponseDTO.class);
        } catch (EntityNotFoundException e) {
            log.warn("Response not found for ID: {}", responseId);
            throw e;
        }
        catch (Exception e) {
            log.error("Error fetching response by ID: {}", e.getMessage(), e);
            throw new CustomRuntimeException("Error fetching response by ID", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @Override
    public List<ResponseDTO> getResponsesBySurveyId(String surveyId) {
        try {
            log.info("Attempting to get response by SurveyId: {}", surveyId);
            List<Response> responses = responseRepository.findBySurveyId(surveyId);

            if (responses.isEmpty()) {
                log.warn("No responses found for SurveyId: {}", surveyId);
                throw new EntityNotFoundException("No responses found for SurveyId: " + surveyId);
            }
            log.info("Responses found successfully for SurveyId: {}", surveyId);
            return responses.stream()
                    .map(response -> modelMapper.map(response, ResponseDTO.class))
                    .collect(Collectors.toList());
        } catch (EntityNotFoundException e) {
            log.warn("No responses found for SurveyId: {}", surveyId);
            throw e;
        } catch (Exception e) {
            log.error("Error fetching responses by survey ID: {}", e.getMessage(), e);
            throw new CustomRuntimeException("Error fetching responses by survey ID", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @Override
    public Page<ResponseDTO> getResponsesByUserId(String userId, Pageable pageable) {
        try {
            log.info("Attempting to get response by userId: {}", userId);
            Page<Response> responsesByUserId = responseRepository.findByUserId(userId, pageable);

            if (responsesByUserId.isEmpty()) {
                log.warn("No responses found for userId: {}", userId);
                throw new EntityNotFoundException("No responses found for userId: " + userId);
            }
            log.info("Responses found successfully for userId: {}", userId);
            return responsesByUserId.map(response -> modelMapper.map(response, ResponseDTO.class));
        } catch (EntityNotFoundException e) {
            log.warn("No responses found for userId: {}", userId);
            throw e;
        } catch (Exception e) {
            log.error("Error fetching responses by user ID: {}", e.getMessage(), e);
            throw new CustomRuntimeException("Error fetching responses by user ID", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    private boolean isValidSurveyAndQuestions(String surveyId, List<Object> answers) {
        try {
            log.info("Attempting to validate survey and questions for surveyId: {}", surveyId);
            Survey survey = surveyRepository.findById(surveyId).orElseThrow(
                    () -> {
                        throw new EntityNotFoundException("Survey not found for ID: " + surveyId);
                    }
            );

            if (survey.isDeleted()) {
                log.warn("Survey is deleted for ID: {}", surveyId);
                return false;
            }

            // Validate element names
            boolean isValid = answers.stream()
                    .allMatch(answer -> {
                        if (answer instanceof Map) {
                            Map<?, ?> answerMap = (Map<?, ?>) answer;

                            // Check if all keys are valid element names
                            return answerMap.keySet().stream()
                                    .allMatch(elementName ->
                                            survey.getPages().stream()
                                                    .flatMap(page -> page.getElements().stream())
                                                    .anyMatch(element -> element.getName().equals(elementName.toString()))
                                    );
                        }
                        log.warn("Invalid answer format for surveyId: {}", surveyId);
                        return false;
                    });

            if (!isValid) {
                log.warn("Invalid element names in answers for surveyId: {}", surveyId);
            }
            log.info("Survey and questions validated successfully for surveyId: {}", surveyId);
            return isValid;
        } catch (EntityNotFoundException e) {
            log.warn("Survey not found for ID: {}", surveyId);
            throw e;
        } catch (Exception e) {
            log.error("Error validating survey and questions for surveyId: {}", surveyId, e);
            throw new CustomRuntimeException("Error validating survey and questions", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    public String getCurrentUserId() {
        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();

        if (principal instanceof UserDetails) {
            return ((UserDetails) principal).getUsername();
        } else {
            return "guest";
        }
    }
}
