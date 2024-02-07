package com.example.surveysystembackend.model;

import lombok.Data;
import org.springframework.data.annotation.Id;

import java.util.List;

@Data
public class Answer {
    @Id
    private String questionId;
    private List<String> text;
}
