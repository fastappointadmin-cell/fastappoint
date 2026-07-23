package com.fastappoint.core;

/**
 * How the quantity of a single {@code ServiceRequirement} line is satisfied.
 * This is a property of the requirement line, NOT of the whole service:
 * one service can mix modes (e.g. a dinner needs SINGLE waiter + MERGE tables).
 */
public enum AllocationMode {

    /** Exactly one resource that matches the predicate. (barber, waiter, ramp) */
    SINGLE,

    /** A fixed number N of distinct resources that each match the predicate. */
    MULTIPLE,

    /**
     * As many matching resources as needed until the sum of their {@code capacity}
     * reaches the demand. (tables merged until seats >= partySize)
     */
    MERGE
}
