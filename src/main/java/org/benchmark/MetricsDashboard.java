package org.benchmark;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import java.util.concurrent.atomic.LongAdder;

/**
 * An in-memory, thread-safe dashboard aggregating macro-level analytics from processed rows.
 * <p>
 * This class relies entirely on {@link LongAdder} instead of standard {@code AtomicLong} or
 * synchronizations. By utilizing internally stripped cells allocated per thread, it minimizes
 * cache-line bouncing and maximizes throughput under intense thread contention.
 * </p>
 */
@NoArgsConstructor
@Getter
@Setter
@ToString
public class MetricsDashboard {

    /** The total count of all processed data records. */
    private LongAdder totalRows = new LongAdder();

    /** The cumulative financial total across all records, scaled to cents to bypass double rounding errors. */
    private LongAdder totalAmountInCents = new LongAdder();

    /** The cumulative count of trips identified and parsed. */
    private LongAdder totalTripsCount = new LongAdder();

    /** The cumulative distance covered, scaled up to preserve precision under concurrency. */
    private LongAdder totalTripDistance = new LongAdder();

    /** The cumulative passenger count recorded across all valid entries. */
    private LongAdder totalPassengerCount = new LongAdder();
}
