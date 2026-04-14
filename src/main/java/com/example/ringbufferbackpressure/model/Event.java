package com.example.ringbufferbackpressure.model;

/**
 * Immutable event object used by producers and the consumer in the demo.
 */
public final class Event {
    private final long sequence;
    private final String producerName;
    private final long createdAtNanos;
    private final String payload;

    public Event(long sequence, String producerName, long createdAtNanos, String payload) {
        this.sequence = sequence;
        this.producerName = producerName;
        this.createdAtNanos = createdAtNanos;
        this.payload = payload;
    }

    public long getSequence() {
        return sequence;
    }

    public String getProducerName() {
        return producerName;
    }

    public long getCreatedAtNanos() {
        return createdAtNanos;
    }

    public String getPayload() {
        return payload;
    }
}
