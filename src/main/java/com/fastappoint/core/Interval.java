package com.fastappoint.core;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

/**
 * A half-open time interval [start, end): start is included, end is excluded.
 * Immutable value object. Touching endpoints (a.end == b.start) do NOT overlap,
 * which is exactly what you want for scheduling: a 10:00-11:00 booking and an
 * 11:00-12:00 booking sit back to back without conflict.
 */
public final class Interval {

    private final LocalDateTime start;
    private final LocalDateTime end;

    private Interval(LocalDateTime start, LocalDateTime end) {
        if (!start.isBefore(end)) {
            throw new IllegalArgumentException("start must be before end: " + start + " .. " + end);
        }
        this.start = start;
        this.end = end;
    }

    public static Interval of(LocalDateTime start, LocalDateTime end) {
        return new Interval(start, end);
    }

    public LocalDateTime start() { return start; }
    public LocalDateTime end() { return end; }
    public Duration length() { return Duration.between(start, end); }

    public boolean overlaps(Interval other) {
        return start.isBefore(other.end) && other.start.isBefore(end);
    }

    public boolean contains(LocalDateTime instant) {
        return !instant.isBefore(start) && instant.isBefore(end);
    }

    /** The shared part of two intervals, if they overlap (touching does not count). */
    public Optional<Interval> intersection(Interval other) {
        LocalDateTime s = latest(start, other.start);
        LocalDateTime e = earliest(end, other.end);
        return s.isBefore(e) ? Optional.of(new Interval(s, e)) : Optional.empty();
    }

    /** This interval minus a busy block: yields 0, 1, or 2 remaining pieces. */
    public List<Interval> minus(Interval busy) {
        if (!overlaps(busy)) return List.of(this);
        List<Interval> remaining = new ArrayList<>(2);
        if (start.isBefore(busy.start)) remaining.add(new Interval(start, busy.start));
        if (busy.end.isBefore(end)) remaining.add(new Interval(busy.end, end));
        return remaining; // empty when busy fully covers this interval
    }

    static LocalDateTime latest(LocalDateTime a, LocalDateTime b) { return a.isAfter(b) ? a : b; }
    static LocalDateTime earliest(LocalDateTime a, LocalDateTime b) { return a.isBefore(b) ? a : b; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Interval other)) return false;
        return start.equals(other.start) && end.equals(other.end);
    }

    @Override
    public int hashCode() { return Objects.hash(start, end); }

    @Override
    public String toString() { return "[" + start + " .. " + end + ")"; }
}
