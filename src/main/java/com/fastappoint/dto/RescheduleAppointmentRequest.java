package com.fastappoint.dto;

import java.time.LocalDateTime;

public class RescheduleAppointmentRequest {
    private LocalDateTime startTime;
    private LocalDateTime endTime;

    public RescheduleAppointmentRequest() {}

    public LocalDateTime getStartTime() { return startTime; }
    public void setStartTime(LocalDateTime startTime) { this.startTime = startTime; }

    public LocalDateTime getEndTime() { return endTime; }
    public void setEndTime(LocalDateTime endTime) { this.endTime = endTime; }
}
