package com.example.surveysystembackend.repository;

import com.example.surveysystembackend.model.Response;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ResponseRepository extends MongoRepository<Response, String> {
    List<Response> findBySurveyId(String surveyId);
    List<Response> findByUserId(String userId);

}
