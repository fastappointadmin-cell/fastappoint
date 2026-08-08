package com.fastappoint.dto;

import com.fastappoint.domain.ServiceRequirementFulfillmentMode;

import java.util.List;
import java.util.UUID;

public class AddServiceRequirementRequest {
    private UUID resourceTypeId;
    private Integer quantity;
    private ServiceRequirementFulfillmentMode fulfillmentMode;
    private Integer requiredCapacity;
    private String capacityInputKey;
    private List<ServiceRequirementConstraintInput> constraints;


    public AddServiceRequirementRequest() {
    }

    public UUID getResourceTypeId() {
        return resourceTypeId;
    }

    public void setResourceTypeId(UUID resourceTypeId) {
        this.resourceTypeId = resourceTypeId;
    }

    public Integer getQuantity() {
        return quantity;
    }

    public void setQuantity(Integer quantity) {
        this.quantity = quantity;
    }

    public ServiceRequirementFulfillmentMode getFulfillmentMode() {
        return fulfillmentMode;
    }

    public void setFulfillmentMode(ServiceRequirementFulfillmentMode fulfillmentMode) {
        this.fulfillmentMode = fulfillmentMode;
    }

    public Integer getRequiredCapacity() {
        return requiredCapacity;
    }

    public void setRequiredCapacity(Integer requiredCapacity) {
        this.requiredCapacity = requiredCapacity;
    }

    public String getCapacityInputKey() {
        return capacityInputKey;
    }

    public void setCapacityInputKey(String capacityInputKey) {
        this.capacityInputKey = capacityInputKey;
    }

    public List<ServiceRequirementConstraintInput> getConstraints() {
        return constraints;
    }

    public void setConstraints(List<ServiceRequirementConstraintInput> constraints) {
        this.constraints = constraints;
    }

}
