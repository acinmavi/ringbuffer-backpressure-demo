package com.example.ringbufferbackpressure;

import com.example.ringbufferbackpressure.benchmark.BenchmarkSnapshot;
import com.example.ringbufferbackpressure.benchmark.IngestionBenchmark;
import com.example.ringbufferbackpressure.demo.SimulationConfig;
import com.example.ringbufferbackpressure.demo.SimulationReport;
import com.example.ringbufferbackpressure.demo.SimulationRunner;

/**
 * Console entrypoint that runs both backpressure demos and a simple comparison benchmark.
 */
public final class App {
    private App() {
    }

    public static void main(String[] args) throws InterruptedException {
        SimulationRunner runner = new SimulationRunner();
        SimulationReport dropReport = runner.run(SimulationConfig.dropDemo());
        SimulationReport blockReport = runner.run(SimulationConfig.blockDemo());

        IngestionBenchmark benchmark = new IngestionBenchmark();
        BenchmarkSnapshot ringSnapshot = benchmark.runRingBufferScenario();
        BenchmarkSnapshot queueSnapshot = benchmark.runQueueScenario();

        System.out.println("=== Backpressure Demo ===");
        System.out.println(dropReport);
        System.out.println(blockReport);
        System.out.println();
        System.out.println("=== Benchmark Snapshot ===");
        System.out.println(ringSnapshot);
        System.out.println(queueSnapshot);
    }
}
