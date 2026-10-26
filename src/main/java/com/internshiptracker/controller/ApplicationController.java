package com.internshiptracker.controller;

import com.internshiptracker.dto.ApiResponse;
import com.internshiptracker.dto.ApplicationRequest;
import com.internshiptracker.dto.ApplicationResponse;
import com.internshiptracker.dto.ApplicationStatusUpdateRequest;
import com.internshiptracker.entity.ApplicationStatus;
import com.internshiptracker.entity.WorkMode;
import com.internshiptracker.service.ApplicationService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;

@RestController
@RequestMapping("/api/applications")
public class ApplicationController {

    private final ApplicationService applicationService;

    public ApplicationController(ApplicationService applicationService) {
        this.applicationService = applicationService;
    }

    @GetMapping
    public ResponseEntity<ApiResponse<Page<ApplicationResponse>>> getApplications(
            @RequestParam(required = false) String search,
            @RequestParam(required = false) ApplicationStatus status,
            @RequestParam(required = false) WorkMode workMode,
            @RequestParam(required = false) String location,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fromDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate toDate,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "desc") String direction) {

        Sort sort = direction.equalsIgnoreCase("asc") ? Sort.by(sortBy).ascending() : Sort.by(sortBy).descending();
        Pageable pageable = PageRequest.of(page, size, sort);

        Page<ApplicationResponse> applications = applicationService.getApplications(
                search, status, workMode, location, fromDate, toDate, pageable
        );

        return ResponseEntity.ok(ApiResponse.ok(applications));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<ApplicationResponse>> getApplicationById(@PathVariable Long id) {
        ApplicationResponse response = applicationService.getApplicationById(id);
        return ResponseEntity.ok(ApiResponse.ok(response));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<ApplicationResponse>> createApplication(@Valid @RequestBody ApplicationRequest request) {
        ApplicationResponse response = applicationService.createApplication(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.ok("Application created successfully", response));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<ApplicationResponse>> updateApplication(
            @PathVariable Long id,
            @Valid @RequestBody ApplicationRequest request) {
        ApplicationResponse response = applicationService.updateApplication(id, request);
        return ResponseEntity.ok(ApiResponse.ok("Application updated successfully", response));
    }

    @PatchMapping("/{id}/status")
    public ResponseEntity<ApiResponse<ApplicationResponse>> updateStatus(
            @PathVariable Long id,
            @Valid @RequestBody ApplicationStatusUpdateRequest request) {
        ApplicationResponse response = applicationService.updateStatus(id, request.getStatus());
        return ResponseEntity.ok(ApiResponse.ok("Application status updated successfully", response));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteApplication(@PathVariable Long id) {
        applicationService.deleteApplication(id);
        return ResponseEntity.ok(ApiResponse.message("Application deleted successfully"));
    }
}
