package com.example.ringbufferbackpressure.core;

import java.util.List;

import com.example.ringbufferbackpressure.buffer.BoundedRingBuffer;
import com.example.ringbufferbackpressure.model.Event;

/**
 * Wraps the ring buffer and translates admission outcomes into observable metrics.
 */
public final class RingBufferIngestionService {
    private final BoundedRingBuffer<Event> buffer;
    private final IngestionMetrics metrics = new IngestionMetrics();

    public RingBufferIngestionService(int capacity) {
        this.buffer = new BoundedRingBuffer<>(capacity);
    }

    public PublishResult publish(Event event, BackpressurePolicy policy) throws InterruptedException {
        metrics.incrementAttempted();
        boolean hadToWait = policy == BackpressurePolicy.BLOCK && buffer.size() == buffer.capacity();
        boolean published = buffer.publish(event, policy);
        if (!published) {
            metrics.incrementDropped();
            return PublishResult.DROPPED;
        }

        if (hadToWait) {
            metrics.incrementBlocked();
        }
        metrics.incrementPublished();
        metrics.recordBufferSize(buffer.size());
        return hadToWait ? PublishResult.BLOCKED_THEN_PUBLISHED : PublishResult.PUBLISHED;
    }

    public int drainTo(List<Event> target, int maxItems) {
        int drained = buffer.drainTo(target, maxItems);
        metrics.addConsumed(drained);
        return drained;
    }

    public int size() {
        return buffer.size();
    }

    public int capacity() {
        return buffer.capacity();
    }

    public IngestionMetrics getMetrics() {
        return metrics;
    }
}
