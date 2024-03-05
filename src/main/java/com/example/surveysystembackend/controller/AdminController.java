package com.example.surveysystembackend.controller;

import com.example.surveysystembackend.service.admin.AdminDashboardService;
import com.example.surveysystembackend.service.log.LogService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
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
    }

    @GetMapping("/logs")
    public ResponseEntity<String> getLogs() {
        log.info("Fetching logs from LogService");
        String logs = logService.getConsoleLogs();
        log.info("Logs fetched successfully");
        return ResponseEntity.ok(logs);
    }
}
