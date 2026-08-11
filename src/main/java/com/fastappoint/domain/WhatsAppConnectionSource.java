package com.fastappoint.domain;

/** Where a business's WhatsApp number came from -- determines whether onboarding is fully
 * automated (a number FastAppoint provisions and owns) or requires one manual OTP step
 * (a number the business already owns and wants to keep using). */
public enum WhatsAppConnectionSource {
    PROVISIONED,
    OWN_NUMBER
}
