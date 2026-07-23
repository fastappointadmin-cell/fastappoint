package com.fastappoint.domain;

import com.fastappoint.core.AllocationMode;
import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

import java.time.Duration;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;

/**
 * One line in a service's resource needs. This is the universal booking primitive:
 * "I need {mode} resources of {resourceType} that also have {requiredCapabilities},
 *  occupying {occupationDuration} of the appointment."
 *
 * The predicate a resource must satisfy = resourceType + requiredCapabilities
 *   (+ capacity, interpreted by MERGE).
 * The quantity rule = mode + (quantity | demandParameter).
 *   - SINGLE / MULTIPLE : quantity is a fixed count of distinct resources.
 *   - MERGE             : demand = quantity (fixed) OR the runtime value named by
 *                         demandParameter; satisfied when summed capacity >= demand.
 */
@Entity
@Table(name = "service_requirement")
public class ServiceRequirement {

    @Id
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "service_id", nullable = false)
    private BusinessService businessService;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "resource_type_id", nullable = false)
    private ResourceType resourceType;

    /** Extra predicate: a matching resource must have ALL of these capabilities. */
    @ManyToMany
    @JoinTable(
            name = "requirement_capability",
            joinColumns = @JoinColumn(name = "requirement_id"),
            inverseJoinColumns = @JoinColumn(name = "capability_id")
    )
    private Set<Capability> requiredCapabilities = new HashSet<>();

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 16)
    private AllocationMode mode;

    /** Fixed count (SINGLE/MULTIPLE) or fixed demand (MERGE). null when demand is dynamic. */
    @Column
    private Integer quantity;

    /** If set, the demand for this line is read from this booking input at reservation time. */
    @Column(name = "demand_parameter")
    private String demandParameter;

    /**
     * How long this resource is tied up, measured from appointment start.
     * null => occupies the full service duration.
     * (Lets a mechanic occupy 2h while the ramp occupies the whole slot.)
     */
    @Column(name = "occupation_duration")
    private Duration occupationDuration;

    protected ServiceRequirement() { // required by Hibernate
    }

    private ServiceRequirement(BusinessService businessService, ResourceType resourceType, AllocationMode mode,
                               Integer quantity, String demandParameter) {
        this.id = UUID.randomUUID();
        this.businessService = businessService;
        this.resourceType = resourceType;
        this.mode = mode;
        this.quantity = quantity;
        this.demandParameter = demandParameter;
    }

    static ServiceRequirement fixed(BusinessService businessService, ResourceType type, AllocationMode mode, int quantity) {
        return new ServiceRequirement(businessService, type, mode, quantity, null);
    }

    static ServiceRequirement merged(BusinessService businessService, ResourceType type, String demandParameter) {
        return new ServiceRequirement(businessService, type, AllocationMode.MERGE, null, demandParameter);
    }

    public ServiceRequirement withCapability(Capability capability) {
        this.requiredCapabilities.add(capability);
        return this;
    }

    public ServiceRequirement removeCapability(Capability capability) {
        this.requiredCapabilities.remove(capability);
        return this;
    }

    public ServiceRequirement withOccupationDuration(Duration occupationDuration) {
        this.occupationDuration = occupationDuration;
        return this;
    }

    public boolean hasDynamicDemand() { return demandParameter != null; }

    public UUID getId() { return id; }
    public BusinessService getService() { return businessService; }
    public ResourceType getResourceType() { return resourceType; }
    public Set<Capability> getRequiredCapabilities() { return requiredCapabilities; }
    public AllocationMode getMode() { return mode; }
    public Integer getQuantity() { return quantity; }
    public String getDemandParameter() { return demandParameter; }
    public Duration getOccupationDuration() { return occupationDuration; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ServiceRequirement other)) return false;
        return id != null && id.equals(other.id);
    }

    @Override
    public int hashCode() { return Objects.hashCode(getClass()); }
}
