package com.example.surveysystembackend.model;

import lombok.Data;
import org.springframework.data.annotation.Id;

import java.util.List;

@Data
public class Question {
    @Id
    private String id;
    private String text;
    private String type;
    private List<String> options;
}
