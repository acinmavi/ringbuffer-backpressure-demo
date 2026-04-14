package com.example.ringbufferbackpressure.benchmark;

/**
 * Captures a single benchmark result so scenarios can be compared side by side.
 */
public final class BenchmarkSnapshot {
    private final String scenarioName;
    private final long publishedCount;
    private final long consumedCount;
    private final long droppedCount;
    private final long blockedCount;
    private final long elapsedMillis;

    public BenchmarkSnapshot(String scenarioName,
                             long publishedCount,
                             long consumedCount,
                             long droppedCount,
                             long blockedCount,
                             long elapsedMillis) {
        this.scenarioName = scenarioName;
        this.publishedCount = publishedCount;
        this.consumedCount = consumedCount;
        this.droppedCount = droppedCount;
        this.blockedCount = blockedCount;
        this.elapsedMillis = elapsedMillis;
    }

    public String getScenarioName() {
        return scenarioName;
    }

    public long getPublishedCount() {
        return publishedCount;
    }

    public long getConsumedCount() {
        return consumedCount;
    }

    public long getDroppedCount() {
        return droppedCount;
    }

    public long getBlockedCount() {
        return blockedCount;
    }

    public long getElapsedMillis() {
        return elapsedMillis;
    }

    @Override
    public String toString() {
        return "BenchmarkSnapshot{"
            + "scenarioName='" + scenarioName + '\''
            + ", publishedCount=" + publishedCount
            + ", consumedCount=" + consumedCount
            + ", droppedCount=" + droppedCount
            + ", blockedCount=" + blockedCount
            + ", elapsedMillis=" + elapsedMillis
            + '}';
    }
}
