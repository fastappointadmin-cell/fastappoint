package com.fastappoint.domain;

import jakarta.persistence.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

/**
 * A category of resource, defined per business: "Barber", "Waiter", "Table".
 * Types are NOT created directly by the owner; they emerge the first time a name
 * is used (see Business#resourceTypeNamed) and are reused afterwards. The name is
 * therefore unique within a business, so find-or-create has a stable lookup key.
 */
@Entity
@Table(
    name = "resource_type",
    uniqueConstraints = @UniqueConstraint(
        name = "uq_resource_type_business_name",
        columnNames = {"business_id", "name"}
    )
)
public class ResourceType {

    @Id
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "business_id", nullable = false)
    private Business business;

    @Column(nullable = false)
    private String name;

    @OneToMany(mappedBy = "resourceType", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ResourceAttributeDefinition> attributeDefinitions = new ArrayList<>();

    protected ResourceType() { // required by Hibernate
    }

    ResourceType(Business business, String name) {
        this.id = UUID.randomUUID();
        this.business = business;
        this.name = name;
    }

    public void rename(String name) {
        this.name = name;
    }

    public ResourceAttributeDefinition addAttributeDefinition(
            String name,
            ResourceAttributeType type,
            boolean required,
            List<String> options
    ) {
        ResourceAttributeDefinition attributeDefinition = new ResourceAttributeDefinition(this, name, type, required);
        attributeDefinition.replaceOptions(options);
        attributeDefinitions.add(attributeDefinition);
        return attributeDefinition;
    }

    public java.util.Optional<ResourceAttributeDefinition> findAttributeDefinition(UUID attributeDefinitionId) {
        return attributeDefinitions.stream()
                .filter(attributeDefinition -> attributeDefinition.getId().equals(attributeDefinitionId))
                .findFirst();
    }

    public boolean removeAttributeDefinition(UUID attributeDefinitionId) {
        return attributeDefinitions.removeIf(attributeDefinition -> attributeDefinition.getId().equals(attributeDefinitionId));
    }

    public UUID getId() { return id; }
    public Business getBusiness() { return business; }
    public String getName() { return name; }
    public List<ResourceAttributeDefinition> getAttributeDefinitions() { return attributeDefinitions; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ResourceType other)) return false;
        return id != null && id.equals(other.getId());
    }

    @Override
    public int hashCode() { return Objects.hashCode(getClass()); }
}
