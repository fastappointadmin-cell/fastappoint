package com.fastappoint.availability;

import com.fastappoint.core.Interval;
import com.fastappoint.core.Intervals;
import com.fastappoint.domain.AvailabilityException;
import com.fastappoint.domain.Resource;
import com.fastappoint.domain.ResourceAvailability;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * Free/busy math for a SINGLE resource on a SINGLE day. Pure and stateless: no
 * persistence, no Spring. This is the atom the whole system stands on -- the
 * multi-resource solver runs it per candidate and intersects the results, the
 * availability endpoint reads it, and the booking service re-checks against it.
 *
 * Busy intervals are supplied by the caller (already scoped to this resource),
 * because "which allocations belong to this resource on this date" is a repository
 * query, not calculation. Mapping ResourceAllocation -> Interval is a one-liner the
 * booking service owns.
 *
 * Scope note: windows are assumed to lie within a single calendar day
 * (end-of-day > start-of-day). Overnight windows crossing midnight are out of
 * scope for now and would be modelled as two windows on adjacent days.
 */
public final class AvailabilityCalculator {

    private AvailabilityCalculator() {}

    /**
     * The offered windows for this resource on this date, before bookings.
     * A date-specific {@link AvailabilityException} takes precedence: closed -> no
     * windows; custom hours -> exactly those hours. Otherwise the recurring weekly
     * windows for that day-of-week apply.
     */
    public static List<Interval> windowsFor(Resource resource, LocalDate date) {
        AvailabilityException override = exceptionFor(resource, date);
        if (override != null) {
            if (override.isClosed()) {
                return List.of();
            }
            return List.of(Interval.of(
                    date.atTime(override.getStartTime()),
                    date.atTime(override.getEndTime())));
        }

        DayOfWeek dayOfWeek = date.getDayOfWeek();
        List<Interval> windows = new ArrayList<>();
        for (ResourceAvailability window : resource.getAvailability()) {
            if (window.getDayOfWeek() == dayOfWeek) {
                windows.add(Interval.of(
                        date.atTime(window.getStartTime()),
                        date.atTime(window.getEndTime())));
            }
        }
        windows.sort(Comparator.comparing(Interval::start));
        return windows;
    }

    /**
     * Free gaps for this resource on this date: offered windows minus busy blocks.
     * {@code busy} holds this resource's existing occupied intervals (from its
     * ResourceAllocations); blocks outside the windows are ignored automatically.
     */
    public static List<Interval> freeIntervals(Resource resource, LocalDate date, List<Interval> busy) {
        return Intervals.subtract(windowsFor(resource, date), busy);
    }

    private static AvailabilityException exceptionFor(Resource resource, LocalDate date) {
        for (AvailabilityException ex : resource.getAvailabilityExceptions()) {
            if (ex.getDate().equals(date)) {
                return ex;
            }
        }
        return null;
    }
}
