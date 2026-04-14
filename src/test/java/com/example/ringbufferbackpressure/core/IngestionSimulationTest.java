package com.example.ringbufferbackpressure.core;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

import com.example.ringbufferbackpressure.demo.SimulationConfig;
import com.example.ringbufferbackpressure.demo.SimulationReport;
import com.example.ringbufferbackpressure.demo.SimulationRunner;

class IngestionSimulationTest {

    @Test
    void shouldReportDroppedEventsWhenDropPolicySaturatesBuffer() throws InterruptedException {
        SimulationRunner runner = new SimulationRunner();
        SimulationConfig config = SimulationConfig.dropDemo();

        SimulationReport report = runner.run(config);

        assertThat(report.getMetrics().getPublishedCount()).isGreaterThan(0L);
        assertThat(report.getMetrics().getDroppedCount()).isGreaterThan(0L);
        assertThat(report.getMetrics().getConsumedCount()).isGreaterThan(0L);
        assertThat(report.isBufferReachedCapacity()).isTrue();
    }

    @Test
    void shouldRecordBlockedPublishersWhenBlockPolicySaturatesBuffer() throws InterruptedException {
        SimulationRunner runner = new SimulationRunner();
        SimulationConfig config = SimulationConfig.blockDemo();

        SimulationReport report = runner.run(config);

        assertThat(report.getMetrics().getPublishedCount()).isGreaterThan(0L);
        assertThat(report.getMetrics().getBlockedCount()).isGreaterThan(0L);
        assertThat(report.getMetrics().getConsumedCount()).isGreaterThan(0L);
        assertThat(report.isBufferReachedCapacity()).isTrue();
    }
}
