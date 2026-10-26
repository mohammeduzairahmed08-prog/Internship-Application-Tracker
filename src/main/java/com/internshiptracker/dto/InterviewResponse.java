package com.internshiptracker.dto;

import com.internshiptracker.entity.Interview;
import com.internshiptracker.entity.InterviewMode;
import com.internshiptracker.entity.InterviewOutcome;

import java.time.LocalDateTime;

public class InterviewResponse {

    private Long id;
    private Long applicationId;
    private String companyName;
    private String jobRole;
    private String interviewRound;
    private LocalDateTime interviewDate;
    private InterviewMode interviewMode;
    private String meetingLink;
    private String interviewerName;
    private String preparationNotes;
    private InterviewOutcome outcome;
    private LocalDateTime createdAt;

    public InterviewResponse() {
    }

    public static InterviewResponse fromEntity(Interview interview) {
        InterviewResponse resp = new InterviewResponse();
        resp.setId(interview.getId());
        if (interview.getApplication() != null) {
            resp.setApplicationId(interview.getApplication().getId());
            resp.setCompanyName(interview.getApplication().getCompanyName());
            resp.setJobRole(interview.getApplication().getJobRole());
        }
        resp.setInterviewRound(interview.getInterviewRound());
        resp.setInterviewDate(interview.getInterviewDate());
        resp.setInterviewMode(interview.getInterviewMode());
        resp.setMeetingLink(interview.getMeetingLink());
        resp.setInterviewerName(interview.getInterviewerName());
        resp.setPreparationNotes(interview.getPreparationNotes());
        resp.setOutcome(interview.getOutcome());
        resp.setCreatedAt(interview.getCreatedAt());
        return resp;
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
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

    public String getInterviewRound() {
        return interviewRound;
    }

    public void setInterviewRound(String interviewRound) {
        this.interviewRound = interviewRound;
    }

    public LocalDateTime getInterviewDate() {
        return interviewDate;
    }

    public void setInterviewDate(LocalDateTime interviewDate) {
        this.interviewDate = interviewDate;
    }

    public InterviewMode getInterviewMode() {
        return interviewMode;
    }

    public void setInterviewMode(InterviewMode interviewMode) {
        this.interviewMode = interviewMode;
    }

    public String getMeetingLink() {
        return meetingLink;
    }

    public void setMeetingLink(String meetingLink) {
        this.meetingLink = meetingLink;
    }

    public String getInterviewerName() {
        return interviewerName;
    }

    public void setInterviewerName(String interviewerName) {
        this.interviewerName = interviewerName;
    }

    public String getPreparationNotes() {
        return preparationNotes;
    }

    public void setPreparationNotes(String preparationNotes) {
        this.preparationNotes = preparationNotes;
    }

    public InterviewOutcome getOutcome() {
        return outcome;
    }

    public void setOutcome(InterviewOutcome outcome) {
        this.outcome = outcome;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}
