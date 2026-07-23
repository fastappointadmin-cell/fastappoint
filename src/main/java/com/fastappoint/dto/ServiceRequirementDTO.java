package com.fastappoint.dto;

import java.time.Duration;
import java.util.Set;
import java.util.UUID;

public class ServiceRequirementDTO {
    private UUID id;
    private UUID serviceId;
    private String resourceTypeName;
    private String allocationMode;
    private Integer quantity;
    private String demandParameter;
    private Set<CapabilityRefDTO> requiredCapabilities;
    private Long occupationDurationSeconds;

    public ServiceRequirementDTO() {
    }

    public ServiceRequirementDTO(UUID id, UUID serviceId, String resourceTypeName, String allocationMode,
                                 Integer quantity, String demandParameter, Set<CapabilityRefDTO> requiredCapabilities,
                                 Duration occupationDuration) {
        this.id = id;
        this.serviceId = serviceId;
        this.resourceTypeName = resourceTypeName;
        this.allocationMode = allocationMode;
        this.quantity = quantity;
        this.demandParameter = demandParameter;
        this.requiredCapabilities = requiredCapabilities;
        this.occupationDurationSeconds = occupationDuration != null ? occupationDuration.getSeconds() : null;
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

    public String getResourceTypeName() {
        return resourceTypeName;
    }

    public void setResourceTypeName(String resourceTypeName) {
        this.resourceTypeName = resourceTypeName;
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

    public Set<CapabilityRefDTO> getRequiredCapabilities() {
        return requiredCapabilities;
    }

    public void setRequiredCapabilities(Set<CapabilityRefDTO> requiredCapabilities) {
        this.requiredCapabilities = requiredCapabilities;
    }

    public Long getOccupationDurationSeconds() {
        return occupationDurationSeconds;
    }

    public void setOccupationDurationSeconds(Long occupationDurationSeconds) {
        this.occupationDurationSeconds = occupationDurationSeconds;
    }
}

