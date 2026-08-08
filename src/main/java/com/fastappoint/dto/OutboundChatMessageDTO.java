package com.fastappoint.dto;

import java.time.LocalDateTime;
import java.util.UUID;

public class OutboundChatMessageDTO {
    private UUID id;
    private UUID businessId;
    private UUID appointmentId;
    private String kind;
    private String status;
    private String toPhoneNumber;
    private String fromPhoneNumber;
    private String body;
    private LocalDateTime sendAt;
    private LocalDateTime sentAt;

    public OutboundChatMessageDTO() {}

    public OutboundChatMessageDTO(UUID id, UUID businessId, UUID appointmentId, String kind, String status,
                                  String toPhoneNumber, String fromPhoneNumber, String body,
                                  LocalDateTime sendAt, LocalDateTime sentAt) {
        this.id = id;
        this.businessId = businessId;
        this.appointmentId = appointmentId;
        this.kind = kind;
        this.status = status;
        this.toPhoneNumber = toPhoneNumber;
        this.fromPhoneNumber = fromPhoneNumber;
        this.body = body;
        this.sendAt = sendAt;
        this.sentAt = sentAt;
    }

    public UUID getId() { return id; }
    public UUID getBusinessId() { return businessId; }
    public UUID getAppointmentId() { return appointmentId; }
    public String getKind() { return kind; }
    public String getStatus() { return status; }
    public String getToPhoneNumber() { return toPhoneNumber; }
    public String getFromPhoneNumber() { return fromPhoneNumber; }
    public String getBody() { return body; }
    public LocalDateTime getSendAt() { return sendAt; }
    public LocalDateTime getSentAt() { return sentAt; }
}
