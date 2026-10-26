package com.internshiptracker.entity;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

public enum InterviewOutcome {
    PENDING("Pending"),
    PASSED("Passed"),
    FAILED("Failed");

    private final String displayName;

    InterviewOutcome(String displayName) {
        this.displayName = displayName;
    }

    @JsonValue
    public String getDisplayName() {
        return displayName;
    }

    @JsonCreator
    public static InterviewOutcome fromString(String text) {
        if (text == null || text.trim().isEmpty()) {
            return null;
        }
        String normalized = text.trim();
        for (InterviewOutcome outcome : InterviewOutcome.values()) {
            if (outcome.name().equalsIgnoreCase(normalized) || outcome.displayName.equalsIgnoreCase(normalized)) {
                return outcome;
            }
        }
        throw new IllegalArgumentException("Unknown InterviewOutcome: " + text);
    }
}
