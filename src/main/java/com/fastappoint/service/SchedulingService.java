package com.fastappoint.service;

import com.fastappoint.core.Interval;
import com.fastappoint.domain.BusinessService;
import com.fastappoint.domain.Resource;
import com.fastappoint.exception.InvalidAppointmentException;
import com.fastappoint.domain.ResourceAllocation;
import com.fastappoint.repository.ResourceAllocationRepository;
import com.fastappoint.repository.ResourceRepository;
import com.fastappoint.repository.ServiceRepository;
import com.fastappoint.solver.AllocationPlan;
import com.fastappoint.solver.AllocationSolver;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

/**
 * The bridge between persistence and the pure {@link AllocationSolver}: it loads the
 * service, its resource pool, and their busy intervals for a day, then delegates all
 * scheduling logic to the solver. No booking rules live here -- this class only
 * assembles the solver's inputs and hands back its answers.
 *
 * Uses your existing repositories:
 *   ServiceRepository.findById(UUID)                            -> the BusinessService
 *   ResourceRepository.findByBusinessId(UUID)                   -> the candidate pool
 *   ResourceAllocationRepository.findBusyForResources(ids,..)   -> the pool's busy blocks that day
 */
@Service
public class SchedulingService {

    private final ServiceRepository services;
    private final ResourceRepository resources;
    private final ResourceAllocationRepository allocations;

    public SchedulingService(ServiceRepository services,
                             ResourceRepository resources,
                             ResourceAllocationRepository allocations) {
        this.services = services;
        this.resources = resources;
        this.allocations = allocations;
    }

    /** Bookable start times for a service on a date -- backs the availability endpoint. */
    @Transactional(readOnly = true)
    public List<LocalDateTime> availableStarts(UUID serviceId, LocalDate date,
                                               Set<UUID> preferred, Duration step,
                                               Map<String, Integer> inputs) {
        BusinessService service = loadService(serviceId);
        List<Resource> pool = resources.findByBusinessId(service.getBusiness().getId());
        Map<UUID, List<Interval>> busy = busyByResource(pool, date);
        try {
            return AllocationSolver.feasibleStarts(service, date, preferred, pool, busy, step, safeInputs(inputs));
        } catch (IllegalArgumentException ex) {
            throw new InvalidAppointmentException(ex.getMessage(), ex);
        }
    }

    /** A concrete allocation plan for a specific start -- what the booking flow consumes. */
    @Transactional(readOnly = true)
    public Optional<AllocationPlan> plan(UUID serviceId, LocalDateTime start, Set<UUID> preferred, Map<String, Integer> inputs) {
        BusinessService service = loadService(serviceId);
        List<Resource> pool = resources.findByBusinessId(service.getBusiness().getId());
        Map<UUID, List<Interval>> busy = busyByResource(pool, start.toLocalDate());
        try {
            return AllocationSolver.solve(service, start, preferred, pool, busy, safeInputs(inputs));
        } catch (IllegalArgumentException ex) {
            throw new InvalidAppointmentException(ex.getMessage(), ex);
        }
    }

    private BusinessService loadService(UUID serviceId) {
        return services.findById(serviceId)
                .orElseThrow(() -> new IllegalArgumentException("Unknown service: " + serviceId));
    }

    private Map<UUID, List<Interval>> busyByResource(List<Resource> pool, LocalDate date) {
        if (pool.isEmpty()) {
            return Map.of();
        }
        LocalDateTime dayStart = date.atStartOfDay();
        LocalDateTime dayEnd = date.plusDays(1).atStartOfDay();

        List<UUID> ids = pool.stream().map(Resource::getId).toList();
        Map<UUID, List<Interval>> busy = new HashMap<>();
        for (UUID id : ids) {
            busy.put(id, new ArrayList<>()); // every resource present, even with no bookings
        }
        for (ResourceAllocation allocation : allocations.findBusyForResources(ids, dayStart, dayEnd)) {
            busy.get(allocation.getResource().getId())
                    .add(Interval.of(allocation.getStartTime(), allocation.getEndTime()));
        }
        return busy;
    }

    private Map<String, Integer> safeInputs(Map<String, Integer> inputs) {
        return inputs == null ? Map.of() : new HashMap<>(inputs);
    }
}
