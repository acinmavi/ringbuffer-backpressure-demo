package com.example.ringbufferbackpressure.benchmark;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class IngestionBenchmarkTest {

    @Test
    void shouldProduceComparableBenchmarkSnapshotsForQueueAndRingBuffer() throws InterruptedException {
        IngestionBenchmark benchmark = new IngestionBenchmark();

        BenchmarkSnapshot ringSnapshot = benchmark.runRingBufferScenario();
        BenchmarkSnapshot queueSnapshot = benchmark.runQueueScenario();

        assertThat(ringSnapshot.getPublishedCount()).isGreaterThan(0L);
        assertThat(queueSnapshot.getPublishedCount()).isGreaterThan(0L);
        assertThat(ringSnapshot.getElapsedMillis()).isGreaterThan(0L);
        assertThat(queueSnapshot.getElapsedMillis()).isGreaterThan(0L);
        assertThat(ringSnapshot.getScenarioName()).isEqualTo("ring-buffer");
        assertThat(queueSnapshot.getScenarioName()).isEqualTo("array-blocking-queue");
    }
}
