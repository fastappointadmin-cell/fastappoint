package com.fastappoint.domain;

import jakarta.persistence.*;

import java.time.DayOfWeek;
import java.time.LocalTime;
import java.util.Objects;
import java.util.UUID;

/**
 * A recurring weekly window during which a resource offers itself.
 * Wall-clock (business-local) times. A resource with no windows is never bookable.
 * Multiple windows per day are allowed (e.g. 09:00-13:00 and 16:00-20:00).
 */
@Entity
@Table(name = "resource_availability")
public class ResourceAvailability {

    @Id
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "resource_id", nullable = false)
    private Resource resource;

    @Enumerated(EnumType.STRING)
    @Column(name = "day_of_week", nullable = false, length = 16)
    private DayOfWeek dayOfWeek;

    @Column(name = "start_time", nullable = false)
    private LocalTime startTime;

    @Column(name = "end_time", nullable = false)
    private LocalTime endTime;

    protected ResourceAvailability() { // required by Hibernate
    }

    ResourceAvailability(Resource resource, DayOfWeek dayOfWeek, LocalTime startTime, LocalTime endTime) {
        this.id = UUID.randomUUID();
        this.resource = resource;
        this.dayOfWeek = dayOfWeek;
        this.startTime = startTime;
        this.endTime = endTime;
    }

    public UUID getId() { return id; }
    public Resource getResource() { return resource; }
    public DayOfWeek getDayOfWeek() { return dayOfWeek; }
    public LocalTime getStartTime() { return startTime; }
    public LocalTime getEndTime() { return endTime; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ResourceAvailability other)) return false;
        return id != null && id.equals(other.id);
    }

    @Override
    public int hashCode() { return Objects.hashCode(getClass()); }
}
