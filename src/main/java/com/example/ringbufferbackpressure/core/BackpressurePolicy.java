package com.example.ringbufferbackpressure.core;

/**
 * Defines how a producer reacts when the bounded buffer has no free slot.
 */
public enum BackpressurePolicy {
    BLOCK,
    DROP
}
