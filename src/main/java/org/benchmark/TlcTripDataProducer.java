package org.benchmark;

import lombok.extern.slf4j.Slf4j;
import java.io.*;
import java.util.concurrent.BlockingQueue;

/**
 * Responsible for single-threaded file parsing and work distribution.
 * <p>
 * This class streams rows sequentially from a local disk space to minimize memory overhead.
 * The streamed rows are systematically pushed to a bounded queue.
 * When execution finishes, it broadcasts terminal elements to safely clear worker allocations.
 * </p>
 */
@Slf4j
public class TlcTripDataProducer {

    /** The target queue bounded buffer shared with independent consumer threads. */
    private final BlockingQueue<String> dataQueue;

    private final Config config;

    public TlcTripDataProducer(BlockingQueue<String> dataQueue, Config config) {
        this.dataQueue = dataQueue;
        this.config = config;
    }

    /**
     * Executes the main ingestion stream loop, reading records out of a flat CSV file
     * and managing queue boundary allocations.
     * * @throws InterruptedException If the thread is interrupted while waiting to place elements into a filled queue.
     */
    public void run() throws InterruptedException {
        log.info("Producer running.");
        try (BufferedReader bufferedReader = new BufferedReader(
            new FileReader(config.getTlcDatasetFilePath()), 16384)) {
            // Skip the first line
            String header = bufferedReader.readLine();

            String line;
            while ((line = bufferedReader.readLine()) != null) {
                dataQueue.put(line);
            }

            sendPoisonPills();
            log.info("Producer has published all the rows.");
        } catch (IOException e) {
            log.error(e.getMessage());
        }
    }

    /**
     * Appends designated dummy termination entries to the end of the data stream queue.
     * The number of generated items matches the active thread count to prevent engine lock-up.
     * * @throws InterruptedException If interrupted while putting terminal entries into the queue.
     */
    private void sendPoisonPills() throws InterruptedException {
        log.info("Ingestion completed. Broadcasting {} poison pill packets to consumer pool.", config.getWorkerCount());
        for (int i = 0; i < config.getWorkerCount(); i++) {
            dataQueue.put(config.getPoisonPill());
        }
    }
}
