package org.benchmark;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import tools.jackson.databind.DeserializationFeature;
import tools.jackson.dataformat.csv.CsvMapper;
import tools.jackson.dataformat.csv.CsvSchema;
import tools.jackson.datatype.jsr310.JavaTimeModule;

import java.util.concurrent.BlockingQueue;

/**
 * Concurrent worker instance implementing {@link Runnable} to continuously consume and process records.
 * <p>
 * Instances pull tasks out of the shared queue context, transform raw data strings to Java objects,
 * evaluate terminal metrics conditions, apply conversions to preserve decimal exactness, and
 * record analytical parameters into the dashboard metrics.
 * </p>
 */
@RequiredArgsConstructor
@Slf4j
public class TlcTripDataConsumer implements Runnable {

    private final BlockingQueue<String> dataQueue;
    private final MetricsDashboard metricsDashboard;
    private final Config config;

    private final CsvMapper MAPPER = createMapper();
    private final CsvSchema SCHEMA = MAPPER.schemaFor(TlcTripData.class);

    private static CsvMapper createMapper() {
        return CsvMapper.builder()
            .addModule(new JavaTimeModule())
            .disable(DeserializationFeature.FAIL_ON_NULL_FOR_PRIMITIVES)
            .build();
    }
    /**
     * Executes the polling consumption cycle. Iterates continuously until intercepting
     * an operational poison pill item.
     */
    @Override
    public void run() {
        log.info("{} consumer thread running.", Thread.currentThread().getName());

        String poisonPill = config.getPoisonPill();

        while (true) {
            try {
                String tlcTripDataString = dataQueue.take();

                // Poison Pill interception rule
                if (poisonPill.equals(tlcTripDataString)) {
                    log.info("{} has consumed the poison pill. Thread is terminating now.",
                        Thread.currentThread().getName());
                    break;
                }

                if (tlcTripDataString.isBlank()) {
                    continue;
                }

                TlcTripData tlcTripData = MAPPER.readerFor(TlcTripData.class)
                    .with(SCHEMA)
                    .readValue(tlcTripDataString);

                metricsDashboard.getTotalRows().add(1);
                metricsDashboard.getTotalTripsCount().add(1);
                metricsDashboard.getTotalPassengerCount().add(tlcTripData.getPassengerCount());

                // Scaled conversion fixes to completely eliminate floating point truncation bugs
                long amountInCents = Math.round(tlcTripData.getTotalAmount() * 100.0);
                metricsDashboard.getTotalAmountInCents().add(amountInCents);

                long distanceScaled = Math.round(tlcTripData.getTripDistance() * 100.0);
                metricsDashboard.getTotalTripDistance().add(distanceScaled);

            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }
    }
}
