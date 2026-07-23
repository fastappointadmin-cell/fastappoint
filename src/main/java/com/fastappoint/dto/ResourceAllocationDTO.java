package com.fastappoint.dto;

import java.time.LocalDateTime;
import java.util.UUID;

public class ResourceAllocationDTO {
    private UUID id;
    private UUID appointmentId;
    private UUID resourceId;
    private UUID requirementId;
    private LocalDateTime startTime;
    private LocalDateTime endTime;

    public ResourceAllocationDTO() {
    }

    public ResourceAllocationDTO(UUID id, UUID appointmentId, UUID resourceId, UUID requirementId,
                                 LocalDateTime startTime, LocalDateTime endTime) {
        this.id = id;
        this.appointmentId = appointmentId;
        this.resourceId = resourceId;
        this.requirementId = requirementId;
        this.startTime = startTime;
        this.endTime = endTime;
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public UUID getAppointmentId() {
        return appointmentId;
    }

    public void setAppointmentId(UUID appointmentId) {
        this.appointmentId = appointmentId;
    }

    public UUID getResourceId() {
        return resourceId;
    }

    public void setResourceId(UUID resourceId) {
        this.resourceId = resourceId;
    }

    public UUID getRequirementId() {
        return requirementId;
    }

    public void setRequirementId(UUID requirementId) {
        this.requirementId = requirementId;
    }

    public LocalDateTime getStartTime() {
        return startTime;
    }

    public void setStartTime(LocalDateTime startTime) {
        this.startTime = startTime;
    }

    public LocalDateTime getEndTime() {
        return endTime;
    }

    public void setEndTime(LocalDateTime endTime) {
        this.endTime = endTime;
    }
}

