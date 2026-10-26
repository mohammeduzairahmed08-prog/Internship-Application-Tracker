package com.internshiptracker.entity;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

public enum ApplicationStatus {
    WISHLIST("Wishlist"),
    APPLIED("Applied"),
    ONLINE_ASSESSMENT("Online Assessment"),
    INTERVIEW_SCHEDULED("Interview Scheduled"),
    INTERVIEW_COMPLETED("Interview Completed"),
    OFFER_RECEIVED("Offer Received"),
    REJECTED("Rejected"),
    WITHDRAWN("Withdrawn");

    private final String displayName;

    ApplicationStatus(String displayName) {
        this.displayName = displayName;
    }

    @JsonValue
    public String getDisplayName() {
        return displayName;
    }

    @JsonCreator
    public static ApplicationStatus fromString(String text) {
        if (text == null || text.trim().isEmpty()) {
            return null;
        }
        String normalized = text.trim();
        for (ApplicationStatus status : ApplicationStatus.values()) {
            if (status.name().equalsIgnoreCase(normalized) ||
                status.displayName.equalsIgnoreCase(normalized) ||
                status.name().replace("_", " ").equalsIgnoreCase(normalized)) {
                return status;
            }
        }
        throw new IllegalArgumentException("Unknown ApplicationStatus: " + text);
    }
}
