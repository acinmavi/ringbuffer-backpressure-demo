package com.example.ringbufferbackpressure.demo;

import com.example.ringbufferbackpressure.core.IngestionMetrics;

/**
 * Summarizes a simulation run for console output and tests.
 */
public final class SimulationReport {
    private final String scenarioName;
    private final IngestionMetrics metrics;
    private final boolean bufferReachedCapacity;
    private final long elapsedMillis;

    public SimulationReport(String scenarioName,
                            IngestionMetrics metrics,
                            boolean bufferReachedCapacity,
                            long elapsedMillis) {
        this.scenarioName = scenarioName;
        this.metrics = metrics;
        this.bufferReachedCapacity = bufferReachedCapacity;
        this.elapsedMillis = elapsedMillis;
    }

    public String getScenarioName() {
        return scenarioName;
    }

    public IngestionMetrics getMetrics() {
        return metrics;
    }

    public boolean isBufferReachedCapacity() {
        return bufferReachedCapacity;
    }

    public long getElapsedMillis() {
        return elapsedMillis;
    }

    @Override
    public String toString() {
        return "SimulationReport{"
            + "scenarioName='" + scenarioName + '\''
            + ", published=" + metrics.getPublishedCount()
            + ", dropped=" + metrics.getDroppedCount()
            + ", blocked=" + metrics.getBlockedCount()
            + ", consumed=" + metrics.getConsumedCount()
            + ", maxBufferSize=" + metrics.getMaxObservedBufferSize()
            + ", elapsedMillis=" + elapsedMillis
            + '}';
    }
}
