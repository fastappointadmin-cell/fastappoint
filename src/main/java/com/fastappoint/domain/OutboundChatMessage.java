package com.fastappoint.domain;

import jakarta.persistence.*;

import java.time.LocalDateTime;
import java.util.Objects;
import java.util.UUID;

@Entity
@Table(name = "outbound_chat_message")
public class OutboundChatMessage {

    @Id
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "business_id", nullable = false)
    private Business business;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "appointment_id", nullable = false)
    private Appointment appointment;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 16)
    private OutboundChatMessageKind kind;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 16)
    private OutboundChatMessageStatus status;

    @Column(name = "to_phone_number", nullable = false, length = 32)
    private String toPhoneNumber;

    @Column(name = "from_phone_number", nullable = false, length = 32)
    private String fromPhoneNumber;

    @Column(nullable = false, length = 4000)
    private String body;

    @Column(name = "send_at", nullable = false)
    private LocalDateTime sendAt;

    @Column(name = "sent_at")
    private LocalDateTime sentAt;

    protected OutboundChatMessage() {
    }

    public OutboundChatMessage(Business business, Appointment appointment, OutboundChatMessageKind kind,
                               String toPhoneNumber, String fromPhoneNumber, String body, LocalDateTime sendAt) {
        this.id = UUID.randomUUID();
        this.business = business;
        this.appointment = appointment;
        this.kind = kind;
        this.status = OutboundChatMessageStatus.PENDING;
        this.toPhoneNumber = toPhoneNumber;
        this.fromPhoneNumber = fromPhoneNumber;
        this.body = body;
        this.sendAt = sendAt;
    }

    public void markDispatched(LocalDateTime sentAt) {
        this.status = OutboundChatMessageStatus.DISPATCHED;
        this.sentAt = sentAt;
    }

    public void markSkipped(LocalDateTime decidedAt) {
        this.status = OutboundChatMessageStatus.SKIPPED;
        this.sentAt = decidedAt;
    }

    public UUID getId() { return id; }
    public Business getBusiness() { return business; }
    public Appointment getAppointment() { return appointment; }
    public OutboundChatMessageKind getKind() { return kind; }
    public OutboundChatMessageStatus getStatus() { return status; }
    public String getToPhoneNumber() { return toPhoneNumber; }
    public String getFromPhoneNumber() { return fromPhoneNumber; }
    public String getBody() { return body; }
    public LocalDateTime getSendAt() { return sendAt; }
    public LocalDateTime getSentAt() { return sentAt; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof OutboundChatMessage other)) return false;
        return id != null && id.equals(other.id);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(getClass());
    }
}
