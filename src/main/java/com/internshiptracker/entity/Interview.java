package com.internshiptracker.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.Objects;

@Entity
@Table(name = "interviews")
public class Interview {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "application_id", nullable = false)
    private Application application;

    @Column(name = "interview_round", nullable = false, length = 100)
    private String interviewRound;

    @Column(name = "interview_date", nullable = false)
    private LocalDateTime interviewDate;

    @Enumerated(EnumType.STRING)
    @Column(name = "interview_mode", nullable = false, length = 50)
    private InterviewMode interviewMode = InterviewMode.ONLINE;

    @Column(name = "meeting_link", length = 500)
    private String meetingLink;

    @Column(name = "interviewer_name", length = 150)
    private String interviewerName;

    @Column(name = "preparation_notes", columnDefinition = "TEXT")
    private String preparationNotes;

    @Enumerated(EnumType.STRING)
    @Column(name = "outcome", nullable = false, length = 50)
    private InterviewOutcome outcome = InterviewOutcome.PENDING;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    public Interview() {
    }

    @PrePersist
    protected void onCreate() {
        if (this.createdAt == null) {
            this.createdAt = LocalDateTime.now();
        }
        if (this.interviewMode == null) {
            this.interviewMode = InterviewMode.ONLINE;
        }
        if (this.outcome == null) {
            this.outcome = InterviewOutcome.PENDING;
        }
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Application getApplication() {
        return application;
    }

    public void setApplication(Application application) {
        this.application = application;
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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Interview interview = (Interview) o;
        return Objects.equals(id, interview.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
