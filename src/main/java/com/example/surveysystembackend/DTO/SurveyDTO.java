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
public class SurveyDTO {
    private String id;
    private String title;
    private List<ElementDTO> elements;
    private String ownerId;
    private boolean isPublic;
    private boolean deleted;
}
