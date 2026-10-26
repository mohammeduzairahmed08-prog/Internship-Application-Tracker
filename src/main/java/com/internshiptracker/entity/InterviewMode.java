package com.internshiptracker.entity;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

public enum InterviewMode {
    ONLINE("Online"),
    PHONE("Phone"),
    IN_PERSON("In-person");

    private final String displayName;

    InterviewMode(String displayName) {
        this.displayName = displayName;
    }

    @JsonValue
    public String getDisplayName() {
        return displayName;
    }

    @JsonCreator
    public static InterviewMode fromString(String text) {
        if (text == null || text.trim().isEmpty()) {
            return null;
        }
        String normalized = text.trim();
        for (InterviewMode mode : InterviewMode.values()) {
            if (mode.name().equalsIgnoreCase(normalized) ||
                mode.displayName.equalsIgnoreCase(normalized) ||
                mode.name().replace("_", "-").equalsIgnoreCase(normalized) ||
                mode.name().replace("_", " ").equalsIgnoreCase(normalized)) {
                return mode;
            }
        }
        throw new IllegalArgumentException("Unknown InterviewMode: " + text);
    }
}
