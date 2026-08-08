package com.fastappoint.domain;

import jakarta.persistence.*;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

/**
 * Something a business offers ("Haircut", "Dinner reservation", "Brake change").
 * A service is defined entirely by its total duration + a list of requirements.
 */
@Entity
@Table(name = "business_service")
public class BusinessService {

    @Id
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "business_id", nullable = false)
    private Business business;

    @Column(nullable = false)
    private String name;

    /** Total wall-clock span the appointment occupies. */
    @Column(nullable = false)
    private Duration duration;

    @OneToMany(mappedBy = "businessService", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ServiceRequirement> requirements = new ArrayList<>();

    protected BusinessService() { // required by Hibernate
    }

    BusinessService(Business business, String name, Duration duration) {
        this.id = UUID.randomUUID();
        this.business = business;
        this.name = name;
        this.duration = duration;
    }

    public void rename(String name) {
        this.name = name;
    }

    public void changeDuration(Duration duration) {
        this.duration = duration;
    }

    /**
     * Add a requirement line: {@code quantity} resources of {@code type} are needed.
     * Returns the requirement so callers can chain advanced options (.withLabel(), .withNotes()).
     */
    public ServiceRequirement addRequirement(ResourceType type, int quantity) {
        ServiceRequirement req = new ServiceRequirement(this, type, quantity);
        requirements.add(req);
        return req;
    }

    public Optional<ServiceRequirement> findRequirement(UUID requirementId) {
        return requirements.stream()
                .filter(requirement -> requirement.getId().equals(requirementId))
                .findFirst();
    }

    public boolean removeRequirement(UUID requirementId) {
        return requirements.removeIf(requirement -> requirement.getId().equals(requirementId));
    }

    public UUID getId() { return id; }
    public Business getBusiness() { return business; }
    public String getName() { return name; }
    public Duration getDuration() { return duration; }
    public List<ServiceRequirement> getRequirements() { return requirements; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof BusinessService other)) return false;
        return id != null && id.equals(other.id);
    }

    @Override
    public int hashCode() { return Objects.hashCode(getClass()); }
}
