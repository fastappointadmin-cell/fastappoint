package com.fastappoint.repository;

import com.fastappoint.domain.BusinessMembership;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface BusinessMembershipRepository extends JpaRepository<BusinessMembership, UUID> {
    List<BusinessMembership> findByUser_Id(UUID userId);
    Optional<BusinessMembership> findByUser_IdAndBusiness_Id(UUID userId, UUID businessId);
    boolean existsByUser_IdAndBusiness_Id(UUID userId, UUID businessId);
}
