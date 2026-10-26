package com.internshiptracker.dto;

import com.internshiptracker.entity.ApplicationStatus;

public class StatusDistributionDto {

    private ApplicationStatus status;
    private String label;
    private long count;
    private String color;

    public StatusDistributionDto() {
    }

    public StatusDistributionDto(ApplicationStatus status, String label, long count, String color) {
        this.status = status;
        this.label = label;
        this.count = count;
        this.color = color;
    }

    public ApplicationStatus getStatus() {
        return status;
    }

    public void setStatus(ApplicationStatus status) {
        this.status = status;
    }

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public long getCount() {
        return count;
    }

    public void setCount(long count) {
        this.count = count;
    }

    public String getColor() {
        return color;
    }

    public void setColor(String color) {
        this.color = color;
    }
}
