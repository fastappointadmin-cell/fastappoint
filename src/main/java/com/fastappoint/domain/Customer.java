package com.fastappoint.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;

/**
 * Who the appointment is for. Embedded for now; promote to its own entity once
 * you need repeat-customer history, profiles, or dedup by phone.
 */
@Embeddable
public class Customer {

    @Column(name = "customer_name")
    private String name;

    @Column(name = "customer_phone")
    private String phone;

    protected Customer() { // required by Hibernate
    }

    public Customer(String name, String phone) {
        this.name = name;
        this.phone = phone;
    }

    public String getName() { return name; }
    public String getPhone() { return phone; }
}
