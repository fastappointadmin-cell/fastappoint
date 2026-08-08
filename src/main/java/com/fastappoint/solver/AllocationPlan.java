package com.fastappoint.solver;

import com.fastappoint.core.Interval;
import com.fastappoint.domain.BusinessService;

import java.util.List;

/**
 * A complete, feasible way to place one appointment: every requirement of the
 * service satisfied by concrete resources, over the appointment's interval.
 * The solver returns Optional.empty() instead of a partial plan -- a booking is
 * all-or-nothing (you cannot seat the party without also having a free waiter).
 */
public record AllocationPlan(BusinessService service, Interval appointment, List<PlannedAllocation> allocations) {}
