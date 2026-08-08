package com.fastappoint.dto;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;

public class ChatInboundMessageRequest {
    private String toPhoneNumber;
    private String fromPhoneNumber;
    private String customerName;
    private String message;
    private UUID serviceId;
    private LocalDate date;
    private LocalDateTime startTime;
    private Map<String, Integer> inputs;

    public String getToPhoneNumber() { return toPhoneNumber; }
    public void setToPhoneNumber(String toPhoneNumber) { this.toPhoneNumber = toPhoneNumber; }
    public String getFromPhoneNumber() { return fromPhoneNumber; }
    public void setFromPhoneNumber(String fromPhoneNumber) { this.fromPhoneNumber = fromPhoneNumber; }
    public String getCustomerName() { return customerName; }
    public void setCustomerName(String customerName) { this.customerName = customerName; }
    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }
    public UUID getServiceId() { return serviceId; }
    public void setServiceId(UUID serviceId) { this.serviceId = serviceId; }
    public LocalDate getDate() { return date; }
    public void setDate(LocalDate date) { this.date = date; }
    public LocalDateTime getStartTime() { return startTime; }
    public void setStartTime(LocalDateTime startTime) { this.startTime = startTime; }
    public Map<String, Integer> getInputs() { return inputs; }
    public void setInputs(Map<String, Integer> inputs) { this.inputs = inputs; }
}
