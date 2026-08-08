package com.fastappoint.dto;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;

/** What an anonymous client submits to book a service -- a deliberately narrow subset of
 * {@link CreateAppointmentRequest}: no manual bookings, no resource picking, nothing an
 * unauthenticated caller shouldn't be able to influence. */
public class PublicBookingRequest {
    private UUID businessId;
    private UUID serviceId;
    private LocalDateTime startTime;
    private String customerName;
    private String customerPhone;
    private Map<String, Integer> inputs;

    public PublicBookingRequest() {}

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

    public Map<String, Integer> getInputs() { return inputs; }
    public void setInputs(Map<String, Integer> inputs) { this.inputs = inputs; }
}
