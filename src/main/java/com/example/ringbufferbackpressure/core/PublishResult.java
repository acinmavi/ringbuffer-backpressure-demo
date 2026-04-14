package com.example.ringbufferbackpressure.core;

/**
 * Describes the admission outcome for a single publish attempt.
 */
public enum PublishResult {
    PUBLISHED,
    BLOCKED_THEN_PUBLISHED,
    DROPPED
}
