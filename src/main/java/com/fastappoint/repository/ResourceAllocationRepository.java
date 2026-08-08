package com.fastappoint.repository;

import com.fastappoint.domain.ResourceAllocation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;
import java.util.UUID;

/**
 * Allocations are the source of "busy": each row is one resource occupied over one
 * interval. These queries return the blocks that overlap a given day window, which
 * the scheduling layer maps to Intervals and feeds to the AvailabilityCalculator/solver.
 *
 * Cancelled appointments are excluded -- a cancelled booking frees its resources.
 * Overlap is the standard half-open test: startTime < dayEnd AND endTime > dayStart.
 */
@Repository
public interface ResourceAllocationRepository extends JpaRepository<ResourceAllocation, UUID> {

    /** Busy blocks for a single resource on a day. Used by the single-resource availability path. */
    @Query("""
            select a from ResourceAllocation a
            where a.resource.id = :resourceId
              and a.appointment.status <> com.fastappoint.domain.AppointmentStatus.CANCELLED
              and a.startTime < :dayEnd
              and a.endTime > :dayStart
            """)
    List<ResourceAllocation> findBusy(@Param("resourceId") UUID resourceId,
                                      @Param("dayStart") LocalDateTime dayStart,
                                      @Param("dayEnd") LocalDateTime dayEnd);

    /** Busy blocks for a whole pool of resources in one query -- avoids N+1 in the solver. */
    @Query("""
            select a from ResourceAllocation a
            where a.resource.id in :resourceIds
              and a.appointment.status <> com.fastappoint.domain.AppointmentStatus.CANCELLED
              and a.startTime < :dayEnd
              and a.endTime > :dayStart
            """)
    List<ResourceAllocation> findBusyForResources(@Param("resourceIds") Collection<UUID> resourceIds,
                                                  @Param("dayStart") LocalDateTime dayStart,
                                                  @Param("dayEnd") LocalDateTime dayEnd);
}
