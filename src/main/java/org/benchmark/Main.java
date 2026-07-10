package org.benchmark;

import lombok.extern.slf4j.Slf4j;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.concurrent.*;

@Slf4j
public class Main {
    private static final int THREAD_POOL_SIZE = 6;

    public static void main(String[] args) throws InterruptedException {
        LocalDateTime timeAtStart = LocalDateTime.now();
        BlockingQueue<TlcTripData> dataQueue = new ArrayBlockingQueue<TlcTripData>(50000);
        TlcTripDataProducer tlcTripDataProducer = new TlcTripDataProducer(dataQueue);
        MetricsDashboard metricsDashboard = new MetricsDashboard();

        try (ExecutorService consumerPool = Executors.newFixedThreadPool(THREAD_POOL_SIZE)) {
            for(int i = 0; i < THREAD_POOL_SIZE; i++) {
                consumerPool.execute(new TlcTripDataConsumer(dataQueue, metricsDashboard));
            }

            tlcTripDataProducer.runProducer(dataQueue);
        }

        LocalDateTime timeAtEnd = LocalDateTime.now();
        log.info(metricsDashboard.toString());
        log.info("Time at start: {}", timeAtStart);
        log.info("Time at end: {}", timeAtEnd);
        log.info("Total time taken: {} seconds.", Duration.between(timeAtStart, timeAtEnd).toSeconds());
    }
}
