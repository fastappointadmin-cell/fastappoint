package com.fastappoint.dto;

import java.time.LocalDateTime;
import java.util.UUID;

public class WhatsAppConnectionDTO {
    private UUID businessId;
    private boolean connected;
    private String source;
    private String status;
    private String phoneNumber;
    private String waLink;
    private String failureReason;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public WhatsAppConnectionDTO() {
    }

    public WhatsAppConnectionDTO(
            UUID businessId,
            boolean connected,
            String source,
            String status,
            String phoneNumber,
            String waLink,
            String failureReason,
            LocalDateTime createdAt,
            LocalDateTime updatedAt
    ) {
        this.businessId = businessId;
        this.connected = connected;
        this.source = source;
        this.status = status;
        this.phoneNumber = phoneNumber;
        this.waLink = waLink;
        this.failureReason = failureReason;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public UUID getBusinessId() { return businessId; }
    public boolean isConnected() { return connected; }
    public String getSource() { return source; }
    public String getStatus() { return status; }
    public String getPhoneNumber() { return phoneNumber; }
    public String getWaLink() { return waLink; }
    public String getFailureReason() { return failureReason; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
}
