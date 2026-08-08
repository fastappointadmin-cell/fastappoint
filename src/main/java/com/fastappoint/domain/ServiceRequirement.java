package com.fastappoint.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

/**
 * One resource line in a service's requirements.
 *
 * Mandatory fields:
 *   - resourceType  : the type of resource needed
 *   - quantity      : how many resources of this type are needed (default 1)
 *
 * Advanced (optional) fields:
 *   - label             : human-readable label for this line (e.g. "senior barber", "shampoo station")
 *   - notes             : free-text notes about this requirement
 */
@Entity
@Table(name = "service_requirement")
public class ServiceRequirement {

    @Id
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "service_id", nullable = false)
    private BusinessService businessService;

    @ManyToOne(fetch = FetchType.EAGER, optional = false)
    @JoinColumn(name = "resource_type_id", nullable = false)
    private ResourceType resourceType;

    /** How many resources of this type are needed. Must be >= 1. */
    @Column(nullable = false)
    private int quantity = 1;

    @Enumerated(EnumType.STRING)
    @Column(name = "fulfillment_mode", length = 24)
    private ServiceRequirementFulfillmentMode fulfillmentMode = ServiceRequirementFulfillmentMode.QUANTITY;

    @Column(name = "required_capacity")
    private Integer requiredCapacity;

    @Column(name = "capacity_input_key", length = 120)
    private String capacityInputKey;

    // --- Advanced / optional ---

    /** Human-readable label for this requirement line. */
    @Column(length = 255)
    private String label;

    /** Free-text notes about this requirement. */
    @Column(length = 1000)
    private String notes;

    @OneToMany(mappedBy = "serviceRequirement", cascade = jakarta.persistence.CascadeType.ALL, orphanRemoval = true)
    private List<ServiceRequirementConstraint> constraints = new ArrayList<>();

    protected ServiceRequirement() { // required by Hibernate
    }

    ServiceRequirement(BusinessService businessService, ResourceType resourceType, int quantity) {
        this.id = UUID.randomUUID();
        this.businessService = businessService;
        this.resourceType = resourceType;
        this.quantity = quantity;
    }

    // --- fluent advanced configuration ---

    public ServiceRequirement withLabel(String label) {
        this.label = label;
        return this;
    }

    public ServiceRequirement withNotes(String notes) {
        this.notes = notes;
        return this;
    }

    public void changeResourceType(ResourceType resourceType) {
        this.resourceType = resourceType;
    }

    public void changeQuantity(int quantity) {
        this.quantity = quantity;
    }

    public void configureQuantityMode(int quantity) {
        this.fulfillmentMode = ServiceRequirementFulfillmentMode.QUANTITY;
        this.quantity = quantity;
        this.requiredCapacity = null;
        this.capacityInputKey = null;
    }

    public void configureCapacityMode(Integer requiredCapacity, String capacityInputKey) {
        this.fulfillmentMode = ServiceRequirementFulfillmentMode.CAPACITY;
        this.quantity = 1;
        this.requiredCapacity = requiredCapacity;
        this.capacityInputKey = capacityInputKey;
    }

    public ServiceRequirementConstraint addConstraint(
            ResourceAttributeDefinition attributeDefinition,
            ServiceRequirementConstraintOperator operator,
            String expectedValue
    ) {
        ServiceRequirementConstraint constraint =
                new ServiceRequirementConstraint(this, attributeDefinition, operator, expectedValue);
        constraints.add(constraint);
        return constraint;
    }

    public void clearConstraints() {
        constraints.clear();
    }

    // --- accessors ---

    public UUID getId() { return id; }
    public BusinessService getService() { return businessService; }
    public ResourceType getResourceType() { return resourceType; }
    public int getQuantity() { return quantity; }
    public ServiceRequirementFulfillmentMode getFulfillmentMode() {
        return fulfillmentMode == null ? ServiceRequirementFulfillmentMode.QUANTITY : fulfillmentMode;
    }
    public Integer getRequiredCapacity() { return requiredCapacity; }
    public String getCapacityInputKey() { return capacityInputKey; }
    public String getLabel() { return label; }
    public String getNotes() { return notes; }
    public List<ServiceRequirementConstraint> getConstraints() { return constraints; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ServiceRequirement other)) return false;
        return id != null && id.equals(other.id);
    }

    @Override
    public int hashCode() { return Objects.hashCode(getClass()); }
}
