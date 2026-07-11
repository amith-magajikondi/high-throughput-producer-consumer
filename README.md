# high-throughput-producer-consumer

An enterprise-grade performance blueprint for high-volume batch processing, streaming simulation, and non-blocking concurrency evaluation using Java 21.

---

## 🚀 Purpose

This project is a high-performance benchmarking sandbox designed to demonstrate the **Producer-Consumer pattern**, **bounded memory management (Backpressure)**, and **lock-free thread synchronization**.

By ingestion and processing of millions of rows from the New York City Taxi & Limousine Commission (TLC) dataset, this application provides an isolated playground to study hardware utilization, thread pools, and data pipeline bottlenecks on a single multicore machine.

---

## Performance Benchmarks

* **Hardware:** AMD Ryzen 5 4600H (6 Cores / 12 Threads), 8GB RAM
* **Dataset Size:** 12.7 Million Rows (~1.8 GB Uncompressed text format)
* **Initial Benchmark:** **45 seconds** 
* **Optimized Benchmark:** **< 20 seconds** 
* **Processing Throughput:** ~635,000+ records per second

### The Optimization Strategy
In the initial iteration, the single Producer thread handled both file reading and heavy Jackson CSV object inflation sequentially before placing items onto the queue. This starved the consumer pool, making the text parser the primary bottleneck.

By refactoring the pipeline to drop raw, un-parsed `String` rows directly into the `BlockingQueue`, the Producer thread was freed up to focus entirely on sequential disk read capacity. The heavy computation string tokenization, datetime parsing, data cleaning, and metric transformations was completely shifted downstream to the parallel worker array. This structural shift cut total processing latency by over **55%** and maxed out multicore CPU efficiency.

---

## Architecture & Core Design Patterns

* **Decoupled Work Allocator:** The system leverages a classic Producer-Consumer architecture using a bounded `ArrayBlockingQueue` to throttle ingestion rate, implement mandatory system backpressure, and keep the application's runtime memory allocation flat.
* **Lock-Free Cellular Stripping:** Relies on JDK `LongAdder` constructs to increment telemetry fields. By avoiding global synchronized blocks or standard `AtomicLong` CAS loops, it completely eliminates cache-line contention among concurrent worker threads.
* **Deterministic Financial Exactness:** Bypasses non-associative floating-point rounding errors native to primitive `double` additions across arbitrary thread pools by converting currency indices and distances into fixed-point long spaces (cents) during downstream consumption.
* **Centralized Configuration Singleton:** Managed via a root-level `application.properties` configuration scheme to externalize local data environment paths cleanly without code modification or leaking environmental file states into version control.
* **Poison Pill Thread Lifecycle:** Utilizes a custom, configured terminal string packet injected into the stream buffer to notify the dynamic parallel worker pool to abort execution loops gracefully.

---

## Project Setup

### Prerequisites
* **Java Development Kit (JDK):** Version 21 or higher
* **Build Tool:** Apache Maven 3.9+
* **Data File:** Download the `yellow_tripdata_2015-01.csv` from [NYC Yellow Taxi Trip Data](https://www.kaggle.com/datasets/elemento/nyc-yellow-taxi-trip-data) and save it to your local workspace path.

## Local Configuration & Run Guide

### 1. Environment Configuration
The application externalizes all environment and tuning parameters. Create an `application.properties` file in your root workspace directory (at the same level as `pom.xml`):

```properties
# Path for the TCL Trip Dataset.
tlc.dataset.filepath=

# Total number of parallel worker threads allocated to consume and process data rows.
worker.count=

# The maximum bounds of the internal queue used to buffer inflated objects before processing.
queue.capacity=

# Poison pill to shut down the consumers.
poison.pill.string=