package com.internshiptracker.dto;

import com.internshiptracker.entity.ApplicationStatus;
import com.internshiptracker.entity.Priority;

import java.time.LocalDate;

public class UpcomingDeadlineDto {

    private Long applicationId;
    private String companyName;
    private String jobRole;
    private LocalDate deadline;
    private long daysRemaining;
    private boolean overdue;
    private ApplicationStatus status;
    private Priority priority;

    public UpcomingDeadlineDto() {
    }

    public UpcomingDeadlineDto(Long applicationId, String companyName, String jobRole,
                               LocalDate deadline, long daysRemaining, boolean overdue,
                               ApplicationStatus status, Priority priority) {
        this.applicationId = applicationId;
        this.companyName = companyName;
        this.jobRole = jobRole;
        this.deadline = deadline;
        this.daysRemaining = daysRemaining;
        this.overdue = overdue;
        this.status = status;
        this.priority = priority;
    }

    public Long getApplicationId() {
        return applicationId;
    }

    public void setApplicationId(Long applicationId) {
        this.applicationId = applicationId;
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

    public LocalDate getDeadline() {
        return deadline;
    }

    public void setDeadline(LocalDate deadline) {
        this.deadline = deadline;
    }

    public long getDaysRemaining() {
        return daysRemaining;
    }

    public void setDaysRemaining(long daysRemaining) {
        this.daysRemaining = daysRemaining;
    }

    public boolean isOverdue() {
        return overdue;
    }

    public void setOverdue(boolean overdue) {
        this.overdue = overdue;
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
}
