package com.example.surveysystembackend.service.response;

import com.example.surveysystembackend.DTO.ResponseDTO;
import com.example.surveysystembackend.model.Response;
import com.example.surveysystembackend.model.Survey;
import com.example.surveysystembackend.repository.ResponseRepository;
import com.example.surveysystembackend.repository.SurveyRepository;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

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
        // Map ResponseDTO to Response entity
        Response response = modelMapper.map(responseDTO, Response.class);

        // Get the current logged-in user's ID
        String userId = getCurrentUserId();
        response.setUserId(userId);

        // Ensure new response has null id before saving
        response.setId(null);

        // Validate survey ID and question IDs
        if (!isValidSurveyAndQuestions(response.getSurveyId(), response.getAnswers())) {
            throw new IllegalArgumentException("Invalid survey or question IDs or Deleted");
        }

        // Save the response to the repository
        response = responseRepository.save(response);

        return modelMapper.map(response, ResponseDTO.class);
    }

    @Override
    public ResponseDTO getResponseById(String responseId) {
        Optional<Response> optionalResponse = responseRepository.findById(responseId);
        return optionalResponse.map(response -> modelMapper.map(response, ResponseDTO.class)).orElse(null);
    }

    @Override
    public List<ResponseDTO> getResponsesBySurveyId(String surveyId) {
        return responseRepository.findBySurveyId(surveyId).stream()
                .map(response -> modelMapper.map(response, ResponseDTO.class))
                .collect(Collectors.toList());
    }

    @Override
    public List<ResponseDTO> getResponsesByUserId(String userId) {
        List<Response> responsesByUserId = responseRepository.findByUserId(userId);
        return responsesByUserId.stream()
                .map(response -> modelMapper.map(response, ResponseDTO.class))
                .collect(Collectors.toList());
    }

    private boolean isValidSurveyAndQuestions(String surveyId, List<Object> answers) {
        Optional<Survey> surveyOptional = surveyRepository.findById(surveyId);

        if (surveyOptional.isPresent()) {
            Survey survey = surveyOptional.get();

            // Validate survey ID
            if (survey.isDeleted()) {
                return false;  // Survey is marked as deleted
            }

            // Validate element names
            return answers.stream()
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
                        return false;
                    });
        }

        return false;
    }



    private String getCurrentUserId() {
        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();

        if (principal instanceof UserDetails) {
            return ((UserDetails) principal).getUsername();
        } else {
            return "guest";
        }
    }

}
