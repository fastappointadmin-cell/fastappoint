package com.fastappoint.domain;

import com.fastappoint.core.AllocationMode;
import jakarta.persistence.*;

import java.time.Duration;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;

/**
 * Something a business offers ("Haircut", "Dinner reservation", "Brake change").
 * A service is defined entirely by its total duration + a list of requirements.
 * There is deliberately NO single allocation strategy here: strategy lives per
 * requirement line, because one service can mix modes.
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

    /** Fixed-count line: SINGLE (n=1) or MULTIPLE (n>1) resources of a type. */
    public ServiceRequirement require(ResourceType type, AllocationMode mode, int quantity) {
        ServiceRequirement req = ServiceRequirement.fixed(this, type, mode, quantity);
        requirements.add(req);
        return req;
    }

    /** MERGE line whose demand is supplied at booking time (e.g. "partySize"). */
    public ServiceRequirement requireMerged(ResourceType type, String demandParameter) {
        ServiceRequirement req = ServiceRequirement.merged(this, type, demandParameter);
        requirements.add(req);
        return req;
    }

    public UUID getId() { return id; }
    public Business getBusiness() { return business; }
    public String getName() { return name; }
    public Duration getDuration() { return duration; }
    public List<ServiceRequirement> getRequirements() { return requirements; }

    /**
     * Names of the booking inputs this service needs from the user, derived from its
     * requirements (e.g. a table MERGE line contributes "partySize"). Lets a chatbot
     * discover which questions to ask, generically, with no per-vertical hardcoding.
     */
    public Set<String> requiredInputParameters() {
        Set<String> params = new HashSet<>();
        for (ServiceRequirement r : requirements) {
            if (r.getDemandParameter() != null) {
                params.add(r.getDemandParameter());
            }
        }
        return params;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof BusinessService other)) return false;
        return id != null && id.equals(other.id);
    }

    @Override
    public int hashCode() { return Objects.hashCode(getClass()); }
}
