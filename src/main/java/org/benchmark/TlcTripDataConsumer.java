package org.benchmark;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;

import java.util.concurrent.BlockingQueue;

@RequiredArgsConstructor
@Slf4j
public class TlcTripDataConsumer implements Runnable {
    private final BlockingQueue<TlcTripData> dataQueue;
    private final MetricsDashboard metricsDashboard;

    @Override
    public void run() {
        log.info("{} consumer thread running.", Thread.currentThread().getName());
        while(true) {
            TlcTripData tlcTripData = null;
            try {
                tlcTripData = dataQueue.take();
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
            if(tlcTripData.getDummyRow() == 1) {
                log.info("{} has consumed the poison pill. Thread is terminating now.", Thread.currentThread().getName());
                break;
            }

            metricsDashboard.getTotalRows().add(1);
            metricsDashboard.getTotalTripsCount().add(1);
            metricsDashboard.getTotalAmount().add((long) tlcTripData.getTotalAmount());
            metricsDashboard.getTotalPassengerCount().add(tlcTripData.getPassengerCount());
            metricsDashboard.getTotalTripDistance().add((long) tlcTripData.getTripDistance());
        }
    }
}
