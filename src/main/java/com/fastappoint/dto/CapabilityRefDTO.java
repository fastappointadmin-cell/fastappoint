package com.fastappoint.dto;

import java.util.UUID;

/**
 * Lightweight reference to a capability used in nested DTOs
 */
public class CapabilityRefDTO {
    private UUID id;
    private String name;

    public CapabilityRefDTO() {
    }

    public CapabilityRefDTO(UUID id, String name) {
        this.id = id;
        this.name = name;
    }

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
}

