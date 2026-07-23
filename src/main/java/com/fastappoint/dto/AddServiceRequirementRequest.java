package com.fastappoint.dto;

import java.util.List;
import java.util.Set;
import java.util.UUID;

public class AddServiceRequirementRequest {
    private List<UUID> resourceTypeIds;
    private String allocationMode;
    private Integer quantity;
    private String demandParameter;
    private Set<UUID> requiredCapabilityIds;
    private Long occupationDurationSeconds;

    public AddServiceRequirementRequest() {
    }

    public List<UUID> getResourceTypeIds() {
        return resourceTypeIds;
    }

    public void setResourceTypeIds(List<UUID> resourceTypeIds) {
        this.resourceTypeIds = resourceTypeIds;
    }

    public String getAllocationMode() {
        return allocationMode;
    }

    public void setAllocationMode(String allocationMode) {
        this.allocationMode = allocationMode;
    }

    public Integer getQuantity() {
        return quantity;
    }

    public void setQuantity(Integer quantity) {
        this.quantity = quantity;
    }

    public String getDemandParameter() {
        return demandParameter;
    }

    public void setDemandParameter(String demandParameter) {
        this.demandParameter = demandParameter;
    }

    public Set<UUID> getRequiredCapabilityIds() {
        return requiredCapabilityIds;
    }

    public void setRequiredCapabilityIds(Set<UUID> requiredCapabilityIds) {
        this.requiredCapabilityIds = requiredCapabilityIds;
    }

    public Long getOccupationDurationSeconds() {
        return occupationDurationSeconds;
    }

    public void setOccupationDurationSeconds(Long occupationDurationSeconds) {
        this.occupationDurationSeconds = occupationDurationSeconds;
    }
}

