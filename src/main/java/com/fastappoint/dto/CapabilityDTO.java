package com.fastappoint.dto;

import java.util.UUID;

public class CapabilityDTO {
    private UUID id;
    private UUID businessId;
    private String name;
    private String description;

    public CapabilityDTO() {
    }

    public CapabilityDTO(UUID id, UUID businessId, String name, String description) {
        this.id = id;
        this.businessId = businessId;
        this.name = name;
        this.description = description;
    }

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }

    public UUID getBusinessId() { return businessId; }
    public void setBusinessId(UUID businessId) { this.businessId = businessId; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
}

