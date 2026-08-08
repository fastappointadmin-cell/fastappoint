package com.fastappoint.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

import java.time.LocalDateTime;
import java.util.Objects;
import java.util.UUID;

/**
 * Grants an {@link AppUser} access to a {@link Business} -- the join row behind "multiple staff
 * logins per business." Every authorization check in the app (does this caller get to touch this
 * business's data?) goes through the existence of a row here.
 */
@Entity
@Table(name = "business_membership", uniqueConstraints =
        @UniqueConstraint(name = "uk_membership_user_business", columnNames = {"user_id", "business_id"}))
public class BusinessMembership {

    @Id
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private AppUser user;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "business_id", nullable = false)
    private Business business;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 16)
    private MembershipRole role;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    protected BusinessMembership() { // required by Hibernate
    }

    public BusinessMembership(AppUser user, Business business, MembershipRole role) {
        this.id = UUID.randomUUID();
        this.user = user;
        this.business = business;
        this.role = role;
        this.createdAt = LocalDateTime.now();
    }

    public UUID getId() { return id; }
    public AppUser getUser() { return user; }
    public Business getBusiness() { return business; }
    public MembershipRole getRole() { return role; }
    public LocalDateTime getCreatedAt() { return createdAt; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof BusinessMembership other)) return false;
        return id != null && id.equals(other.id);
    }

    @Override
    public int hashCode() { return Objects.hashCode(getClass()); }
}
