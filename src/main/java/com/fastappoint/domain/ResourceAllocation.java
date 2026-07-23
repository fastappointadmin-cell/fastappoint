package com.fastappoint.domain;

import jakarta.persistence.*;

import java.time.LocalDateTime;
import java.util.Objects;
import java.util.UUID;

/**
 * One resource bound to one requirement line of an appointment, over the exact
 * interval it is occupied. This is the row that must never overlap another
 * allocation for the same resource -- the natural place for a DB-level
 * exclusion constraint on (resource_id, [start_time, end_time)) later on.
 */
@Entity
@Table(name = "resource_allocation")
public class ResourceAllocation {

    @Id
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "appointment_id", nullable = false)
    private Appointment appointment;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "resource_id", nullable = false)
    private Resource resource;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "requirement_id", nullable = false)
    private ServiceRequirement requirement;

    @Column(name = "start_time", nullable = false)
    private LocalDateTime startTime;

    @Column(name = "end_time", nullable = false)
    private LocalDateTime endTime;

    protected ResourceAllocation() { // required by Hibernate
    }

    ResourceAllocation(Appointment appointment, Resource resource, ServiceRequirement requirement,
                       LocalDateTime startTime, LocalDateTime endTime) {
        this.id = UUID.randomUUID();
        this.appointment = appointment;
        this.resource = resource;
        this.requirement = requirement;
        this.startTime = startTime;
        this.endTime = endTime;
    }

    public UUID getId() { return id; }
    public Appointment getAppointment() { return appointment; }
    public Resource getResource() { return resource; }
    public ServiceRequirement getRequirement() { return requirement; }
    public LocalDateTime getStartTime() { return startTime; }
    public LocalDateTime getEndTime() { return endTime; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ResourceAllocation other)) return false;
        return id != null && id.equals(other.id);
    }

    @Override
    public int hashCode() { return Objects.hashCode(getClass()); }
}
