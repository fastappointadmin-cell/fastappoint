package com.fastappoint.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

import java.util.Objects;
import java.util.UUID;

@Entity
@Table(name = "service_requirement_constraint")
public class ServiceRequirementConstraint {

    @Id
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "service_requirement_id", nullable = false)
    private ServiceRequirement serviceRequirement;

    @ManyToOne(fetch = FetchType.EAGER, optional = false)
    @JoinColumn(name = "attribute_definition_id", nullable = false)
    private ResourceAttributeDefinition attributeDefinition;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ServiceRequirementConstraintOperator operator;

    @Column(name = "expected_value", length = 1000)
    private String expectedValue;

    protected ServiceRequirementConstraint() {
    }

    ServiceRequirementConstraint(
            ServiceRequirement serviceRequirement,
            ResourceAttributeDefinition attributeDefinition,
            ServiceRequirementConstraintOperator operator,
            String expectedValue
    ) {
        this.id = UUID.randomUUID();
        this.serviceRequirement = serviceRequirement;
        this.attributeDefinition = attributeDefinition;
        this.operator = operator;
        this.expectedValue = expectedValue;
    }

    public void changeAttributeDefinition(ResourceAttributeDefinition attributeDefinition) {
        this.attributeDefinition = attributeDefinition;
    }

    public void changeOperator(ServiceRequirementConstraintOperator operator) {
        this.operator = operator;
    }

    public void changeExpectedValue(String expectedValue) {
        this.expectedValue = expectedValue;
    }

    public UUID getId() { return id; }
    public ServiceRequirement getServiceRequirement() { return serviceRequirement; }
    public ResourceAttributeDefinition getAttributeDefinition() { return attributeDefinition; }
    public ServiceRequirementConstraintOperator getOperator() { return operator; }
    public String getExpectedValue() { return expectedValue; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ServiceRequirementConstraint other)) return false;
        return id != null && id.equals(other.id);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(getClass());
    }
}
