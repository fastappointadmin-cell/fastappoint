package com.fastappoint.solver;

import com.fastappoint.availability.AvailabilityCalculator;
import com.fastappoint.core.Interval;
import com.fastappoint.core.Intervals;
import com.fastappoint.domain.BusinessService;
import com.fastappoint.domain.Resource;
import com.fastappoint.domain.ResourceAttributeType;
import com.fastappoint.domain.ServiceRequirement;
import com.fastappoint.domain.ServiceRequirementConstraint;
import com.fastappoint.domain.ServiceRequirementConstraintOperator;
import com.fastappoint.domain.ServiceRequirementFulfillmentMode;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

/**
 * Pure, stateless booking solver. Given a service and a start time, finds a concrete
 * assignment of resources to each requirement line or reports infeasibility.
 *
 * How each requirement is satisfied:
 *   - A resource MATCHES a line when its type equals the required type.
 *   - It is USABLE when it is free for the line's occupation interval and not already
 *     taken by an earlier line of the same appointment.
 *   - {@code quantity} resources are selected per line (default 1).
 *
 * Backtracking is used to resolve conflicts across requirement lines.
 */
public final class AllocationSolver {

    private AllocationSolver() {}

    /**
     * Try to place {@code service} at {@code start}. Returns a concrete plan or empty.
     *
     * @param preferred        resource ids the customer prefers; honoured when free
     * @param pool             all resources that could conceivably serve (the business's resources)
     * @param busyByResourceId each resource's already-occupied intervals on that day
     */
    public static Optional<AllocationPlan> solve(
            BusinessService service,
            LocalDateTime start,
            Set<UUID> preferred,
            List<Resource> pool,
            Map<UUID, List<Interval>> busyByResourceId,
            Map<String, Integer> inputs) {
        return solve(service, start, preferred, pool, busyByResourceId, false, inputs);
    }

    /**
     * Same as {@link #solve(BusinessService, LocalDateTime, Set, List, Map)}, but when
     * {@code requirePreferred} is true, any requirement line that a preferred resource could satisfy
     * (same type) is restricted to ONLY preferred candidates, instead of merely trying them first.
     * Used by {@link #feasibleStarts} so "available starts" for a chosen resource reflect that
     * resource's own availability, not just "some resource of the same type is free" -- a soft
     * preference is the right call at actual booking time (a slot already known-feasible should still
     * book even if the preferred resource got taken in the meantime), but wrong for "show me when this
     * resource is free," which is what a caller asking for feasible starts with a preferred resource means.
     */
    private static Optional<AllocationPlan> solve(
            BusinessService service,
            LocalDateTime start,
            Set<UUID> preferred,
            List<Resource> pool,
            Map<UUID, List<Interval>> busyByResourceId,
            boolean requirePreferred,
            Map<String, Integer> inputs) {

        Interval appointment = Interval.of(start, start.plus(service.getDuration()));

        List<RequirementContext> contexts = new ArrayList<>();
        int order = 0;

        for (ServiceRequirement requirement : service.getRequirements()) {
            Interval occupation = occupationInterval(start, service, requirement);
            RequirementDemand demand = resolveDemand(requirement, inputs);

            List<Resource> candidates = new ArrayList<>();
            for (Resource resource : pool) {
                if (!matchesType(resource, requirement)) continue;
                if (!isFree(resource, start.toLocalDate(), occupation, busyByResourceId)) continue;
                candidates.add(resource);
            }
            if (requirePreferred && preferred != null && !preferred.isEmpty()
                    && demand.mode() == ServiceRequirementFulfillmentMode.QUANTITY) {
                boolean lineHasPreferredCandidate = pool.stream()
                        .anyMatch(resource -> matchesType(resource, requirement) && preferred.contains(resource.getId()));
                if (lineHasPreferredCandidate) {
                    candidates.removeIf(resource -> !preferred.contains(resource.getId()));
                }
            }
            preferredFirst(candidates, preferred);

            List<List<Resource>> options = requirementOptions(candidates, demand, preferred, requirePreferred);
            if (options.isEmpty()) {
                return Optional.empty(); // this line is unsatisfiable -> booking infeasible
            }
            contexts.add(new RequirementContext(order++, requirement, occupation, options));
        }

        // Sort by fewest options first (most-constrained-variable heuristic)
        contexts.sort(Comparator.comparingInt(ctx -> ctx.options().size()));

        List<AssignedAllocation> chosen = new ArrayList<>();
        if (!search(contexts, 0, new HashSet<>(), chosen)) {
            return Optional.empty();
        }

        chosen.sort(Comparator.comparingInt(AssignedAllocation::order)
                .thenComparing(a -> a.allocation().resource().getName()));

        List<PlannedAllocation> planned = chosen.stream()
                .map(AssignedAllocation::allocation)
                .toList();
        return Optional.of(new AllocationPlan(service, appointment, planned));
    }

    /**
     * Every start on {@code date} at which the service can be booked, stepping by {@code step}.
     */
    public static List<LocalDateTime> feasibleStarts(
            BusinessService service,
            LocalDate date,
            Set<UUID> preferred,
            List<Resource> pool,
            Map<UUID, List<Interval>> busyByResourceId,
            Duration step,
            Map<String, Integer> inputs) {

        LocalDateTime earliest = null;
        LocalDateTime latest = null;
        for (Resource resource : pool) {
            for (Interval window : AvailabilityCalculator.windowsFor(resource, date)) {
                if (earliest == null || window.start().isBefore(earliest)) earliest = window.start();
                if (latest == null || window.end().isAfter(latest)) latest = window.end();
            }
        }
        if (earliest == null) return List.of();

        boolean requirePreferred = preferred != null && !preferred.isEmpty();
        List<LocalDateTime> starts = new ArrayList<>();
        Duration duration = service.getDuration();
        for (LocalDateTime t = earliest; !t.plus(duration).isAfter(latest); t = t.plus(step)) {
            if (solve(service, t, preferred, pool, busyByResourceId, requirePreferred, inputs).isPresent()) {
                starts.add(t);
            }
        }
        return starts;
    }

    // --- internals ---

    private static Interval occupationInterval(LocalDateTime start, BusinessService service, ServiceRequirement req) {
        return Interval.of(start, start.plus(service.getDuration()));
    }

    private static boolean matchesType(Resource resource, ServiceRequirement req) {
        return resource.getType().equals(req.getResourceType()) && matchesConstraints(resource, req);
    }

    private static RequirementDemand resolveDemand(ServiceRequirement requirement, Map<String, Integer> inputs) {
        if (requirement.getFulfillmentMode() == ServiceRequirementFulfillmentMode.QUANTITY) {
            return new RequirementDemand(ServiceRequirementFulfillmentMode.QUANTITY, requirement.getQuantity(), null);
        }

        String inputKey = requirement.getCapacityInputKey();
        if (inputKey != null && !inputKey.isBlank()) {
            Integer inputValue = inputs.get(inputKey);
            if (inputValue == null) {
                throw new IllegalArgumentException(
                        "Booking input \"" + inputKey + "\" is required for " + requirement.getResourceType().getName());
            }
            if (inputValue <= 0) {
                throw new IllegalArgumentException("Booking input \"" + inputKey + "\" must be positive");
            }
            return new RequirementDemand(ServiceRequirementFulfillmentMode.CAPACITY, 1, inputValue);
        }

        Integer requiredCapacity = requirement.getRequiredCapacity();
        if (requiredCapacity == null || requiredCapacity <= 0) {
            throw new IllegalArgumentException(
                    "Capacity requirement for " + requirement.getResourceType().getName() + " is not configured correctly");
        }
        return new RequirementDemand(ServiceRequirementFulfillmentMode.CAPACITY, 1, requiredCapacity);
    }

    private static List<List<Resource>> requirementOptions(
            List<Resource> candidates,
            RequirementDemand demand,
            Set<UUID> preferred,
            boolean requirePreferred
    ) {
        List<List<Resource>> options = demand.mode() == ServiceRequirementFulfillmentMode.CAPACITY
                ? capacityOptions(candidates, demand.requiredCapacity(), preferred)
                : combinations(candidates, demand.quantity());

        if (demand.mode() == ServiceRequirementFulfillmentMode.CAPACITY
                && requirePreferred
                && preferred != null
                && !preferred.isEmpty()) {
            List<List<Resource>> preferredOptions = options.stream()
                    .filter(option -> option.stream().anyMatch(resource -> preferred.contains(resource.getId())))
                    .toList();
            if (!preferredOptions.isEmpty()) {
                return preferredOptions;
            }
        }

        return options;
    }

    private static List<List<Resource>> capacityOptions(
            List<Resource> candidates,
            Integer requiredCapacity,
            Set<UUID> preferred
    ) {
        if (requiredCapacity == null || requiredCapacity <= 0) {
            return List.of();
        }

        List<List<Resource>> options = new ArrayList<>();

        for (Resource resource : candidates) {
            Integer capacity = resource.getCapacity();
            if (capacity != null && capacity >= requiredCapacity) {
                options.add(List.of(resource));
            }
        }

        Map<String, List<Resource>> candidatesByMergeGroup = new LinkedHashMap<>();
        for (Resource resource : candidates) {
            String mergeGroup = resource.getMergeGroup();
            if (mergeGroup == null || mergeGroup.isBlank() || resource.getCapacity() == null) {
                continue;
            }
            candidatesByMergeGroup.computeIfAbsent(mergeGroup, ignored -> new ArrayList<>()).add(resource);
        }

        for (List<Resource> groupedCandidates : candidatesByMergeGroup.values()) {
            collectCapacityOptions(groupedCandidates, requiredCapacity, 0, new ArrayList<>(), 0, options);
        }

        options.sort(
                Comparator.<List<Resource>>comparingInt(List::size)
                        .thenComparingInt(option -> capacitySlack(option, requiredCapacity))
                        .thenComparingInt(option -> preferredScore(option, preferred))
        );
        return options;
    }

    private static void collectCapacityOptions(
            List<Resource> candidates,
            int requiredCapacity,
            int index,
            List<Resource> current,
            int currentCapacity,
            List<List<Resource>> result
    ) {
        if (currentCapacity >= requiredCapacity) {
            if (current.size() > 1) {
                result.add(List.copyOf(current));
            }
            return;
        }
        if (index >= candidates.size()) {
            return;
        }

        Resource candidate = candidates.get(index);
        Integer candidateCapacity = candidate.getCapacity();

        if (candidateCapacity != null) {
            current.add(candidate);
            collectCapacityOptions(
                    candidates,
                    requiredCapacity,
                    index + 1,
                    current,
                    currentCapacity + candidateCapacity,
                    result
            );
            current.removeLast();
        }

        collectCapacityOptions(candidates, requiredCapacity, index + 1, current, currentCapacity, result);
    }

    private static int capacitySlack(List<Resource> option, int requiredCapacity) {
        return option.stream()
                .map(Resource::getCapacity)
                .filter(Objects::nonNull)
                .mapToInt(Integer::intValue)
                .sum() - requiredCapacity;
    }

    private static int preferredScore(List<Resource> option, Set<UUID> preferred) {
        if (preferred == null || preferred.isEmpty()) {
            return 1;
        }
        return option.stream().anyMatch(resource -> preferred.contains(resource.getId())) ? 0 : 1;
    }

    private static boolean matchesConstraints(Resource resource, ServiceRequirement req) {
        for (ServiceRequirementConstraint constraint : req.getConstraints()) {
            String actualValue = resource.getAttributeValues().stream()
                    .filter(value -> value.getAttributeDefinition().getId().equals(constraint.getAttributeDefinition().getId()))
                    .map(value -> value.getValue())
                    .findFirst()
                    .orElse(null);
            if (actualValue == null || !matchesConstraint(actualValue, constraint)) {
                return false;
            }
        }
        return true;
    }

    private static boolean matchesConstraint(String actualValue, ServiceRequirementConstraint constraint) {
        ResourceAttributeType type = constraint.getAttributeDefinition().getType();
        String expectedValue = constraint.getExpectedValue();
        ServiceRequirementConstraintOperator operator = constraint.getOperator();

        return switch (type) {
            case TEXT -> switch (operator) {
                case EQUALS -> actualValue.equalsIgnoreCase(expectedValue);
                case CONTAINS -> actualValue.toLowerCase().contains(expectedValue.toLowerCase());
                default -> false;
            };
            case NUMBER -> {
                double actual = Double.parseDouble(actualValue);
                double expected = Double.parseDouble(expectedValue);
                yield switch (operator) {
                    case EQUALS -> Double.compare(actual, expected) == 0;
                    case GREATER_THAN_OR_EQUAL -> actual >= expected;
                    case LESS_THAN_OR_EQUAL -> actual <= expected;
                    default -> false;
                };
            }
            case BOOLEAN, SINGLE_SELECT -> operator == ServiceRequirementConstraintOperator.EQUALS
                    && actualValue.equalsIgnoreCase(expectedValue);
        };
    }

    private static boolean isFree(Resource resource, LocalDate date, Interval occupation,
                                   Map<UUID, List<Interval>> busyByResourceId) {
        List<Interval> busy = busyByResourceId.getOrDefault(resource.getId(), List.of());
        List<Interval> free = AvailabilityCalculator.freeIntervals(resource, date, busy);
        return Intervals.covers(free, occupation);
    }

    private static void preferredFirst(List<Resource> candidates, Set<UUID> preferred) {
        if (preferred == null || preferred.isEmpty()) return;
        candidates.sort(Comparator.comparing(r -> preferred.contains(r.getId()) ? 0 : 1));
    }

    /**
     * Generate all combinations of {@code n} resources from the candidates list.
     * When n == 1 this degenerates to a single-element list per candidate (fast path).
     */
    private static List<List<Resource>> combinations(List<Resource> candidates, int n) {
        if (n == 1) {
            return candidates.stream().map(List::of).toList();
        }
        List<List<Resource>> result = new ArrayList<>();
        collectCombinations(candidates, n, 0, new ArrayList<>(), result);
        return result;
    }

    private static void collectCombinations(List<Resource> candidates, int n, int index,
                                            List<Resource> current, List<List<Resource>> result) {
        if (current.size() == n) {
            result.add(List.copyOf(current));
            return;
        }
        if (index >= candidates.size()) return;
        // remaining elements not enough to complete the combination
        if (candidates.size() - index < n - current.size()) return;

        current.add(candidates.get(index));
        collectCombinations(candidates, n, index + 1, current, result);
        current.removeLast();
        collectCombinations(candidates, n, index + 1, current, result);
    }

    private static boolean search(List<RequirementContext> contexts, int index, Set<UUID> used,
                                   List<AssignedAllocation> chosen) {
        if (index >= contexts.size()) return true;

        RequirementContext ctx = contexts.get(index);
        for (List<Resource> option : ctx.options()) {
            if (conflicts(option, used)) continue;

            markUsed(option, used);
            for (Resource resource : option) {
                chosen.add(new AssignedAllocation(ctx.order(),
                        new PlannedAllocation(ctx.requirement(), resource, ctx.occupation())));
            }

            if (search(contexts, index + 1, used, chosen)) return true;

            unmarkUsed(option, used);
            for (int i = 0; i < option.size(); i++) chosen.removeLast();
        }
        return false;
    }

    private static boolean conflicts(List<Resource> option, Set<UUID> used) {
        for (Resource resource : option) {
            if (used.contains(resource.getId())) return true;
        }
        return false;
    }

    private static void markUsed(List<Resource> option, Set<UUID> used) {
        for (Resource resource : option) used.add(resource.getId());
    }

    private static void unmarkUsed(List<Resource> option, Set<UUID> used) {
        for (Resource resource : option) used.remove(resource.getId());
    }

    private record RequirementContext(int order, ServiceRequirement requirement, Interval occupation,
                                      List<List<Resource>> options) {}

    private record AssignedAllocation(int order, PlannedAllocation allocation) {}
    private record RequirementDemand(ServiceRequirementFulfillmentMode mode, int quantity, Integer requiredCapacity) {}
}
