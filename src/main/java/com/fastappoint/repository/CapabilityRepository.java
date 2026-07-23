package com.fastappoint.repository;

import com.fastappoint.domain.Capability;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface CapabilityRepository extends JpaRepository<Capability, UUID> {
    List<Capability> findByBusinessId(UUID businessId);
    Optional<Capability> findByBusinessIdAndNameIgnoreCase(UUID businessId, String name);
}

