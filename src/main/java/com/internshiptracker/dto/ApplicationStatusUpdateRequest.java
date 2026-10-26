package com.internshiptracker.dto;

import com.internshiptracker.entity.ApplicationStatus;
import jakarta.validation.constraints.NotNull;

public class ApplicationStatusUpdateRequest {

    @NotNull(message = "Status is required")
    private ApplicationStatus status;

    public ApplicationStatusUpdateRequest() {
    }

    public ApplicationStatusUpdateRequest(ApplicationStatus status) {
        this.status = status;
    }

    public ApplicationStatus getStatus() {
        return status;
    }

    public void setStatus(ApplicationStatus status) {
        this.status = status;
    }
}
