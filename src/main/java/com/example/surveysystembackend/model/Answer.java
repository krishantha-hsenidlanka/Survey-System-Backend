package com.example.surveysystembackend.model;

import org.springframework.data.annotation.Id;

public class Answer {
    @Id
    private String id;
    private String questionId;
    private String text;
    private String userId;
}
