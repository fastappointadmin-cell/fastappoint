package com.fastappoint.dto;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.UUID;

public class ResourceAvailabilityDTO {
    private UUID id;
    private UUID resourceId;
    private LocalDate date;
    private LocalTime startTime;
    private LocalTime endTime;

    public ResourceAvailabilityDTO() {
    }

    public ResourceAvailabilityDTO(UUID id, UUID resourceId, LocalDate date, LocalTime startTime, LocalTime endTime) {
        this.id = id;
        this.resourceId = resourceId;
        this.date = date;
        this.startTime = startTime;
        this.endTime = endTime;
    }

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }

    public UUID getResourceId() { return resourceId; }
    public void setResourceId(UUID resourceId) { this.resourceId = resourceId; }

    public LocalDate getDate() { return date; }
    public void setDate(LocalDate date) { this.date = date; }

    public LocalTime getStartTime() { return startTime; }
    public void setStartTime(LocalTime startTime) { this.startTime = startTime; }

    public LocalTime getEndTime() { return endTime; }
    public void setEndTime(LocalTime endTime) { this.endTime = endTime; }
}
