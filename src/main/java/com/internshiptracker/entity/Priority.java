package com.internshiptracker.entity;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

public enum Priority {
    LOW("Low"),
    MEDIUM("Medium"),
    HIGH("High");

    private final String displayName;

    Priority(String displayName) {
        this.displayName = displayName;
    }

    @JsonValue
    public String getDisplayName() {
        return displayName;
    }

    @JsonCreator
    public static Priority fromString(String text) {
        if (text == null || text.trim().isEmpty()) {
            return null;
        }
        String normalized = text.trim();
        for (Priority p : Priority.values()) {
            if (p.name().equalsIgnoreCase(normalized) || p.displayName.equalsIgnoreCase(normalized)) {
                return p;
            }
        }
        throw new IllegalArgumentException("Unknown Priority: " + text);
    }
}
