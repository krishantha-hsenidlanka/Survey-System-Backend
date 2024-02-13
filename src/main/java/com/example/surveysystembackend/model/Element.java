package com.example.surveysystembackend.model;

import lombok.Data;
import org.springframework.data.annotation.Id;

import java.util.List;

@Data
public class Element {
    @Id
    private String id;
    private String type;
    private String name;
    private String title;
    private List<Object> choices;
    private Boolean isRequired;
}
