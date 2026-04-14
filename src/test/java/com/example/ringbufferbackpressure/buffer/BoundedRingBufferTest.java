package com.example.ringbufferbackpressure.buffer;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;

import org.junit.jupiter.api.Test;

import com.example.ringbufferbackpressure.core.BackpressurePolicy;
import com.example.ringbufferbackpressure.model.Event;

class BoundedRingBufferTest {

    @Test
    void shouldRejectPublishWhenFullInDropMode() throws InterruptedException {
        BoundedRingBuffer<Event> buffer = new BoundedRingBuffer<>(2);

        assertThat(buffer.publish(event(1), BackpressurePolicy.DROP)).isTrue();
        assertThat(buffer.publish(event(2), BackpressurePolicy.DROP)).isTrue();
        assertThat(buffer.publish(event(3), BackpressurePolicy.DROP)).isFalse();
        assertThat(buffer.size()).isEqualTo(2);
    }

    @Test
    void shouldDrainInFifoOrder() throws InterruptedException {
        BoundedRingBuffer<Event> buffer = new BoundedRingBuffer<>(4);
        List<Event> drained = new ArrayList<>();

        buffer.publish(event(1), BackpressurePolicy.DROP);
        buffer.publish(event(2), BackpressurePolicy.DROP);
        buffer.publish(event(3), BackpressurePolicy.DROP);

        int drainedCount = buffer.drainTo(drained, 2);

        assertThat(drainedCount).isEqualTo(2);
        assertThat(drained).extracting(Event::getSequence).containsExactly(1L, 2L);
        assertThat(buffer.size()).isEqualTo(1);
    }

    @Test
    void shouldBlockProducerUntilConsumerMakesSpace() throws Exception {
        BoundedRingBuffer<Event> buffer = new BoundedRingBuffer<>(1);
        CountDownLatch ready = new CountDownLatch(1);
        AtomicBoolean completed = new AtomicBoolean(false);
        AtomicLong publishDurationMillis = new AtomicLong();

        assertThat(buffer.publish(event(1), BackpressurePolicy.DROP)).isTrue();

        Thread producer = new Thread(() -> {
            ready.countDown();
            long start = System.nanoTime();
            try {
                buffer.publish(event(2), BackpressurePolicy.BLOCK);
                publishDurationMillis.set(TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - start));
                completed.set(true);
            } catch (InterruptedException ex) {
                Thread.currentThread().interrupt();
            }
        });

        producer.start();
        assertThat(ready.await(1, TimeUnit.SECONDS)).isTrue();
        Thread.sleep(150L);
        assertThat(completed).isFalse();

        List<Event> drained = new ArrayList<>();
        assertThat(buffer.drainTo(drained, 1)).isEqualTo(1);

        producer.join(1000L);
        assertThat(completed).isTrue();
        assertThat(publishDurationMillis.get()).isGreaterThanOrEqualTo(100L);
        assertThat(buffer.size()).isEqualTo(1);
    }

    private Event event(long sequence) {
        return new Event(sequence, "producer-a", System.nanoTime(), "payload-" + sequence);
    }
}
