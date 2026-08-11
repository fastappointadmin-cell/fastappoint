package com.fastappoint.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;

import java.time.LocalDateTime;
import java.util.Objects;
import java.util.UUID;

/**
 * A business's connection to the shared WhatsApp Business Account (one Meta app/token for the whole
 * platform -- see {@link com.fastappoint.service.WhatsAppCloudApiClient}). One row per business:
 * restarting a failed/disconnected attempt mutates this row in place rather than creating a new one,
 * so {@code business_id} can stay unique.
 */
@Entity
@Table(name = "whatsapp_connection")
public class WhatsAppConnection {

    @Id
    private UUID id;

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "business_id", nullable = false, unique = true)
    private Business business;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 16)
    private WhatsAppConnectionSource source;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 16)
    private WhatsAppConnectionStatus status;

    /** E.164 number, known from the start on both paths (purchased up front, or entered by the owner). */
    @Column(name = "phone_number", nullable = false, length = 32)
    private String phoneNumber;

    /** Meta's identifier for this number once registered against the WABA -- what every Cloud API call
     * (send, profile update, dereigster) and the inbound webhook route by. Null until registration succeeds. */
    @Column(name = "meta_phone_number_id", length = 64)
    private String metaPhoneNumberId;

    /** The number-provider's own identifier (e.g. a Twilio SID), only set for {@link WhatsAppConnectionSource#PROVISIONED} --
     * needed to release the number back to the provider on disconnect. */
    @Column(name = "provider_number_sid", length = 64)
    private String providerNumberSid;

    @Column(name = "failure_reason", length = 500)
    private String failureReason;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    protected WhatsAppConnection() {
    }

    public WhatsAppConnection(Business business, WhatsAppConnectionSource source, String phoneNumber) {
        this.id = UUID.randomUUID();
        this.business = business;
        this.source = source;
        this.phoneNumber = phoneNumber;
        this.status = WhatsAppConnectionStatus.AWAITING_OTP;
        this.createdAt = LocalDateTime.now();
        this.updatedAt = this.createdAt;
    }

    /** Reuses this row for a new connection attempt (new number, possibly a different source) instead
     * of inserting a second row for the same business. */
    public void restart(WhatsAppConnectionSource source, String phoneNumber) {
        this.source = source;
        this.phoneNumber = phoneNumber;
        this.status = WhatsAppConnectionStatus.AWAITING_OTP;
        this.metaPhoneNumberId = null;
        this.providerNumberSid = null;
        this.failureReason = null;
        this.updatedAt = LocalDateTime.now();
    }

    public void markProviderNumberSid(String providerNumberSid) {
        this.providerNumberSid = providerNumberSid;
        this.updatedAt = LocalDateTime.now();
    }

    public void markMetaPhoneNumberId(String metaPhoneNumberId) {
        this.metaPhoneNumberId = metaPhoneNumberId;
        this.updatedAt = LocalDateTime.now();
    }

    public void activate() {
        this.status = WhatsAppConnectionStatus.ACTIVE;
        this.failureReason = null;
        this.updatedAt = LocalDateTime.now();
    }

    public void fail(String reason) {
        this.status = WhatsAppConnectionStatus.FAILED;
        this.failureReason = reason;
        this.updatedAt = LocalDateTime.now();
    }

    public void disconnect() {
        this.status = WhatsAppConnectionStatus.DISCONNECTED;
        this.updatedAt = LocalDateTime.now();
    }

    public UUID getId() { return id; }
    public Business getBusiness() { return business; }
    public WhatsAppConnectionSource getSource() { return source; }
    public WhatsAppConnectionStatus getStatus() { return status; }
    public String getPhoneNumber() { return phoneNumber; }
    public String getMetaPhoneNumberId() { return metaPhoneNumberId; }
    public String getProviderNumberSid() { return providerNumberSid; }
    public String getFailureReason() { return failureReason; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof WhatsAppConnection other)) return false;
        return id != null && id.equals(other.id);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(getClass());
    }
}
