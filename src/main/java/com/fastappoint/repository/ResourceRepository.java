package com.fastappoint.repository;

import com.fastappoint.domain.Resource;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ResourceRepository extends JpaRepository<Resource, UUID> {
	List<Resource> findByBusinessId(UUID businessId);

	/**
	 * Blocks concurrent transactions from reading this resource's row until the current one commits/rolls back --
	 * used to close the check-then-act race between the availability check (findBusy) and saving the new
	 * allocation when creating a manual booking, so two overlapping bookings can't both pass the check at once.
	 */
	@Lock(LockModeType.PESSIMISTIC_WRITE)
	@Query("select r from Resource r where r.id = :id")
	Optional<Resource> findWithLockingById(@Param("id") UUID id);
}
