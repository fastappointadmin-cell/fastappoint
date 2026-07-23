package com.fastappoint.dto;

import java.util.UUID;

public class ResourceTypeDTO {
    private UUID id;
    private UUID businessId;
    private String name;

    public ResourceTypeDTO() {
    }

    public ResourceTypeDTO(UUID id, UUID businessId, String name) {
        this.id = id;
        this.businessId = businessId;
        this.name = name;
    }

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }

    public UUID getBusinessId() { return businessId; }
    public void setBusinessId(UUID businessId) { this.businessId = businessId; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
}

