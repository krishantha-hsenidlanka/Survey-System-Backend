package com.example.surveysystembackend.controller;

import com.example.surveysystembackend.exception.CustomRuntimeException;
import com.example.surveysystembackend.service.admin.AdminDashboardService;
import com.example.surveysystembackend.service.log.LogService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@RestController
@PreAuthorize("hasRole('ADMIN')")
@RequestMapping("/api/admin")
public class AdminController {

    @Autowired
    private AdminDashboardService adminDashboardService;

    @Autowired
    private LogService logService;

    @GetMapping("/dashboard-summary")
    public ResponseEntity<?> getAdminDashboardSummary() {
        try {
            log.info("API hit: GET /api/admin/dashboard-summary");
            log.info("Fetching admin dashboard summary");
            Map<String, Long> summary = new HashMap<>();
            summary.put("userCount", adminDashboardService.getUserCount());
            summary.put("surveyCount", adminDashboardService.getSurveyCount());
            summary.put("activeSurveyCount", adminDashboardService.getActiveSurveyCount());
            summary.put("deletedSurveyCount", adminDashboardService.getDeletedSurveyCount());
            summary.put("enabledUserCount", adminDashboardService.getEnabledUserCount());
            summary.put("responsesCount", adminDashboardService.getResponsesCount());
            log.info("Admin dashboard summary fetched successfully");
            return ResponseEntity.ok(summary);
        } catch (Exception e) {
            log.error("Error fetching admin dashboard summary: {}", e.getMessage(), e);
            throw new CustomRuntimeException("Error fetching admin dashboard summary", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping("/logs")
    public ResponseEntity<String> getLogs() {
        try {
            log.info("API hit: GET /api/admin/logs");
            log.info("Fetching logs from LogService");
            String logs = logService.getConsoleLogs();
            log.info("Logs fetched successfully");
            return ResponseEntity.ok(logs);
        } catch (Exception e) {
            log.error("Error fetching logs: {}", e.getMessage(), e);
            throw new CustomRuntimeException("Error fetching logs", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}