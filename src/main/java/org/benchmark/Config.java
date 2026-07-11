package org.benchmark;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * A thread-safe, thread-confined Singleton configuration registry for the application session.
 * <p>
 * This class locks down runtime execution metrics (such as thread limits, file targets, and capacity bounds)
 * once initialized. It strictly prohibits re-initialization during the active JVM lifecycle to maintain state integrity
 * across the decoupled producer and consumer processing pools.
 * </p>
 */
@Getter
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public class Config {

    private static volatile Config instance;

    private final String tlcDatasetFilePath;
    private final int workerCount;
    private final int queueCapacity;
    private final String poisonPill;

    public static synchronized void initialiseConfig(String tlcDatasetFilePath, int workerCount, int queueCapacity,
        String poisonPill) {
        if (instance != null) {
            throw new IllegalStateException(
                "Config has already been initialised for the session. Please restart.");
        }

        instance = new Config(tlcDatasetFilePath, workerCount, queueCapacity, poisonPill);
    }

    public static Config getInstance() {
        if (instance == null) {
            throw new IllegalStateException(
                "Config has not been initialised. Please initialise the config first.");
        }

        return instance;
    }
}
