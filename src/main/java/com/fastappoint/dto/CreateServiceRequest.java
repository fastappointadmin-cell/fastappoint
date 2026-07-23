package com.fastappoint.dto;

public class CreateServiceRequest {
    private String name;
    private long durationSeconds;

    public CreateServiceRequest() {}

    // Getters and Setters
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public long getDurationSeconds() { return durationSeconds; }
    public void setDurationSeconds(long durationSeconds) { this.durationSeconds = durationSeconds; }
}