package com.fastappoint.dto;

import com.fastappoint.domain.ServiceRequirementFulfillmentMode;

import java.util.UUID;

public class ServiceRequirementDTO {
    private UUID id;
    private UUID serviceId;
    private UUID resourceTypeId;
    private String resourceTypeName;
    private Integer quantity;
    private ServiceRequirementFulfillmentMode fulfillmentMode;
    private Integer requiredCapacity;
    private String capacityInputKey;
    private java.util.List<ServiceRequirementConstraintDTO> constraints;

    public ServiceRequirementDTO() {
    }

    public ServiceRequirementDTO(UUID id, UUID serviceId, UUID resourceTypeId, String resourceTypeName,
                                 Integer quantity, ServiceRequirementFulfillmentMode fulfillmentMode,
                                 Integer requiredCapacity, String capacityInputKey,
                                 java.util.List<ServiceRequirementConstraintDTO> constraints) {
        this.id = id;
        this.serviceId = serviceId;
        this.resourceTypeId = resourceTypeId;
        this.resourceTypeName = resourceTypeName;
        this.quantity = quantity;
        this.fulfillmentMode = fulfillmentMode;
        this.requiredCapacity = requiredCapacity;
        this.capacityInputKey = capacityInputKey;
        this.constraints = constraints;
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public UUID getServiceId() {
        return serviceId;
    }

    public void setServiceId(UUID serviceId) {
        this.serviceId = serviceId;
    }

    public UUID getResourceTypeId() {
        return resourceTypeId;
    }

    public void setResourceTypeId(UUID resourceTypeId) {
        this.resourceTypeId = resourceTypeId;
    }

    public String getResourceTypeName() {
        return resourceTypeName;
    }

    public void setResourceTypeName(String resourceTypeName) {
        this.resourceTypeName = resourceTypeName;
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

    public java.util.List<ServiceRequirementConstraintDTO> getConstraints() {
        return constraints;
    }

    public void setConstraints(java.util.List<ServiceRequirementConstraintDTO> constraints) {
        this.constraints = constraints;
    }

}
