package com.example.surveysystembackend.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.List;

@Document(collection = "responses")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Response {
    @Id
    private String id;
    private String userId;
    private String surveyId;
    private List<Answer> Answers;
}
