package com.example.surveysystembackend.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;

import java.util.List;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Element {
    @Id
    private String id;
    private String type;
    private String name;
    private String title;
    private List<Object> choices;
    private Boolean isRequired;
    private String imageFit;
}
