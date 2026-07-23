package com.fastappoint.domain;

import jakarta.persistence.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

/**
 * The tenant boundary and the aggregate root for a business's configuration.
 * Resource types are never created explicitly: they are resolved by name through
 * {@link #resourceTypeNamed(String)}, which finds an existing one or creates it.
 * Both resources and service requirements go through that single door, so the type
 * vocabulary accretes from usage and is deduplicated per business.
 */
@Entity
@Table(name = "business")
public class Business {

    @Id
    private UUID id;

    @Column(nullable = false, unique = true)
    private String name;

    @OneToMany(mappedBy = "business", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ResourceType> resourceTypes = new ArrayList<>();

    @OneToMany(mappedBy = "business", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Resource> resources = new ArrayList<>();

    @OneToMany(mappedBy = "business", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<BusinessService> businessServices = new ArrayList<>();

    @OneToMany(mappedBy = "business", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Capability> capabilities = new ArrayList<>();

    protected Business() { // required by Hibernate
    }

    public Business(String name) {
        this.id = UUID.randomUUID();
        this.name = name;
    }

    public void rename(String name) {
        this.name = name;
    }

    /**
     * Find-or-create the type for this name within this business. Matching is
     * case-insensitive on the trimmed name; the first spelling seen is kept.
     * The DB unique constraint on (business_id, name) is the backstop against
     * duplicates created by concurrent requests.
     */
    public ResourceType resourceTypeNamed(String typeName) {
        String normalized = typeName.trim();
        for (ResourceType existing : resourceTypes) {
            if (existing.getName().equalsIgnoreCase(normalized)) {
                return existing;
            }
        }
        ResourceType created = new ResourceType(this, normalized);
        resourceTypes.add(created);
        return created;
    }

    /**
     * Add a resource, resolving its type by name (creating the type if new).
     * This is the primary path: the owner types the type name while adding the resource.
     */
    public Resource addResource(String resourceName, String typeName) {
        Resource resource = new Resource(this, resourceName, resourceTypeNamed(typeName));
        resources.add(resource);
        return resource;
    }

    /**
     * Add a resource using an explicitly pre-created type.
     */
    public Resource addResource(String resourceName, ResourceType type) {
        Resource resource = new Resource(this, resourceName, type);
        resources.add(resource);
        return resource;
    }

    public BusinessService addService(String serviceName, java.time.Duration duration) {
        BusinessService businessService = new BusinessService(this, serviceName, duration);
        businessServices.add(businessService);
        return businessService;
    }

    /**
     * Find-or-create a capability for this business.
     * Matching is case-insensitive on the trimmed name; the first spelling seen is kept.
     */
    public Capability capabilityNamed(String capabilityName) {
        String normalized = capabilityName.trim();
        for (Capability existing : capabilities) {
            if (existing.getName().equalsIgnoreCase(normalized)) {
                return existing;
            }
        }
        Capability created = new Capability(this, normalized);
        capabilities.add(created);
        return created;
    }

    public UUID getId() { return id; }
    public String getName() { return name; }
    public List<ResourceType> getResourceTypes() { return resourceTypes; }
    public List<Resource> getResources() { return resources; }
    public List<BusinessService> getServices() { return businessServices; }
    public List<Capability> getCapabilities() { return capabilities; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Business other)) return false;
        return id != null && id.equals(other.id);
    }

    @Override
    public int hashCode() { return Objects.hashCode(getClass()); }
}
