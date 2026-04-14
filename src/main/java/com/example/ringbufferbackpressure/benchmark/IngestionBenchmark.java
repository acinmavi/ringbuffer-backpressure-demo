package com.example.ringbufferbackpressure.benchmark;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;

import com.example.ringbufferbackpressure.core.BackpressurePolicy;
import com.example.ringbufferbackpressure.core.IngestionMetrics;
import com.example.ringbufferbackpressure.core.QueueIngestionService;
import com.example.ringbufferbackpressure.core.RingBufferIngestionService;
import com.example.ringbufferbackpressure.model.Event;

/**
 * Runs a lightweight workload against two bounded ingestion implementations.
 */
public final class IngestionBenchmark {
    private static final int PRODUCER_COUNT = 2;
    private static final int EVENTS_PER_PRODUCER = 2_000;
    private static final int CAPACITY = 256;
    private static final int CONSUMER_BATCH_SIZE = 32;

    public BenchmarkSnapshot runRingBufferScenario() throws InterruptedException {
        RingBufferIngestionService service = new RingBufferIngestionService(CAPACITY);
        return runScenario("ring-buffer", service);
    }

    public BenchmarkSnapshot runQueueScenario() throws InterruptedException {
        QueueIngestionService service = new QueueIngestionService(CAPACITY);
        return runScenario("array-blocking-queue", service);
    }

    private BenchmarkSnapshot runScenario(String scenarioName, Object service) throws InterruptedException {
        CountDownLatch producersDone = new CountDownLatch(PRODUCER_COUNT);
        AtomicBoolean consumerRunning = new AtomicBoolean(true);
        AtomicLong sequence = new AtomicLong();
        Thread consumer = new Thread(() -> consumeLoop(service, producersDone, consumerRunning), scenarioName + "-consumer");

        long start = System.nanoTime();
        consumer.start();

        List<Thread> producers = new ArrayList<>();
        for (int producerIndex = 0; producerIndex < PRODUCER_COUNT; producerIndex++) {
            Thread producer = new Thread(() -> produceLoop(service, producersDone, sequence), scenarioName + "-producer-" + producerIndex);
            producers.add(producer);
            producer.start();
        }

        for (Thread producer : producers) {
            producer.join();
        }
        producersDone.await();
        consumerRunning.set(false);
        consumer.join();

        IngestionMetrics metrics = metricsOf(service);
        long elapsedMillis = (System.nanoTime() - start) / 1_000_000L;
        return new BenchmarkSnapshot(
            scenarioName,
            metrics.getPublishedCount(),
            metrics.getConsumedCount(),
            metrics.getDroppedCount(),
            metrics.getBlockedCount(),
            elapsedMillis);
    }

    private void produceLoop(Object service, CountDownLatch producersDone, AtomicLong sequence) {
        try {
            for (int i = 0; i < EVENTS_PER_PRODUCER; i++) {
                long id = sequence.incrementAndGet();
                Event event = new Event(id, Thread.currentThread().getName(), System.nanoTime(), "payload-" + id);
                if (service instanceof RingBufferIngestionService) {
                    ((RingBufferIngestionService) service).publish(event, BackpressurePolicy.BLOCK);
                } else {
                    ((QueueIngestionService) service).publish(event, BackpressurePolicy.BLOCK);
                }
            }
        } catch (InterruptedException ex) {
            Thread.currentThread().interrupt();
        } finally {
            producersDone.countDown();
        }
    }

    private void consumeLoop(Object service, CountDownLatch producersDone, AtomicBoolean consumerRunning) {
        try {
            while (consumerRunning.get() || producersDone.getCount() > 0 || sizeOf(service) > 0) {
                List<Event> batch = new ArrayList<>(CONSUMER_BATCH_SIZE);
                int drained;
                if (service instanceof RingBufferIngestionService) {
                    drained = ((RingBufferIngestionService) service).drainTo(batch, CONSUMER_BATCH_SIZE);
                } else {
                    drained = ((QueueIngestionService) service).drainTo(batch, CONSUMER_BATCH_SIZE);
                }
                if (drained == 0) {
                    Thread.sleep(1L);
                }
            }
        } catch (InterruptedException ex) {
            Thread.currentThread().interrupt();
        }
    }

    private int sizeOf(Object service) {
        if (service instanceof RingBufferIngestionService) {
            return ((RingBufferIngestionService) service).size();
        }
        return ((QueueIngestionService) service).size();
    }

    private IngestionMetrics metricsOf(Object service) {
        if (service instanceof RingBufferIngestionService) {
            return ((RingBufferIngestionService) service).getMetrics();
        }
        return ((QueueIngestionService) service).getMetrics();
    }
}
