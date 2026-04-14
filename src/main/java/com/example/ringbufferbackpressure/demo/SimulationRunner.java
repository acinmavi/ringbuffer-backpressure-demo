package com.example.ringbufferbackpressure.demo;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;

import com.example.ringbufferbackpressure.core.RingBufferIngestionService;
import com.example.ringbufferbackpressure.model.Event;

/**
 * Runs a small concurrent workload to make full-buffer behavior visible.
 */
public final class SimulationRunner {

    public SimulationReport run(SimulationConfig config) throws InterruptedException {
        RingBufferIngestionService service = new RingBufferIngestionService(config.getBufferCapacity());
        CountDownLatch producersDone = new CountDownLatch(config.getProducerCount());
        AtomicBoolean consumerRunning = new AtomicBoolean(true);
        AtomicLong sequence = new AtomicLong();

        Thread consumer = new Thread(() -> consumeLoop(config, service, producersDone, consumerRunning), "consumer");
        long start = System.nanoTime();
        consumer.start();

        List<Thread> producers = new ArrayList<>();
        for (int producerIndex = 0; producerIndex < config.getProducerCount(); producerIndex++) {
            final String producerName = "producer-" + producerIndex;
            Thread producer = new Thread(() -> produceLoop(config, service, producersDone, sequence, producerName), producerName);
            producers.add(producer);
            producer.start();
        }

        for (Thread producer : producers) {
            producer.join();
        }
        producersDone.await();
        consumerRunning.set(false);
        consumer.join();

        long elapsedMillis = (System.nanoTime() - start) / 1_000_000L;
        boolean reachedCapacity = service.getMetrics().getMaxObservedBufferSize() >= config.getBufferCapacity();
        return new SimulationReport(config.getName(), service.getMetrics(), reachedCapacity, elapsedMillis);
    }

    private void produceLoop(SimulationConfig config,
                             RingBufferIngestionService service,
                             CountDownLatch producersDone,
                             AtomicLong sequence,
                             String producerName) {
        try {
            for (int i = 0; i < config.getEventsPerProducer(); i++) {
                long id = sequence.incrementAndGet();
                Event event = new Event(id, producerName, System.nanoTime(), "payload-" + id);
                service.publish(event, config.getPolicy());
                pause(config.getProducerPauseMillis());
            }
        } catch (InterruptedException ex) {
            Thread.currentThread().interrupt();
        } finally {
            producersDone.countDown();
        }
    }

    private void consumeLoop(SimulationConfig config,
                             RingBufferIngestionService service,
                             CountDownLatch producersDone,
                             AtomicBoolean consumerRunning) {
        try {
            while (consumerRunning.get() || producersDone.getCount() > 0 || service.size() > 0) {
                List<Event> batch = new ArrayList<>(config.getConsumerBatchSize());
                int drained = service.drainTo(batch, config.getConsumerBatchSize());
                if (drained == 0) {
                    Thread.sleep(1L);
                    continue;
                }
                pause(config.getConsumerPauseMillis());
            }
        } catch (InterruptedException ex) {
            Thread.currentThread().interrupt();
        }
    }

    private void pause(long millis) throws InterruptedException {
        if (millis > 0L) {
            Thread.sleep(millis);
        }
    }
}
