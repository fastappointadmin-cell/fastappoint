package com.fastappoint.dto;

import com.fastappoint.domain.ServiceRequirementConstraintOperator;

import java.util.UUID;

public class ServiceRequirementConstraintInput {
    private UUID attributeDefinitionId;
    private ServiceRequirementConstraintOperator operator;
    private String expectedValue;

    public ServiceRequirementConstraintInput() {
    }

    public UUID getAttributeDefinitionId() { return attributeDefinitionId; }
    public void setAttributeDefinitionId(UUID attributeDefinitionId) { this.attributeDefinitionId = attributeDefinitionId; }
    public ServiceRequirementConstraintOperator getOperator() { return operator; }
    public void setOperator(ServiceRequirementConstraintOperator operator) { this.operator = operator; }
    public String getExpectedValue() { return expectedValue; }
    public void setExpectedValue(String expectedValue) { this.expectedValue = expectedValue; }
}
