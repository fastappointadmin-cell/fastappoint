package com.fastappoint.dto;

import java.time.LocalDateTime;
import java.util.UUID;

public class CreateAppointmentRequest {
    private UUID businessId;
    private UUID serviceId;
    private LocalDateTime startTime;
    private String customerName;
    private String customerPhone;

    public CreateAppointmentRequest() {}

    // Getters and Setters
    public UUID getBusinessId() { return businessId; }
    public void setBusinessId(UUID businessId) { this.businessId = businessId; }

    public UUID getServiceId() { return serviceId; }
    public void setServiceId(UUID serviceId) { this.serviceId = serviceId; }

    public LocalDateTime getStartTime() { return startTime; }
    public void setStartTime(LocalDateTime startTime) { this.startTime = startTime; }

    public String getCustomerName() { return customerName; }
    public void setCustomerName(String customerName) { this.customerName = customerName; }

    public String getCustomerPhone() { return customerPhone; }
    public void setCustomerPhone(String customerPhone) { this.customerPhone = customerPhone; }
}