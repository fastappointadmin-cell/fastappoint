package com.fastappoint.dto;

import com.fastappoint.domain.ResourceAttributeType;

import java.util.List;
import java.util.UUID;

public class ResourceAttributeValueDTO {
    private UUID attributeDefinitionId;
    private String attributeName;
    private ResourceAttributeType attributeType;
    private boolean required;
    private List<String> options;
    private String value;

    public ResourceAttributeValueDTO() {
    }

    public ResourceAttributeValueDTO(
            UUID attributeDefinitionId,
            String attributeName,
            ResourceAttributeType attributeType,
            boolean required,
            List<String> options,
            String value
    ) {
        this.attributeDefinitionId = attributeDefinitionId;
        this.attributeName = attributeName;
        this.attributeType = attributeType;
        this.required = required;
        this.options = options;
        this.value = value;
    }

    public UUID getAttributeDefinitionId() { return attributeDefinitionId; }
    public void setAttributeDefinitionId(UUID attributeDefinitionId) { this.attributeDefinitionId = attributeDefinitionId; }
    public String getAttributeName() { return attributeName; }
    public void setAttributeName(String attributeName) { this.attributeName = attributeName; }
    public ResourceAttributeType getAttributeType() { return attributeType; }
    public void setAttributeType(ResourceAttributeType attributeType) { this.attributeType = attributeType; }
    public boolean isRequired() { return required; }
    public void setRequired(boolean required) { this.required = required; }
    public List<String> getOptions() { return options; }
    public void setOptions(List<String> options) { this.options = options; }
    public String getValue() { return value; }
    public void setValue(String value) { this.value = value; }
}
