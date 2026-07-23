package com.fastappoint.dto;

public class UpdateServiceRequest {
    private String name;
    private Long durationSeconds;

    public UpdateServiceRequest() {
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Long getDurationSeconds() {
        return durationSeconds;
    }

    public void setDurationSeconds(Long durationSeconds) {
        this.durationSeconds = durationSeconds;
    }
}

