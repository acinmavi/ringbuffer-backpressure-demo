package com.example.ringbufferbackpressure.buffer;

import java.util.List;

import com.example.ringbufferbackpressure.core.BackpressurePolicy;

/**
 * A monitor-based circular buffer with fixed capacity and explicit backpressure.
 *
 * @param <E> event type stored in the ring
 */
public final class BoundedRingBuffer<E> {
    private final Object[] entries;
    private int head;
    private int tail;
    private int size;

    public BoundedRingBuffer(int capacity) {
        if (capacity <= 0) {
            throw new IllegalArgumentException("capacity must be greater than zero");
        }
        this.entries = new Object[capacity];
    }

    public synchronized boolean publish(E event, BackpressurePolicy policy) throws InterruptedException {
        while (size == entries.length) {
            if (policy == BackpressurePolicy.DROP) {
                return false;
            }
            wait();
        }

        entries[tail] = event;
        tail = (tail + 1) % entries.length;
        size++;
        notifyAll();
        return true;
    }

    public synchronized int drainTo(List<? super E> target, int maxItems) {
        int drained = 0;
        while (drained < maxItems && size > 0) {
            @SuppressWarnings("unchecked")
            E event = (E) entries[head];
            entries[head] = null;
            head = (head + 1) % entries.length;
            size--;
            target.add(event);
            drained++;
        }

        if (drained > 0) {
            notifyAll();
        }
        return drained;
    }

    public synchronized int size() {
        return size;
    }

    public int capacity() {
        return entries.length;
    }
}
