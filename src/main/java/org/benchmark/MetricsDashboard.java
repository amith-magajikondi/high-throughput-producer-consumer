package org.benchmark;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import java.util.concurrent.atomic.LongAdder;

@NoArgsConstructor
@Getter
@Setter
@ToString
public class MetricsDashboard {
    private LongAdder totalRows = new LongAdder();
    private LongAdder totalAmount = new LongAdder();
    private LongAdder totalTripsCount = new LongAdder();
    private LongAdder totalTripDistance = new LongAdder();
    private LongAdder totalPassengerCount = new LongAdder();
}
