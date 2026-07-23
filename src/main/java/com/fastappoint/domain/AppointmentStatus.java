package com.fastappoint.domain;

public enum AppointmentStatus {
    PENDING,     // created, resources tentatively held, not yet confirmed
    CONFIRMED,   // confirmed by the customer / business
    CANCELLED,
    COMPLETED
}
