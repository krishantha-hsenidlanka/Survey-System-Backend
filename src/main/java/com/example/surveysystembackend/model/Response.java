package com.example.surveysystembackend.model;

import org.springframework.data.annotation.Id;

import java.util.List;

public class Response {
    @Id
    private String id;
    private String user;
    private String surveyId;
    private List<String> answersIds;
}
