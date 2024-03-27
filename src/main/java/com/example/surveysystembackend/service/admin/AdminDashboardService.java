package com.example.surveysystembackend.service.admin;

import com.example.surveysystembackend.exception.CustomRuntimeException;
import com.example.surveysystembackend.repository.ResponseRepository;
import com.example.surveysystembackend.repository.SurveyRepository;
import com.example.surveysystembackend.repository.UserRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class AdminDashboardService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private SurveyRepository surveyRepository;

    @Autowired
    private ResponseRepository responseRepository;

    public long getUserCount() {
        try {
            log.info("Getting user count");
            return userRepository.count();
        } catch (Exception e) {
            log.error("Error getting user count: {}", e.getMessage(), e);
            throw new CustomRuntimeException("Error getting user count", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    public long getSurveyCount() {
        try {
            log.info("Getting survey count");
            return surveyRepository.count();
        } catch (Exception e) {
            log.error("Error getting survey count: {}", e.getMessage(), e);
            throw new CustomRuntimeException("Error getting survey count", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    public long getDeletedSurveyCount() {
        try {
            log.info("Getting deleted survey count");
            return surveyRepository.countByDeletedTrue();
        } catch (Exception e) {
            log.error("Error getting deleted survey count: {}", e.getMessage(), e);
            throw new CustomRuntimeException("Error getting deleted survey count", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    public long getEnabledUserCount() {
        try {
            log.info("Getting enabled user count");
            return userRepository.findByEnabledTrue().size();
        } catch (Exception e) {
            log.error("Error getting enabled user count: {}", e.getMessage(), e);
            throw new CustomRuntimeException("Error getting enabled user count", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    public long getResponsesCount() {
        try {
            log.info("Getting responses count");
            return responseRepository.count();
        } catch (Exception e) {
            log.error("Error getting responses count: {}", e.getMessage(), e);
            throw new CustomRuntimeException("Error getting responses count", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    public long getActiveSurveyCount() {
        try {
            log.info("Getting active survey count");
            return surveyRepository.countByDeletedFalse();
        } catch (Exception e) {
            log.error("Error getting active survey count: {}", e.getMessage(), e);
            throw new CustomRuntimeException("Error getting active survey count", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}