package com.fastappoint.dto;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public class AppointmentDTO {
    private UUID id;
    private UUID businessId;
    private UUID serviceId;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private String status;
    private String customerName;
    private String customerPhone;
    private List<ResourceAllocationDTO> allocations;

    public AppointmentDTO() {}

    public AppointmentDTO(UUID id, UUID businessId, UUID serviceId, LocalDateTime startTime,
                          LocalDateTime endTime, String status, String customerName, String customerPhone) {
        this.id = id;
        this.businessId = businessId;
        this.serviceId = serviceId;
        this.startTime = startTime;
        this.endTime = endTime;
        this.status = status;
        this.customerName = customerName;
        this.customerPhone = customerPhone;
    }

    public AppointmentDTO(UUID id, UUID businessId, UUID serviceId, LocalDateTime startTime,
                          LocalDateTime endTime, String status, String customerName, String customerPhone,
                          List<ResourceAllocationDTO> allocations) {
        this(id, businessId, serviceId, startTime, endTime, status, customerName, customerPhone);
        this.allocations = allocations;
    }

    // Getters and Setters
    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }

    public UUID getBusinessId() { return businessId; }
    public void setBusinessId(UUID businessId) { this.businessId = businessId; }

    public UUID getServiceId() { return serviceId; }
    public void setServiceId(UUID serviceId) { this.serviceId = serviceId; }

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