package com.example.surveysystembackend.service.admin;

import com.example.surveysystembackend.repository.ResponseRepository;
import com.example.surveysystembackend.repository.SurveyRepository;
import com.example.surveysystembackend.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class AdminDashboardService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private SurveyRepository surveyRepository;

    @Autowired
    private ResponseRepository responseRepository;

    public long getUserCount() {
        return userRepository.count();
    }

    public long getSurveyCount() {
        return surveyRepository.count();
    }

    public long getDeletedSurveyCount() {
        return surveyRepository.countByDeletedTrue();
    }

    public long getEnabledUserCount() {
        return userRepository.findByEnabledTrue().size();
    }

    public long getResponsesCount() {
        return responseRepository.count();
    }

    public long getActiveSurveyCount() {
        return surveyRepository.countByDeletedFalse();
    }

}
