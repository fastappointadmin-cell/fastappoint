package com.fastappoint.solver;

import com.fastappoint.core.Interval;
import com.fastappoint.domain.Resource;
import com.fastappoint.domain.ServiceRequirement;

/**
 * One line of a proposed booking: a concrete resource bound to the requirement it
 * satisfies, over the interval it would be occupied. This is an intention, not a
 * persisted entity -- the booking service turns a plan into an Appointment +
 * ResourceAllocations. Keeping the solver free of entities makes it pure and testable.
 */
public record PlannedAllocation(ServiceRequirement requirement, Resource resource, Interval interval) {}
