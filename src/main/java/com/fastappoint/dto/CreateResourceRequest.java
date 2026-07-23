package com.fastappoint.dto;

import java.util.Set;
import java.util.UUID;

public class CreateResourceRequest {
    private String name;
    private UUID typeId;
    private Integer capacity;
    private Set<UUID> capabilityIds;

    public CreateResourceRequest() {
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public UUID getTypeId() {
        return typeId;
    }

    public void setTypeId(UUID typeId) {
        this.typeId = typeId;
    }

    public Integer getCapacity() {
        return capacity;
    }

    public void setCapacity(Integer capacity) {
        this.capacity = capacity;
    }

    public Set<UUID> getCapabilityIds() {
        return capabilityIds;
    }

    public void setCapabilityIds(Set<UUID> capabilityIds) {
        this.capabilityIds = capabilityIds;
    }
}

