package com.example.surveysystembackend.DTO.Survey;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Set;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SurveyDTO {
    private String id;
    private String title;
    private String description;
    private List<PageDTO> pages;
    private String ownerId;
    private Set<String> editAccessUserIds;
    private boolean isPublic;
    private boolean deleted;
}