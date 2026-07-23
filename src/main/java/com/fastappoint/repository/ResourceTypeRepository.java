package com.fastappoint.repository;

import com.fastappoint.domain.ResourceType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface ResourceTypeRepository extends JpaRepository<ResourceType, UUID> {
    List<ResourceType> findByBusinessId(UUID businessId);
    Optional<ResourceType> findByBusinessIdAndNameIgnoreCase(UUID businessId, String name);
}

