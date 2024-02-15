package com.example.surveysystembackend.repository;

import com.example.surveysystembackend.model.Response;
import com.example.surveysystembackend.model.Survey;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SurveyRepository extends MongoRepository<Survey, String> {

    List<Survey> findByOwnerId(String ownerId);

    List<Survey> findByOwnerIdAndDeletedFalse(String ownerId);


}

