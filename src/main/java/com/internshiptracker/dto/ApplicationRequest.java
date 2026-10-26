package com.internshiptracker.dto;

import com.internshiptracker.entity.ApplicationStatus;
import com.internshiptracker.entity.Priority;
import com.internshiptracker.entity.WorkMode;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.time.LocalDate;

public class ApplicationRequest {

    @NotBlank(message = "Company name is required")
    @Size(max = 150, message = "Company name cannot exceed 150 characters")
    private String companyName;

    @NotBlank(message = "Job/internship role is required")
    @Size(max = 150, message = "Job role cannot exceed 150 characters")
    private String jobRole;

    private String jobDescription;

    @Size(max = 500, message = "Application URL cannot exceed 500 characters")
    private String applicationUrl;

    @Size(max = 255, message = "Company website cannot exceed 255 characters")
    private String companyWebsite;

    @Size(max = 150, message = "Location cannot exceed 150 characters")
    private String location;

    private WorkMode workMode = WorkMode.REMOTE;

    private String employmentType = "INTERNSHIP";

    @NotNull(message = "Application date is required")
    private LocalDate applicationDate;

    private LocalDate deadline;

    @DecimalMin(value = "0.0", inclusive = true, message = "Expected stipend cannot be negative")
    private BigDecimal stipend = BigDecimal.ZERO;

    private ApplicationStatus status = ApplicationStatus.APPLIED;

    private Priority priority = Priority.MEDIUM;

    private String notes;

    public ApplicationRequest() {
    }

    public String getCompanyName() {
        return companyName;
    }

    public void setCompanyName(String companyName) {
        this.companyName = companyName;
    }

    public String getJobRole() {
        return jobRole;
    }

    public void setJobRole(String jobRole) {
        this.jobRole = jobRole;
    }

    public String getJobDescription() {
        return jobDescription;
    }

    public void setJobDescription(String jobDescription) {
        this.jobDescription = jobDescription;
    }

    public String getApplicationUrl() {
        return applicationUrl;
    }

    public void setApplicationUrl(String applicationUrl) {
        this.applicationUrl = applicationUrl;
    }

    public String getCompanyWebsite() {
        return companyWebsite;
    }

    public void setCompanyWebsite(String companyWebsite) {
        this.companyWebsite = companyWebsite;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public WorkMode getWorkMode() {
        return workMode;
    }

    public void setWorkMode(WorkMode workMode) {
        this.workMode = workMode;
    }

    public String getEmploymentType() {
        return employmentType;
    }

    public void setEmploymentType(String employmentType) {
        this.employmentType = employmentType;
    }

    public LocalDate getApplicationDate() {
        return applicationDate;
    }

    public void setApplicationDate(LocalDate applicationDate) {
        this.applicationDate = applicationDate;
    }

    public LocalDate getDeadline() {
        return deadline;
    }

    public void setDeadline(LocalDate deadline) {
        this.deadline = deadline;
    }

    public BigDecimal getStipend() {
        return stipend;
    }

    public void setStipend(BigDecimal stipend) {
        this.stipend = stipend;
    }

    public ApplicationStatus getStatus() {
        return status;
    }

    public void setStatus(ApplicationStatus status) {
        this.status = status;
    }

    public Priority getPriority() {
        return priority;
    }

    public void setPriority(Priority priority) {
        this.priority = priority;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }
}
