package com.example.surveysystembackend.DTO;

import lombok.Data;

import java.util.List;

@Data
public class AnswerDTO {
    private String questionId;
    private Object value;
}
