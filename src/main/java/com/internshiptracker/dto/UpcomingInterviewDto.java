package com.internshiptracker.dto;

import com.internshiptracker.entity.InterviewMode;
import com.internshiptracker.entity.InterviewOutcome;

import java.time.LocalDateTime;

public class UpcomingInterviewDto {

    private Long interviewId;
    private Long applicationId;
    private String companyName;
    private String jobRole;
    private String interviewRound;
    private LocalDateTime interviewDate;
    private InterviewMode interviewMode;
    private String meetingLink;
    private String interviewerName;
    private InterviewOutcome outcome;

    public UpcomingInterviewDto() {
    }

    public UpcomingInterviewDto(Long interviewId, Long applicationId, String companyName,
                                String jobRole, String interviewRound, LocalDateTime interviewDate,
                                InterviewMode interviewMode, String meetingLink,
                                String interviewerName, InterviewOutcome outcome) {
        this.interviewId = interviewId;
        this.applicationId = applicationId;
        this.companyName = companyName;
        this.jobRole = jobRole;
        this.interviewRound = interviewRound;
        this.interviewDate = interviewDate;
        this.interviewMode = interviewMode;
        this.meetingLink = meetingLink;
        this.interviewerName = interviewerName;
        this.outcome = outcome;
    }

    public Long getInterviewId() {
        return interviewId;
    }

    public void setInterviewId(Long interviewId) {
        this.interviewId = interviewId;
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

    public InterviewOutcome getOutcome() {
        return outcome;
    }

    public void setOutcome(InterviewOutcome outcome) {
        this.outcome = outcome;
    }
}
