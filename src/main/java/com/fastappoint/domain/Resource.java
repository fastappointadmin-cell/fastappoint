package com.fastappoint.domain;

import jakarta.persistence.*;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;

/**
 * A concrete bookable resource: "Marius" (a Barber), "Table 12" (a Table),
 * "Ramp A" (a Ramp). Everything the solver needs to MATCH a resource lives here:
 *   - type            -> the coarse category
 *   - capacity        -> the numeric attribute MERGE sums over (seats). null when N/A.
 *   - capabilities    -> free-form tags the requirement can filter on (skills, zone)
 *   - availability    -> when this resource offers itself (per resource, not per business)
 */
@Entity
@Table(name = "resource")
public class Resource {

    @Id
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "business_id", nullable = false)
    private Business business;

    @Column(nullable = false)
    private String name;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "resource_type_id", nullable = false)
    private ResourceType type;

    /** Numeric attribute used by MERGE requirements (e.g. seats). null when irrelevant. */
    @Column
    private Integer capacity;

    /** Managed capabilities this resource has: linked to the capability registry. */
    @ManyToMany
    @JoinTable(
            name = "resource_capability",
            joinColumns = @JoinColumn(name = "resource_id"),
            inverseJoinColumns = @JoinColumn(name = "capability_id")
    )
    private Set<Capability> capabilities = new HashSet<>();

    /** Recurring weekly working windows. */
    @OneToMany(mappedBy = "resource", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ResourceAvailability> availability = new ArrayList<>();

    /** One-off overrides for specific dates (time off, holidays, extra hours). */
    @OneToMany(mappedBy = "resource", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<AvailabilityException> availabilityExceptions = new ArrayList<>();

    protected Resource() { // required by Hibernate
    }

    Resource(Business business, String name, ResourceType type) {
        this.id = UUID.randomUUID();
        this.business = business;
        this.name = name;
        this.type = type;
    }

    // --- fluent configuration helpers ---

    public Resource withCapacity(Integer capacity) {
        this.capacity = capacity;
        return this;
    }

    public Resource addCapability(Capability capability) {
        this.capabilities.add(capability);
        return this;
    }

    public Resource removeCapability(Capability capability) {
        this.capabilities.remove(capability);
        return this;
    }

    public ResourceAvailability addAvailability(DayOfWeek day, LocalTime start, LocalTime end) {
        ResourceAvailability window = new ResourceAvailability(this, day, start, end);
        availability.add(window);
        return window;
    }

    public AvailabilityException addClosedException(LocalDate date) {
        AvailabilityException ex = AvailabilityException.closed(this, date);
        availabilityExceptions.add(ex);
        return ex;
    }

    public AvailabilityException addCustomHoursException(LocalDate date, LocalTime start, LocalTime end) {
        AvailabilityException ex = AvailabilityException.customHours(this, date, start, end);
        availabilityExceptions.add(ex);
        return ex;
    }

    public UUID getId() { return id; }
    public Business getBusiness() { return business; }
    public String getName() { return name; }
    public ResourceType getType() { return type; }
    public Integer getCapacity() { return capacity; }
    public Set<Capability> getCapabilities() { return capabilities; }
    public List<ResourceAvailability> getAvailability() { return availability; }
    public List<AvailabilityException> getAvailabilityExceptions() { return availabilityExceptions; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Resource other)) return false;
        return id != null && id.equals(other.id);
    }

    @Override
    public int hashCode() { return Objects.hashCode(getClass()); }
}
