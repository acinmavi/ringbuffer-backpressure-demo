package com.example.ringbufferbackpressure.core;

import java.util.concurrent.atomic.AtomicLong;

/**
 * Collects lightweight counters for the ingestion flow and saturation events.
 */
public final class IngestionMetrics {
    private final AtomicLong attemptedCount = new AtomicLong();
    private final AtomicLong publishedCount = new AtomicLong();
    private final AtomicLong droppedCount = new AtomicLong();
    private final AtomicLong blockedCount = new AtomicLong();
    private final AtomicLong consumedCount = new AtomicLong();
    private final AtomicLong maxObservedBufferSize = new AtomicLong();

    public void incrementAttempted() {
        attemptedCount.incrementAndGet();
    }

    public void incrementPublished() {
        publishedCount.incrementAndGet();
    }

    public void incrementDropped() {
        droppedCount.incrementAndGet();
    }

    public void incrementBlocked() {
        blockedCount.incrementAndGet();
    }

    public void addConsumed(long count) {
        consumedCount.addAndGet(count);
    }

    public void recordBufferSize(long currentSize) {
        long observed = maxObservedBufferSize.get();
        while (currentSize > observed && !maxObservedBufferSize.compareAndSet(observed, currentSize)) {
            observed = maxObservedBufferSize.get();
        }
    }

    public long getAttemptedCount() {
        return attemptedCount.get();
    }

    public long getPublishedCount() {
        return publishedCount.get();
    }

    public long getDroppedCount() {
        return droppedCount.get();
    }

    public long getBlockedCount() {
        return blockedCount.get();
    }

    public long getConsumedCount() {
        return consumedCount.get();
    }

    public long getMaxObservedBufferSize() {
        return maxObservedBufferSize.get();
    }
}
