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

import java.util.Objects;
import java.util.UUID;

/** Links an {@link AppUser} to an external OAuth identity (one row per provider the user signed in with). */
@Entity
@Table(name = "oauth_identity", uniqueConstraints =
        @UniqueConstraint(name = "uk_oauth_identity_provider_subject", columnNames = {"provider", "provider_user_id"}))
public class OAuthIdentity {

    @Id
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private AppUser user;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 16)
    private OAuthProvider provider;

    @Column(name = "provider_user_id", nullable = false)
    private String providerUserId;

    protected OAuthIdentity() { // required by Hibernate
    }

    public OAuthIdentity(AppUser user, OAuthProvider provider, String providerUserId) {
        this.id = UUID.randomUUID();
        this.user = user;
        this.provider = provider;
        this.providerUserId = providerUserId;
    }

    public UUID getId() { return id; }
    public AppUser getUser() { return user; }
    public OAuthProvider getProvider() { return provider; }
    public String getProviderUserId() { return providerUserId; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof OAuthIdentity other)) return false;
        return id != null && id.equals(other.id);
    }

    @Override
    public int hashCode() { return Objects.hashCode(getClass()); }
}
