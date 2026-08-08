package com.fastappoint.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

import java.util.Objects;
import java.util.UUID;

@Entity
@Table(name = "resource_attribute_value")
public class ResourceAttributeValue {

    @Id
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "resource_id", nullable = false)
    private Resource resource;

    @ManyToOne(fetch = FetchType.EAGER, optional = false)
    @JoinColumn(name = "attribute_definition_id", nullable = false)
    private ResourceAttributeDefinition attributeDefinition;

    @Column(length = 1000)
    private String value;

    protected ResourceAttributeValue() {
    }

    ResourceAttributeValue(Resource resource, ResourceAttributeDefinition attributeDefinition, String value) {
        this.id = UUID.randomUUID();
        this.resource = resource;
        this.attributeDefinition = attributeDefinition;
        this.value = value;
    }

    public void changeValue(String value) {
        this.value = value;
    }

    public UUID getId() { return id; }
    public Resource getResource() { return resource; }
    public ResourceAttributeDefinition getAttributeDefinition() { return attributeDefinition; }
    public String getValue() { return value; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ResourceAttributeValue other)) return false;
        return id != null && id.equals(other.id);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(getClass());
    }
}
