package com.internshiptracker.controller;

import com.internshiptracker.dto.*;
import com.internshiptracker.service.DashboardService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/dashboard")
public class DashboardController {

    private final DashboardService dashboardService;

    public DashboardController(DashboardService dashboardService) {
        this.dashboardService = dashboardService;
    }

    @GetMapping("/summary")
    public ResponseEntity<ApiResponse<DashboardSummaryResponse>> getSummary() {
        DashboardSummaryResponse summary = dashboardService.getSummary();
        return ResponseEntity.ok(ApiResponse.ok(summary));
    }

    @GetMapping("/status-distribution")
    public ResponseEntity<ApiResponse<List<StatusDistributionDto>>> getStatusDistribution() {
        List<StatusDistributionDto> distribution = dashboardService.getStatusDistribution();
        return ResponseEntity.ok(ApiResponse.ok(distribution));
    }

    @GetMapping("/application-trends")
    public ResponseEntity<ApiResponse<List<ApplicationTrendDto>>> getApplicationTrends() {
        List<ApplicationTrendDto> trends = dashboardService.getApplicationTrends();
        return ResponseEntity.ok(ApiResponse.ok(trends));
    }

    @GetMapping("/upcoming-deadlines")
    public ResponseEntity<ApiResponse<List<UpcomingDeadlineDto>>> getUpcomingDeadlines() {
        List<UpcomingDeadlineDto> deadlines = dashboardService.getUpcomingDeadlines();
        return ResponseEntity.ok(ApiResponse.ok(deadlines));
    }

    @GetMapping("/upcoming-interviews")
    public ResponseEntity<ApiResponse<List<UpcomingInterviewDto>>> getUpcomingInterviews() {
        List<UpcomingInterviewDto> interviews = dashboardService.getUpcomingInterviews();
        return ResponseEntity.ok(ApiResponse.ok(interviews));
    }
}
