package com.fastappoint.dto;

import java.util.UUID;

public class ResourceAttributeValueInput {
    private UUID attributeDefinitionId;
    private String value;

    public ResourceAttributeValueInput() {
    }

    public UUID getAttributeDefinitionId() { return attributeDefinitionId; }
    public void setAttributeDefinitionId(UUID attributeDefinitionId) { this.attributeDefinitionId = attributeDefinitionId; }
    public String getValue() { return value; }
    public void setValue(String value) { this.value = value; }
}
