package com.fastappoint.availability;

import com.fastappoint.core.Interval;
import com.fastappoint.core.Intervals;
import com.fastappoint.domain.Resource;
import com.fastappoint.domain.ResourceAvailability;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * Free/busy math for a SINGLE resource on a SINGLE day. Pure and stateless.
 *
 * Availability is now explicitly date-specific: only windows whose date matches
 * the requested date apply. No recurring pattern, no override layer.
 */
public final class AvailabilityCalculator {

    private AvailabilityCalculator() {}

    /**
     * The offered windows for this resource on this date, before bookings.
     * Returns all {@link ResourceAvailability} entries whose date equals {@code date}.
     */
    public static List<Interval> windowsFor(Resource resource, LocalDate date) {
        List<Interval> windows = new ArrayList<>();
        for (ResourceAvailability window : resource.getAvailability()) {
            if (window.getDate().equals(date)) {
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
     */
    public static List<Interval> freeIntervals(Resource resource, LocalDate date, List<Interval> busy) {
        return Intervals.subtract(windowsFor(resource, date), busy);
    }
}
