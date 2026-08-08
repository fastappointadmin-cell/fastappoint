package com.fastappoint.dto;

import com.fastappoint.domain.ResourceAttributeType;
import com.fastappoint.domain.ServiceRequirementConstraintOperator;

import java.util.List;
import java.util.UUID;

public class ServiceRequirementConstraintDTO {
    private UUID id;
    private UUID attributeDefinitionId;
    private String attributeName;
    private ResourceAttributeType attributeType;
    private ServiceRequirementConstraintOperator operator;
    private String expectedValue;
    private List<String> options;

    public ServiceRequirementConstraintDTO() {
    }

    public ServiceRequirementConstraintDTO(
            UUID id,
            UUID attributeDefinitionId,
            String attributeName,
            ResourceAttributeType attributeType,
            ServiceRequirementConstraintOperator operator,
            String expectedValue,
            List<String> options
    ) {
        this.id = id;
        this.attributeDefinitionId = attributeDefinitionId;
        this.attributeName = attributeName;
        this.attributeType = attributeType;
        this.operator = operator;
        this.expectedValue = expectedValue;
        this.options = options;
    }

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }
    public UUID getAttributeDefinitionId() { return attributeDefinitionId; }
    public void setAttributeDefinitionId(UUID attributeDefinitionId) { this.attributeDefinitionId = attributeDefinitionId; }
    public String getAttributeName() { return attributeName; }
    public void setAttributeName(String attributeName) { this.attributeName = attributeName; }
    public ResourceAttributeType getAttributeType() { return attributeType; }
    public void setAttributeType(ResourceAttributeType attributeType) { this.attributeType = attributeType; }
    public ServiceRequirementConstraintOperator getOperator() { return operator; }
    public void setOperator(ServiceRequirementConstraintOperator operator) { this.operator = operator; }
    public String getExpectedValue() { return expectedValue; }
    public void setExpectedValue(String expectedValue) { this.expectedValue = expectedValue; }
    public List<String> getOptions() { return options; }
    public void setOptions(List<String> options) { this.options = options; }
}
