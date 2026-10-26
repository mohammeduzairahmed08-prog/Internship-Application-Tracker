package com.internshiptracker.entity;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

public enum WorkMode {
    REMOTE("Remote"),
    HYBRID("Hybrid"),
    ON_SITE("On-site");

    private final String displayName;

    WorkMode(String displayName) {
        this.displayName = displayName;
    }

    @JsonValue
    public String getDisplayName() {
        return displayName;
    }

    @JsonCreator
    public static WorkMode fromString(String text) {
        if (text == null || text.trim().isEmpty()) {
            return null;
        }
        String normalized = text.trim();
        for (WorkMode mode : WorkMode.values()) {
            if (mode.name().equalsIgnoreCase(normalized) ||
                mode.displayName.equalsIgnoreCase(normalized) ||
                mode.name().replace("_", "-").equalsIgnoreCase(normalized) ||
                mode.name().replace("_", " ").equalsIgnoreCase(normalized)) {
                return mode;
            }
        }
        throw new IllegalArgumentException("Unknown WorkMode: " + text);
    }
}
