package com.fastappoint.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import jakarta.persistence.Id;

import java.util.Objects;
import java.util.UUID;

/**
 * Who the appointment is for. Unique per (business, phone) -- the same phone
 * number booking again within the same business reuses this row rather than
 * creating a duplicate customer.
 */
@Entity
@Table(name = "customer", uniqueConstraints =
        @UniqueConstraint(name = "uk_customer_business_phone", columnNames = {"business_id", "phone"}))
public class Customer {

    @Id
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "business_id", nullable = false)
    private Business business;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private String phone;

    protected Customer() { // required by Hibernate
    }

    public Customer(Business business, String name, String phone) {
        this.id = UUID.randomUUID();
        this.business = business;
        this.name = name;
        this.phone = phone;
    }

    public UUID getId() { return id; }
    public Business getBusiness() { return business; }
    public String getName() { return name; }
    public String getPhone() { return phone; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Customer other)) return false;
        return id != null && id.equals(other.id);
    }

    @Override
    public int hashCode() { return Objects.hashCode(getClass()); }
}
