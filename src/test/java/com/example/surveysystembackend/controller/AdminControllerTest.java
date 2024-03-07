package com.example.surveysystembackend.controller;
import com.example.surveysystembackend.service.admin.AdminDashboardService;
import com.example.surveysystembackend.service.log.LogService;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.ResponseEntity;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

@Slf4j
@SpringBootTest
class AdminControllerTest {

    @Mock
    private AdminDashboardService adminDashboardService;

    @Mock
    private LogService logService;

    @InjectMocks
    private AdminController adminController;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testGetAdminDashboardSummary() {
        // Arrange
        log.info("Setting up test for getAdminDashboardSummary");

        Map<String, Long> expectedSummary = new HashMap<>();
        expectedSummary.put("userCount", 10L);
        expectedSummary.put("surveyCount", 20L);
        expectedSummary.put("activeSurveyCount", 15L);
        expectedSummary.put("deletedSurveyCount", 5L);
        expectedSummary.put("enabledUserCount", 8L);
        expectedSummary.put("responsesCount", 30L);

        when(adminDashboardService.getUserCount()).thenReturn(10L);
        when(adminDashboardService.getSurveyCount()).thenReturn(20L);
        when(adminDashboardService.getActiveSurveyCount()).thenReturn(15L);
        when(adminDashboardService.getDeletedSurveyCount()).thenReturn(5L);
        when(adminDashboardService.getEnabledUserCount()).thenReturn(8L);
        when(adminDashboardService.getResponsesCount()).thenReturn(30L);

        // Act
        log.info("Executing getAdminDashboardSummary method");
        ResponseEntity<?> responseEntity = adminController.getAdminDashboardSummary();

        // Assert
        log.info("Verifying test results");
        assertEquals(expectedSummary, responseEntity.getBody());

        // Log additional information
        log.info("User count: {}", adminDashboardService.getUserCount());
        log.info("Survey count: {}", adminDashboardService.getSurveyCount());
        log.info("Active survey count: {}", adminDashboardService.getActiveSurveyCount());

        // Completion of the test
        log.info("Test for getAdminDashboardSummary passed successfully.");
    }

    @Test
    void testGetLogs() {
        // Arrange
        log.info("Setting up test for getLogs");

        String expectedLogs = "Mocked logs from LogService";
        when(logService.getConsoleLogs()).thenReturn(expectedLogs);

        // Act
        log.info("Executing getLogs method");
        ResponseEntity<String> responseEntity = adminController.getLogs();

        // Assert
        log.info("Verifying test results");
        assertEquals(expectedLogs, responseEntity.getBody());

        // Completion of the test
        log.info("Test for getLogs passed successfully.");
    }
}
