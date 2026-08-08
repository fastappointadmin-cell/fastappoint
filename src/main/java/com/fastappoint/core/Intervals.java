package com.fastappoint.core;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * Pure interval set-math over {@link Interval}. No domain dependencies, so it is
 * trivially testable and reusable: the availability calculator uses subtract(),
 * and the multi-resource solver will reuse merge()/intersection later.
 */
public final class Intervals {

    private Intervals() {}

    /** Collapse overlapping or touching intervals into a sorted, disjoint set. */
    public static List<Interval> merge(List<Interval> intervals) {
        if (intervals.isEmpty()) return List.of();
        List<Interval> sorted = new ArrayList<>(intervals);
        sorted.sort(Comparator.comparing(Interval::start));

        List<Interval> merged = new ArrayList<>();
        Interval current = sorted.getFirst();
        for (int i = 1; i < sorted.size(); i++) {
            Interval next = sorted.get(i);
            if (!next.start().isAfter(current.end())) { // overlap or touch -> extend
                LocalDateTime end = current.end().isAfter(next.end()) ? current.end() : next.end();
                current = Interval.of(current.start(), end);
            } else {
                merged.add(current);
                current = next;
            }
        }
        merged.add(current);
        return merged;
    }

    /**
     * Every window with all busy blocks removed, as sorted free gaps.
     * Busy blocks may overlap each other and may spill outside the windows;
     * both are handled.
     */
    public static List<Interval> subtract(List<Interval> windows, List<Interval> busy) {
        List<Interval> mergedBusy = merge(busy);
        List<Interval> free = new ArrayList<>();
        for (Interval window : windows) {
            free.addAll(subtractFromWindow(window, mergedBusy));
        }
        free.sort(Comparator.comparing(Interval::start));
        return free;
    }

    private static List<Interval> subtractFromWindow(Interval window, List<Interval> mergedBusy) {
        List<Interval> gaps = new ArrayList<>();
        LocalDateTime cursor = window.start();
        for (Interval b : mergedBusy) {
            if (!b.overlaps(window)) continue;
            LocalDateTime busyStart = Interval.latest(b.start(), window.start());
            LocalDateTime busyEnd = Interval.earliest(b.end(), window.end());
            if (cursor.isBefore(busyStart)) {
                gaps.add(Interval.of(cursor, busyStart));
            }
            if (busyEnd.isAfter(cursor)) {
                cursor = busyEnd;
            }
        }
        if (cursor.isBefore(window.end())) {
            gaps.add(Interval.of(cursor, window.end()));
        }
        return gaps;
    }

    /**
     * True when {@code needed} fits entirely inside one of the free intervals.
     * Because free intervals are disjoint and a booking is contiguous, a booking
     * that fits at all must fit within a single free interval.
     */
    public static boolean covers(List<Interval> free, Interval needed) {
        for (Interval f : free) {
            if (!f.start().isAfter(needed.start()) && !f.end().isBefore(needed.end())) {
                return true;
            }
        }
        return false;
    }
}
