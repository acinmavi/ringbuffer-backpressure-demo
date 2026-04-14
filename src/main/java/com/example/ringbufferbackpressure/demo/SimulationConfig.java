package com.example.ringbufferbackpressure.demo;

import com.example.ringbufferbackpressure.core.BackpressurePolicy;

/**
 * Captures the knobs used to make the demos saturate predictably.
 */
public final class SimulationConfig {
    private final String name;
    private final int producerCount;
    private final int eventsPerProducer;
    private final int bufferCapacity;
    private final int consumerBatchSize;
    private final long producerPauseMillis;
    private final long consumerPauseMillis;
    private final BackpressurePolicy policy;

    public SimulationConfig(String name,
                            int producerCount,
                            int eventsPerProducer,
                            int bufferCapacity,
                            int consumerBatchSize,
                            long producerPauseMillis,
                            long consumerPauseMillis,
                            BackpressurePolicy policy) {
        this.name = name;
        this.producerCount = producerCount;
        this.eventsPerProducer = eventsPerProducer;
        this.bufferCapacity = bufferCapacity;
        this.consumerBatchSize = consumerBatchSize;
        this.producerPauseMillis = producerPauseMillis;
        this.consumerPauseMillis = consumerPauseMillis;
        this.policy = policy;
    }

    public static SimulationConfig dropDemo() {
        return new SimulationConfig("drop-demo", 3, 400, 32, 8, 0L, 10L, BackpressurePolicy.DROP);
    }

    public static SimulationConfig blockDemo() {
        return new SimulationConfig("block-demo", 3, 240, 16, 4, 0L, 12L, BackpressurePolicy.BLOCK);
    }

    public String getName() {
        return name;
    }

    public int getProducerCount() {
        return producerCount;
    }

    public int getEventsPerProducer() {
        return eventsPerProducer;
    }

    public int getBufferCapacity() {
        return bufferCapacity;
    }

    public int getConsumerBatchSize() {
        return consumerBatchSize;
    }

    public long getProducerPauseMillis() {
        return producerPauseMillis;
    }

    public long getConsumerPauseMillis() {
        return consumerPauseMillis;
    }

    public BackpressurePolicy getPolicy() {
        return policy;
    }
}
