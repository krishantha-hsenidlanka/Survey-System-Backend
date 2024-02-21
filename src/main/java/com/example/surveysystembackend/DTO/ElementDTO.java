package com.example.surveysystembackend.DTO;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ElementDTO {
    private String id;
    private String type;
    private String name;
    private String title;
    private List<Object> choices;
    private Boolean isRequired;
    private List<Object> rows;
    private List<Object> columns;
}