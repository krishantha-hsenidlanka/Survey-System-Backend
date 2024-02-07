package com.example.surveysystembackend.DTO;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ResponseDTO {
    private String id;
    private String userId;
    private String surveyId;
    private List<AnswerDTO> answers;
}
