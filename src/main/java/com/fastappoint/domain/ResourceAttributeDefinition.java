package com.fastappoint.domain;

import jakarta.persistence.CascadeType;
import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OrderColumn;
import jakarta.persistence.Table;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

@Entity
@Table(name = "resource_attribute_definition")
public class ResourceAttributeDefinition {

    @Id
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "resource_type_id", nullable = false)
    private ResourceType resourceType;

    @Column(nullable = false)
    private String name;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ResourceAttributeType type;

    @Column(nullable = false)
    private boolean required;

    @ElementCollection
    @CollectionTable(name = "resource_attribute_definition_option",
            joinColumns = @JoinColumn(name = "attribute_definition_id"))
    @OrderColumn(name = "option_order")
    @Column(name = "option_value", nullable = false)
    private List<String> options = new ArrayList<>();

    @OneToMany(mappedBy = "attributeDefinition", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ResourceAttributeValue> values = new ArrayList<>();

    @OneToMany(mappedBy = "attributeDefinition", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ServiceRequirementConstraint> constraints = new ArrayList<>();

    protected ResourceAttributeDefinition() {
    }

    ResourceAttributeDefinition(ResourceType resourceType, String name, ResourceAttributeType type, boolean required) {
        this.id = UUID.randomUUID();
        this.resourceType = resourceType;
        this.name = name;
        this.type = type;
        this.required = required;
    }

    public void rename(String name) {
        this.name = name;
    }

    public void changeType(ResourceAttributeType type) {
        this.type = type;
    }

    public void setRequired(boolean required) {
        this.required = required;
    }

    public void replaceOptions(List<String> nextOptions) {
        this.options.clear();
        this.options.addAll(nextOptions);
    }

    public UUID getId() { return id; }
    public ResourceType getResourceType() { return resourceType; }
    public String getName() { return name; }
    public ResourceAttributeType getType() { return type; }
    public boolean isRequired() { return required; }
    public List<String> getOptions() { return options; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ResourceAttributeDefinition other)) return false;
        return id != null && id.equals(other.id);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(getClass());
    }
}
