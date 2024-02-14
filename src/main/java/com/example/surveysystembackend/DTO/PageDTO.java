package com.example.surveysystembackend.DTO;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PageDTO {
    private String name;
    private String title;
    private String description;
    private List<ElementDTO> elements;
}
