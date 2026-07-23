package com.fastappoint.domain;

import jakarta.persistence.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

/**
 * A concrete booking: a service placed on the calendar, with the exact resources
 * bound to it. Times are wall-clock (business-local), consistent with availability.
 * Which resources satisfy which requirement lives in the allocations, because a
 * single appointment can hold several resources on possibly different sub-intervals.
 */
@Entity
@Table(name = "appointment")
public class Appointment {

    @Id
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "business_id", nullable = false)
    private Business business;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "service_id", nullable = false)
    private BusinessService businessService;

    @Column(name = "start_time", nullable = false)
    private LocalDateTime startTime;

    @Column(name = "end_time", nullable = false)
    private LocalDateTime endTime;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 16)
    private AppointmentStatus status;

    @Embedded
    private Customer customer;

    @OneToMany(mappedBy = "appointment", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ResourceAllocation> allocations = new ArrayList<>();

    protected Appointment() { // required by Hibernate
    }

    public Appointment(Business business, BusinessService businessService,
                       LocalDateTime startTime, LocalDateTime endTime, Customer customer) {
        this.id = UUID.randomUUID();
        this.business = business;
        this.businessService = businessService;
        this.startTime = startTime;
        this.endTime = endTime;
        this.customer = customer;
        this.status = AppointmentStatus.PENDING;
    }

    /** Bind a concrete resource to a requirement line over an occupied interval. */
    public ResourceAllocation allocate(Resource resource, ServiceRequirement requirement,
                                       LocalDateTime start, LocalDateTime end) {
        ResourceAllocation allocation = new ResourceAllocation(this, resource, requirement, start, end);
        allocations.add(allocation);
        return allocation;
    }

    public void confirm() { this.status = AppointmentStatus.CONFIRMED; }
    public void cancel() { this.status = AppointmentStatus.CANCELLED; }
    public void complete() { this.status = AppointmentStatus.COMPLETED; }

    public UUID getId() { return id; }
    public Business getBusiness() { return business; }
    public BusinessService getService() { return businessService; }
    public LocalDateTime getStartTime() { return startTime; }
    public LocalDateTime getEndTime() { return endTime; }
    public AppointmentStatus getStatus() { return status; }
    public Customer getCustomer() { return customer; }
    public List<ResourceAllocation> getAllocations() { return allocations; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Appointment other)) return false;
        return id != null && id.equals(other.id);
    }

    @Override
    public int hashCode() { return Objects.hashCode(getClass()); }
}
