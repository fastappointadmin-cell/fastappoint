package com.fastappoint.repository;

import com.fastappoint.domain.Resource;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface ResourceRepository extends JpaRepository<Resource, UUID> {
	List<Resource> findByBusinessId(UUID businessId);
}
