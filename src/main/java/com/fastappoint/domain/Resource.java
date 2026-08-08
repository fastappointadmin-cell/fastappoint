package com.fastappoint.domain;

import jakarta.persistence.*;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

/**
 * A concrete bookable resource: "Marius" (a Barber), "Table 12" (a Table), "Ramp A" (a Ramp).
 * Availability is managed as explicit date-specific windows; capacity is kept for display/filtering.
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

    /** Optional numeric attribute (e.g. seats, station number) for display/filtering. */
    @Column
    private Integer capacity;

    /** Resources sharing a merge group may be combined to satisfy a larger capacity requirement. */
    @Column(length = 120)
    private String mergeGroup;

    /** Date-specific availability windows. */
    @OneToMany(mappedBy = "resource", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ResourceAvailability> availability = new ArrayList<>();

    @OneToMany(mappedBy = "resource", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ResourceAttributeValue> attributeValues = new ArrayList<>();


    protected Resource() { // required by Hibernate
    }

    Resource(Business business, String name, ResourceType type) {
        this.id = UUID.randomUUID();
        this.business = business;
        this.name = name;
        this.type = type;
    }

    public Resource withCapacity(Integer capacity) {
        this.capacity = capacity;
        return this;
    }

    public Resource withMergeGroup(String mergeGroup) {
        this.mergeGroup = mergeGroup;
        return this;
    }

    public ResourceAvailability addAvailability(LocalDate date, LocalTime start, LocalTime end) {
        // windows on the same day that overlap OR touch the new one
        List<ResourceAvailability> connected = availability.stream()
                .filter(a -> a.getDate().equals(date))
                .filter(a -> !start.isAfter(a.getEndTime()) && !a.getStartTime().isAfter(end))
                .toList();

        if (connected.isEmpty()) {
            ResourceAvailability created = new ResourceAvailability(this, date, start, end);
            availability.add(created);
            return created;
        }

        // expand to cover everything the new window connects to
        LocalTime mergedStart = start;
        LocalTime mergedEnd = end;
        for (ResourceAvailability a : connected) {
            if (a.getStartTime().isBefore(mergedStart)) mergedStart = a.getStartTime();
            if (a.getEndTime().isAfter(mergedEnd))     mergedEnd   = a.getEndTime();
        }

        // reuse the first row, drop the rest
        ResourceAvailability survivor = connected.getFirst();
        survivor.setStartTime(mergedStart);
        survivor.setEndTime(mergedEnd);
        availability.removeAll(connected.subList(1, connected.size()));

        return survivor;
    }

    public void rename(String name) {
        this.name = name;
    }

    public void changeType(ResourceType type) {
        this.type = type;
    }

    public void clearAttributeValues() {
        attributeValues.clear();
    }

    public void setAttributeValue(ResourceAttributeDefinition attributeDefinition, String value) {
        ResourceAttributeValue existing = attributeValues.stream()
                .filter(attribute -> attribute.getAttributeDefinition().equals(attributeDefinition))
                .findFirst()
                .orElse(null);
        if (existing != null) {
            existing.changeValue(value);
            return;
        }

        attributeValues.add(new ResourceAttributeValue(this, attributeDefinition, value));
    }

    public UUID getId() { return id; }
    public Business getBusiness() { return business; }
    public String getName() { return name; }
    public ResourceType getType() { return type; }
    public Integer getCapacity() { return capacity; }
    public String getMergeGroup() { return mergeGroup; }
    public List<ResourceAvailability> getAvailability() { return availability; }
    public List<ResourceAttributeValue> getAttributeValues() { return attributeValues; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Resource other)) return false;
        return id != null && id.equals(other.id);
    }

    @Override
    public int hashCode() { return Objects.hashCode(getClass()); }
}
