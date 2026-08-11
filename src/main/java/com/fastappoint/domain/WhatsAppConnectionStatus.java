package com.fastappoint.domain;

public enum WhatsAppConnectionStatus {
    /** Number purchased/entered and submitted to Meta; for {@link WhatsAppConnectionSource#OWN_NUMBER}
     * this is where it waits for the business owner to enter the OTP Meta sent to their phone. */
    AWAITING_OTP,
    ACTIVE,
    FAILED,
    DISCONNECTED
}
