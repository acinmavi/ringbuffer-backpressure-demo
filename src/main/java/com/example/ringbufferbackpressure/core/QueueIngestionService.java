package com.example.ringbufferbackpressure.core;

import java.util.List;
import java.util.concurrent.ArrayBlockingQueue;

import com.example.ringbufferbackpressure.model.Event;

/**
 * Provides a queue-based baseline so the benchmark can compare admission behavior.
 */
public final class QueueIngestionService {
    private final ArrayBlockingQueue<Event> queue;
    private final IngestionMetrics metrics = new IngestionMetrics();

    public QueueIngestionService(int capacity) {
        this.queue = new ArrayBlockingQueue<>(capacity);
    }

    public PublishResult publish(Event event, BackpressurePolicy policy) throws InterruptedException {
        metrics.incrementAttempted();
        boolean hadToWait = policy == BackpressurePolicy.BLOCK && queue.remainingCapacity() == 0;
        boolean published;
        if (policy == BackpressurePolicy.BLOCK) {
            queue.put(event);
            published = true;
        } else {
            published = queue.offer(event);
        }

        if (!published) {
            metrics.incrementDropped();
            return PublishResult.DROPPED;
        }

        if (hadToWait) {
            metrics.incrementBlocked();
        }
        metrics.incrementPublished();
        metrics.recordBufferSize(queue.size());
        return hadToWait ? PublishResult.BLOCKED_THEN_PUBLISHED : PublishResult.PUBLISHED;
    }

    public int drainTo(List<Event> target, int maxItems) {
        int drained = queue.drainTo(target, maxItems);
        metrics.addConsumed(drained);
        return drained;
    }

    public int size() {
        return queue.size();
    }

    public int capacity() {
        return queue.size() + queue.remainingCapacity();
    }

    public IngestionMetrics getMetrics() {
        return metrics;
    }
}
