package com.fastappoint.dto;

import java.util.UUID;

public class ResourceTypeDTO {
    private UUID id;
    private UUID businessId;
    private String name;
    private java.util.List<ResourceAttributeDefinitionDTO> attributeDefinitions;

    public ResourceTypeDTO() {
    }

    public ResourceTypeDTO(
            UUID id,
            UUID businessId,
            String name,
            java.util.List<ResourceAttributeDefinitionDTO> attributeDefinitions
    ) {
        this.id = id;
        this.businessId = businessId;
        this.name = name;
        this.attributeDefinitions = attributeDefinitions;
    }

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }

    public UUID getBusinessId() { return businessId; }
    public void setBusinessId(UUID businessId) { this.businessId = businessId; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public java.util.List<ResourceAttributeDefinitionDTO> getAttributeDefinitions() { return attributeDefinitions; }
    public void setAttributeDefinitions(java.util.List<ResourceAttributeDefinitionDTO> attributeDefinitions) {
        this.attributeDefinitions = attributeDefinitions;
    }
}
