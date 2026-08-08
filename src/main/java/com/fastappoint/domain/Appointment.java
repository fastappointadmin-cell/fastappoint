package com.fastappoint.domain;

import jakarta.persistence.*;

import java.time.Duration;
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

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "service_id")
    private BusinessService businessService;

    @Column(name = "start_time", nullable = false)
    private LocalDateTime startTime;

    @Column(name = "end_time", nullable = false)
    private LocalDateTime endTime;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 16)
    private AppointmentStatus status;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "customer_id", nullable = false)
    private Customer customer;

    /** Free-text label for a manual (service-less) booking. Null for service bookings. */
    @Column(name = "manual_label")
    private String manualLabel;

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

    /** Manual booking: not tied to any BusinessService, resources are assigned directly (not via the solver). */
    public Appointment(Business business, LocalDateTime startTime, LocalDateTime endTime,
                       Customer customer, String manualLabel) {
        this.id = UUID.randomUUID();
        this.business = business;
        this.businessService = null;
        this.startTime = startTime;
        this.endTime = endTime;
        this.customer = customer;
        this.manualLabel = manualLabel;
        this.status = AppointmentStatus.CONFIRMED;
    }

    /**
     * Manual booking that's still tagged with a BusinessService: resources and the exact interval are assigned
     * directly (not via the solver), same as the fully manual constructor, but the service is kept for
     * reporting/reference -- lets a caller override the service's own duration with a custom interval.
     */
    public Appointment(Business business, BusinessService businessService, LocalDateTime startTime, LocalDateTime endTime,
                       Customer customer, String manualLabel) {
        this.id = UUID.randomUUID();
        this.business = business;
        this.businessService = businessService;
        this.startTime = startTime;
        this.endTime = endTime;
        this.customer = customer;
        this.manualLabel = manualLabel;
        this.status = AppointmentStatus.CONFIRMED;
    }

    /** Bind a concrete resource to this appointment over an occupied interval. */
    public ResourceAllocation allocate(Resource resource, LocalDateTime start, LocalDateTime end) {
        ResourceAllocation allocation = new ResourceAllocation(this, resource, start, end);
        allocations.add(allocation);
        return allocation;
    }

    /** Moves the appointment to a new start/end, translating every allocation by the same start delta. */
    public void reschedule(LocalDateTime newStart, LocalDateTime newEnd) {
        Duration deltaStart = Duration.between(startTime, newStart);
        for (ResourceAllocation allocation : allocations) {
            Duration ownDuration = Duration.between(allocation.getStartTime(), allocation.getEndTime());
            LocalDateTime shiftedStart = allocation.getStartTime().plus(deltaStart);
            allocation.setStartTime(shiftedStart);
            allocation.setEndTime(shiftedStart.plus(ownDuration));
        }
        this.startTime = newStart;
        this.endTime = newEnd;
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
    public String getManualLabel() { return manualLabel; }
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
