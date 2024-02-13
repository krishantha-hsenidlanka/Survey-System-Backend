package com.example.surveysystembackend.DTO;

import lombok.Data;

import java.util.List;

@Data
public class ElementDTO {
    private String id;
    private String text;
    private String type;
    private List<String> options;
}
