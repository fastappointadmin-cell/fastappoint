package com.fastappoint.domain;

import jakarta.persistence.*;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Objects;
import java.util.UUID;

/**
 * A one-off override for a specific date, taking precedence over the weekly windows.
 * If startTime/endTime are null the resource is CLOSED that day; otherwise those
 * hours replace the recurring windows for that date only.
 */
@Entity
@Table(name = "availability_exception")
public class AvailabilityException {

    @Id
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "resource_id", nullable = false)
    private Resource resource;

    @Column(name = "exception_date", nullable = false)
    private LocalDate date;

    @Column(name = "start_time")
    private LocalTime startTime; // null => closed all day

    @Column(name = "end_time")
    private LocalTime endTime;   // null => closed all day

    protected AvailabilityException() { // required by Hibernate
    }

    private AvailabilityException(Resource resource, LocalDate date, LocalTime startTime, LocalTime endTime) {
        this.id = UUID.randomUUID();
        this.resource = resource;
        this.date = date;
        this.startTime = startTime;
        this.endTime = endTime;
    }

    static AvailabilityException closed(Resource resource, LocalDate date) {
        return new AvailabilityException(resource, date, null, null);
    }

    static AvailabilityException customHours(Resource resource, LocalDate date, LocalTime start, LocalTime end) {
        return new AvailabilityException(resource, date, start, end);
    }

    public boolean isClosed() { return startTime == null || endTime == null; }

    public UUID getId() { return id; }
    public Resource getResource() { return resource; }
    public LocalDate getDate() { return date; }
    public LocalTime getStartTime() { return startTime; }
    public LocalTime getEndTime() { return endTime; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof AvailabilityException other)) return false;
        return id != null && id.equals(other.id);
    }

    @Override
    public int hashCode() { return Objects.hashCode(getClass()); }
}
