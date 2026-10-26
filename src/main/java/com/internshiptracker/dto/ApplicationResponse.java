package com.internshiptracker.dto;

import com.internshiptracker.entity.Application;
import com.internshiptracker.entity.ApplicationStatus;
import com.internshiptracker.entity.Priority;
import com.internshiptracker.entity.WorkMode;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

public class ApplicationResponse {

    private Long id;
    private String companyName;
    private String jobRole;
    private String jobDescription;
    private String applicationUrl;
    private String companyWebsite;
    private String location;
    private WorkMode workMode;
    private String employmentType;
    private LocalDate applicationDate;
    private LocalDate deadline;
    private BigDecimal stipend;
    private ApplicationStatus status;
    private Priority priority;
    private String notes;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private int interviewCount;

    public ApplicationResponse() {
    }

    public static ApplicationResponse fromEntity(Application app) {
        ApplicationResponse resp = new ApplicationResponse();
        resp.setId(app.getId());
        resp.setCompanyName(app.getCompanyName());
        resp.setJobRole(app.getJobRole());
        resp.setJobDescription(app.getJobDescription());
        resp.setApplicationUrl(app.getApplicationUrl());
        resp.setCompanyWebsite(app.getCompanyWebsite());
        resp.setLocation(app.getLocation());
        resp.setWorkMode(app.getWorkMode());
        resp.setEmploymentType(app.getEmploymentType());
        resp.setApplicationDate(app.getApplicationDate());
        resp.setDeadline(app.getDeadline());
        resp.setStipend(app.getStipend());
        resp.setStatus(app.getStatus());
        resp.setPriority(app.getPriority());
        resp.setNotes(app.getNotes());
        resp.setCreatedAt(app.getCreatedAt());
        resp.setUpdatedAt(app.getUpdatedAt());
        resp.setInterviewCount(app.getInterviews() != null ? app.getInterviews().size() : 0);
        return resp;
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
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

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    public int getInterviewCount() {
        return interviewCount;
    }

    public void setInterviewCount(int interviewCount) {
        this.interviewCount = interviewCount;
    }
}
