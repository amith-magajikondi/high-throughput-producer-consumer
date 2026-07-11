package org.benchmark;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;
import lombok.extern.slf4j.Slf4j;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.concurrent.*;

/**
 * The execution entry point for the high-throughput processing pipeline.
 * <p>
 * This class orchestrates the lifecycle of a high-performance multithreaded system designed
 * to ingest, parse, and aggregate metrics from millions of data rows using the Producer-Consumer pattern.
 * </p>
 * * @author Amith Magajikondi
 */
@Slf4j
public class Main {

    private static Config loadConfig() {
        Properties properties = new Properties();
        try (FileInputStream fileInputStream = new FileInputStream("application.properties")) {
            properties.load(fileInputStream);
            String filePath = properties.getProperty("tlc.dataset.filepath");
            if (filePath == null || filePath.isBlank()) {
                throw new IllegalStateException(
                    "Property 'tlc.dataset.filepath' is missing or empty in config.properties");
            }

            int workerCount = Integer.parseInt(properties.getProperty("worker.count"));
            if (workerCount == 0) {
                throw new IllegalStateException(
                    "Property 'worker.count' cannot be zero."
                );
            }

            int queueCapacity = Integer.parseInt(properties.getProperty("queue.capacity"));
            if(queueCapacity <= 1000 ) {
                throw new IllegalStateException(
                    "Queue capacity must be above 1000 for efficient throughput."
                );
            }

            String poisonPill = properties.getProperty("poison.pill.string");
            if (poisonPill == null || poisonPill.isBlank()) {
                throw new IllegalStateException(
                    "Property 'poison.pill.string' is missing or empty in config.properties"
                );
            }

            Config.initialiseConfig(filePath, workerCount, queueCapacity, poisonPill);
            return Config.getInstance();
        } catch (IOException e) {
            log.error(
                "Failed to load config.properties. Ensure the file exists in the project root.");
            throw new RuntimeException("Missing configuration file", e);
        }
    }

    /**
     * Application entry point. Sets up the execution environment, runs the ingestion
     * engine, and measures system throughput.
     *
     * @param args Command-line arguments (unused).
     * @throws InterruptedException If the main thread execution is interrupted during pipeline execution.
     */
    public static void main(String[] args) throws InterruptedException {
        LocalDateTime timeAtStart = LocalDateTime.now();
        printProjectPurpose();
        // Initialize and load config
        Config config = loadConfig();
        // Bounded queue ensures strict control over memory footprints under heavy backpressure
        BlockingQueue<String> dataQueue = new ArrayBlockingQueue<>(config.getQueueCapacity());
        TlcTripDataProducer tlcTripDataProducer = new TlcTripDataProducer(dataQueue, config);
        MetricsDashboard metricsDashboard = new MetricsDashboard();

        // Manage lifecycle of the consumer thread pool gracefully using Try-With-Resources
        try (ExecutorService consumerPool = Executors.newFixedThreadPool(config.getWorkerCount())) {
            for (int i = 0; i < config.getWorkerCount(); i++) {
                consumerPool.execute(new TlcTripDataConsumer(dataQueue, metricsDashboard, config));
            }

            // Begin single-threaded file stream ingestion
            tlcTripDataProducer.run();
        }

        LocalDateTime timeAtEnd = LocalDateTime.now();
        long totalTimeTaken = Duration.between(timeAtStart, timeAtEnd).toSeconds();
        log.info(metricsDashboard.toString());
        log.info("Total time taken: {} seconds.", totalTimeTaken);
        log.info("Throughput: {} records/sec.", metricsDashboard.getTotalRows().sum() / totalTimeTaken);
    }

    /**
     * Prints a standardized statement explaining the core purpose and architectural layout
     * of this concurrent project.
     */
    private static void printProjectPurpose() {
        log.info("""
            
            ========================================================================================
                          NEW YORK CITY TLC HIGH-THROUGHPUT CONCURRENT ANALYTICS ENGINE
            ========================================================================================
            PURPOSE:
            This system serves as an enterprise-grade performance blueprint for high-volume
            batch processing, low-latency streaming simulation, and non-blocking concurrency evaluation.
            
            ARCHITECTURAL HIGHLIGHTS:
              • Bounded Backpressure: Uses an ArrayBlockingQueue to cap data in-flight, preventing Heap OOM.
              • Decoupled Pipelines: Utilizes a single high-efficiency Producer thread parsing I/O chunks
                independently of multiple concurrent downstream Consumer workers.
              • Lock-Free Metrics: Exploits cell-stripping LongAdders to optimize throughput and completely
                eliminate thread contention overheads under extreme update frequencies.
            ========================================================================================
            """);
    }
}
