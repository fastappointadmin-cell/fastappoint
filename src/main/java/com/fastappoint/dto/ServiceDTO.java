package com.fastappoint.dto;

import java.time.Duration;
import java.util.List;
import java.util.UUID;

public class ServiceDTO {
    private UUID id;
    private UUID businessId;
    private String name;
    private long durationSeconds;
    private List<ServiceRequirementDTO> requirements;

    public ServiceDTO() {}

    public ServiceDTO(UUID id, UUID businessId, String name, Duration duration) {
        this.id = id;
        this.businessId = businessId;
        this.name = name;
        this.durationSeconds = duration.getSeconds();
    }

    public ServiceDTO(UUID id, UUID businessId, String name, Duration duration, List<ServiceRequirementDTO> requirements) {
        this(id, businessId, name, duration);
        this.requirements = requirements;
    }

    // Getters and Setters
    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }

    public UUID getBusinessId() { return businessId; }
    public void setBusinessId(UUID businessId) { this.businessId = businessId; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public long getDurationSeconds() { return durationSeconds; }
    public void setDurationSeconds(long durationSeconds) { this.durationSeconds = durationSeconds; }

    public List<ServiceRequirementDTO> getRequirements() { return requirements; }
    public void setRequirements(List<ServiceRequirementDTO> requirements) { this.requirements = requirements; }
}