package com.fastappoint.dto;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

public class CreateAppointmentRequest {
    private UUID businessId;
    private UUID serviceId; // null => manual booking
    private LocalDateTime startTime;
    private LocalDateTime endTime; // manual bookings only; service bookings derive it from the service duration
    private String customerName;
    private String customerPhone;
    private Map<String, Integer> inputs;
    private Set<UUID> preferredResourceIds; // service bookings: soft hint for the solver
    private Set<UUID> resourceIds; // manual bookings: resources to allocate directly
    private String manualLabel; // manual bookings only: free-text label for the booking

    public CreateAppointmentRequest() {}

    // Getters and Setters
    public UUID getBusinessId() { return businessId; }
    public void setBusinessId(UUID businessId) { this.businessId = businessId; }

    public UUID getServiceId() { return serviceId; }
    public void setServiceId(UUID serviceId) { this.serviceId = serviceId; }

    public LocalDateTime getStartTime() { return startTime; }
    public void setStartTime(LocalDateTime startTime) { this.startTime = startTime; }

    public LocalDateTime getEndTime() { return endTime; }
    public void setEndTime(LocalDateTime endTime) { this.endTime = endTime; }

    public String getCustomerName() { return customerName; }
    public void setCustomerName(String customerName) { this.customerName = customerName; }

    public String getCustomerPhone() { return customerPhone; }
    public void setCustomerPhone(String customerPhone) { this.customerPhone = customerPhone; }

    public Map<String, Integer> getInputs() { return inputs; }
    public void setInputs(Map<String, Integer> inputs) { this.inputs = inputs; }

    public Set<UUID> getPreferredResourceIds() { return preferredResourceIds; }
    public void setPreferredResourceIds(Set<UUID> preferredResourceIds) { this.preferredResourceIds = preferredResourceIds; }

    public Set<UUID> getResourceIds() { return resourceIds; }
    public void setResourceIds(Set<UUID> resourceIds) { this.resourceIds = resourceIds; }

    public String getManualLabel() { return manualLabel; }
    public void setManualLabel(String manualLabel) { this.manualLabel = manualLabel; }
}