package com.fastappoint.dto;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public class AppointmentDTO {
    private UUID id;
    private UUID businessId;
    private UUID serviceId;
    private String serviceName; // null for manual bookings
    private String manualLabel; // null for service bookings
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private String status;
    private String customerName;
    private String customerPhone;
    private List<ResourceAllocationDTO> allocations;

    public AppointmentDTO() {}

    public AppointmentDTO(UUID id, UUID businessId, UUID serviceId, String serviceName, String manualLabel,
                          LocalDateTime startTime, LocalDateTime endTime, String status,
                          String customerName, String customerPhone) {
        this.id = id;
        this.businessId = businessId;
        this.serviceId = serviceId;
        this.serviceName = serviceName;
        this.manualLabel = manualLabel;
        this.startTime = startTime;
        this.endTime = endTime;
        this.status = status;
        this.customerName = customerName;
        this.customerPhone = customerPhone;
    }

    public AppointmentDTO(UUID id, UUID businessId, UUID serviceId, String serviceName, String manualLabel,
                          LocalDateTime startTime, LocalDateTime endTime, String status,
                          String customerName, String customerPhone, List<ResourceAllocationDTO> allocations) {
        this(id, businessId, serviceId, serviceName, manualLabel, startTime, endTime, status, customerName, customerPhone);
        this.allocations = allocations;
    }

   

    // Getters and Setters
    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }

    public UUID getBusinessId() { return businessId; }
    public void setBusinessId(UUID businessId) { this.businessId = businessId; }

    public UUID getServiceId() { return serviceId; }
    public void setServiceId(UUID serviceId) { this.serviceId = serviceId; }

    public String getServiceName() { return serviceName; }
    public void setServiceName(String serviceName) { this.serviceName = serviceName; }

    public String getManualLabel() { return manualLabel; }
    public void setManualLabel(String manualLabel) { this.manualLabel = manualLabel; }

    public LocalDateTime getStartTime() { return startTime; }
    public void setStartTime(LocalDateTime startTime) { this.startTime = startTime; }

    public LocalDateTime getEndTime() { return endTime; }
    public void setEndTime(LocalDateTime endTime) { this.endTime = endTime; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getCustomerName() { return customerName; }
    public void setCustomerName(String customerName) { this.customerName = customerName; }

    public String getCustomerPhone() { return customerPhone; }
    public void setCustomerPhone(String customerPhone) { this.customerPhone = customerPhone; }

    public List<ResourceAllocationDTO> getAllocations() { return allocations; }
    public void setAllocations(List<ResourceAllocationDTO> allocations) { this.allocations = allocations; }
}