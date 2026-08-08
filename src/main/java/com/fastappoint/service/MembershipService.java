package com.fastappoint.service;

import com.fastappoint.domain.AppUser;
import com.fastappoint.domain.Business;
import com.fastappoint.domain.BusinessMembership;
import com.fastappoint.domain.MembershipRole;
import com.fastappoint.exception.BusinessNotFoundException;
import com.fastappoint.exception.ForbiddenException;
import com.fastappoint.repository.AppUserRepository;
import com.fastappoint.repository.BusinessMembershipRepository;
import com.fastappoint.repository.BusinessRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
public class MembershipService {

    private final BusinessMembershipRepository memberships;
    private final AppUserRepository users;
    private final BusinessRepository businesses;

    public MembershipService(BusinessMembershipRepository memberships, AppUserRepository users,
                              BusinessRepository businesses) {
        this.memberships = memberships;
        this.users = users;
        this.businesses = businesses;
    }

    /** Every business-scoped controller method calls this first -- 403s if the caller has no membership
     * on the given business. This is what makes a plain businessId query param safe to trust again. */
    @Transactional(readOnly = true)
    public void requireMembership(UUID userId, UUID businessId) {
        if (!memberships.existsByUser_IdAndBusiness_Id(userId, businessId)) {
            throw new ForbiddenException("You don't have access to this business");
        }
    }

    @Transactional(readOnly = true)
    public List<BusinessMembership> findByUser(UUID userId) {
        return memberships.findByUser_Id(userId);
    }

    @Transactional
    public BusinessMembership grant(AppUser user, Business business, MembershipRole role) {
        return memberships.save(new BusinessMembership(user, business, role));
    }

    /** Convenience for controllers that only have ids on hand (e.g. granting the caller ownership of a
     * business they just created via the plain CRUD endpoint). */
    @Transactional
    public BusinessMembership grantByUserId(UUID userId, UUID businessId, MembershipRole role) {
        AppUser user = users.findById(userId)
                .orElseThrow(() -> new IllegalStateException("Authenticated user not found: " + userId));
        Business business = businesses.findById(businessId)
                .orElseThrow(() -> new BusinessNotFoundException("Business not found with ID: " + businessId));
        return grant(user, business, role);
    }
}
