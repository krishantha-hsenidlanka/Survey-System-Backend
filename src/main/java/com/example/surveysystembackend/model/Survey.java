package com.example.surveysystembackend.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.HashSet;
import java.util.List;
import java.util.Set;


@Document(collection = "surveys")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Survey {
    @Id
    private String id;
    private String title;
    private List<Question> questions;
    private String ownerId;
    private Set<String> editAccessUserIds = new HashSet<>();
    private boolean isPublic;
    private boolean deleted;

}
