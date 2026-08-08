package com.fastappoint.dto;

import com.fastappoint.domain.ResourceAttributeType;

import java.util.List;
import java.util.UUID;

public class ResourceAttributeDefinitionDTO {
    private UUID id;
    private UUID resourceTypeId;
    private String name;
    private ResourceAttributeType type;
    private boolean required;
    private List<String> options;

    public ResourceAttributeDefinitionDTO() {
    }

    public ResourceAttributeDefinitionDTO(
            UUID id,
            UUID resourceTypeId,
            String name,
            ResourceAttributeType type,
            boolean required,
            List<String> options
    ) {
        this.id = id;
        this.resourceTypeId = resourceTypeId;
        this.name = name;
        this.type = type;
        this.required = required;
        this.options = options;
    }

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }
    public UUID getResourceTypeId() { return resourceTypeId; }
    public void setResourceTypeId(UUID resourceTypeId) { this.resourceTypeId = resourceTypeId; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public ResourceAttributeType getType() { return type; }
    public void setType(ResourceAttributeType type) { this.type = type; }
    public boolean isRequired() { return required; }
    public void setRequired(boolean required) { this.required = required; }
    public List<String> getOptions() { return options; }
    public void setOptions(List<String> options) { this.options = options; }
}
