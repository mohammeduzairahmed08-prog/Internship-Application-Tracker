package com.internshiptracker.dto;

import com.internshiptracker.entity.InterviewMode;
import com.internshiptracker.entity.InterviewOutcome;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.LocalDateTime;

public class InterviewRequest {

    @NotBlank(message = "Interview round is required (e.g. Technical Round 1, HR, Screening)")
    @Size(max = 100, message = "Interview round cannot exceed 100 characters")
    private String interviewRound;

    @NotNull(message = "Interview date and time is required")
    private LocalDateTime interviewDate;

    private InterviewMode interviewMode = InterviewMode.ONLINE;

    @Size(max = 500, message = "Meeting link cannot exceed 500 characters")
    private String meetingLink;

    @Size(max = 150, message = "Interviewer name cannot exceed 150 characters")
    private String interviewerName;

    private String preparationNotes;

    private InterviewOutcome outcome = InterviewOutcome.PENDING;

    public InterviewRequest() {
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
}
