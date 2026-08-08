package com.fastappoint.dto;

import java.time.LocalDate;
import java.time.LocalTime;

public class ChatBookingIntent {

    private String serviceName;
    private LocalDate requestedDate;
    private LocalTime requestedTime;

    public static ChatBookingIntent empty() {
        return new ChatBookingIntent();
    }

    public String getServiceName() {
        return serviceName;
    }

    public void setServiceName(String serviceName) {
        this.serviceName = serviceName;
    }

    public LocalDate getRequestedDate() {
        return requestedDate;
    }

    public void setRequestedDate(LocalDate requestedDate) {
        this.requestedDate = requestedDate;
    }

    public LocalTime getRequestedTime() {
        return requestedTime;
    }

    public void setRequestedTime(LocalTime requestedTime) {
        this.requestedTime = requestedTime;
    }
}
