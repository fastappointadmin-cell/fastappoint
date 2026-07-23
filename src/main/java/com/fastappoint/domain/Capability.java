package com.fastappoint.domain;

import jakarta.persistence.*;

import java.util.Objects;
import java.util.UUID;

/**
 * A managed capability registry entry that can be assigned to resources
 * and required by service requirements. Capabilities are scoped to a business
 * and managed via find-or-create pattern like ResourceType.
 *
 * Examples: "speaks_english", "handicap_accessible", "wifi", "parking"
 */
@Entity
@Table(name = "capability", uniqueConstraints = {
        @UniqueConstraint(columnNames = {"business_id", "name"})
})
public class Capability {

    @Id
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "business_id", nullable = false)
    private Business business;

    @Column(nullable = false)
    private String name;

    @Column(length = 500)
    private String description;

    protected Capability() { // required by Hibernate
    }

    public Capability(Business business, String name) {
        this.id = UUID.randomUUID();
        this.business = business;
        this.name = name;
    }

    public UUID getId() { return id; }
    public Business getBusiness() { return business; }
    public String getName() { return name; }
    public String getDescription() { return description; }

    public void setDescription(String description) {
        this.description = description;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Capability other)) return false;
        return id != null && id.equals(other.id);
    }

    @Override
    public int hashCode() { return Objects.hashCode(getClass()); }
}

