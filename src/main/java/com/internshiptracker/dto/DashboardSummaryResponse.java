package com.internshiptracker.dto;

public class DashboardSummaryResponse {

    private long totalApplications;
    private long applicationsSubmitted;
    private long applicationsInAssessment;
    private long interviewsScheduled;
    private long offersReceived;
    private long rejectedApplications;
    private long upcomingDeadlines;
    private long upcomingInterviews;
    private double successRate;

    public DashboardSummaryResponse() {
    }

    public long getTotalApplications() {
        return totalApplications;
    }

    public void setTotalApplications(long totalApplications) {
        this.totalApplications = totalApplications;
    }

    public long getApplicationsSubmitted() {
        return applicationsSubmitted;
    }

    public void setApplicationsSubmitted(long applicationsSubmitted) {
        this.applicationsSubmitted = applicationsSubmitted;
    }

    public long getApplicationsInAssessment() {
        return applicationsInAssessment;
    }

    public void setApplicationsInAssessment(long applicationsInAssessment) {
        this.applicationsInAssessment = applicationsInAssessment;
    }

    public long getInterviewsScheduled() {
        return interviewsScheduled;
    }

    public void setInterviewsScheduled(long interviewsScheduled) {
        this.interviewsScheduled = interviewsScheduled;
    }

    public long getOffersReceived() {
        return offersReceived;
    }

    public void setOffersReceived(long offersReceived) {
        this.offersReceived = offersReceived;
    }

    public long getRejectedApplications() {
        return rejectedApplications;
    }

    public void setRejectedApplications(long rejectedApplications) {
        this.rejectedApplications = rejectedApplications;
    }

    public long getUpcomingDeadlines() {
        return upcomingDeadlines;
    }

    public void setUpcomingDeadlines(long upcomingDeadlines) {
        this.upcomingDeadlines = upcomingDeadlines;
    }

    public long getUpcomingInterviews() {
        return upcomingInterviews;
    }

    public void setUpcomingInterviews(long upcomingInterviews) {
        this.upcomingInterviews = upcomingInterviews;
    }

    public double getSuccessRate() {
        return successRate;
    }

    public void setSuccessRate(double successRate) {
        this.successRate = successRate;
    }
}
