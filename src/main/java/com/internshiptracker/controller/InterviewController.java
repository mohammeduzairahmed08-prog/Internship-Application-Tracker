package com.internshiptracker.controller;

import com.internshiptracker.dto.ApiResponse;
import com.internshiptracker.dto.InterviewRequest;
import com.internshiptracker.dto.InterviewResponse;
import com.internshiptracker.service.InterviewService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api")
public class InterviewController {

    private final InterviewService interviewService;

    public InterviewController(InterviewService interviewService) {
        this.interviewService = interviewService;
    }

    @GetMapping("/applications/{applicationId}/interviews")
    public ResponseEntity<ApiResponse<List<InterviewResponse>>> getInterviewsForApplication(
            @PathVariable Long applicationId) {
        List<InterviewResponse> interviews = interviewService.getInterviewsForApplication(applicationId);
        return ResponseEntity.ok(ApiResponse.ok(interviews));
    }

    @PostMapping("/applications/{applicationId}/interviews")
    public ResponseEntity<ApiResponse<InterviewResponse>> scheduleInterview(
            @PathVariable Long applicationId,
            @Valid @RequestBody InterviewRequest request) {
        InterviewResponse response = interviewService.scheduleInterview(applicationId, request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.ok("Interview scheduled successfully", response));
    }

    @PutMapping("/interviews/{id}")
    public ResponseEntity<ApiResponse<InterviewResponse>> updateInterview(
            @PathVariable Long id,
            @Valid @RequestBody InterviewRequest request) {
        InterviewResponse response = interviewService.updateInterview(id, request);
        return ResponseEntity.ok(ApiResponse.ok("Interview updated successfully", response));
    }

    @DeleteMapping("/interviews/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteInterview(@PathVariable Long id) {
        interviewService.deleteInterview(id);
        return ResponseEntity.ok(ApiResponse.message("Interview deleted successfully"));
    }
}
