package com.hmdm.notification;

/**
 * Message priority levels for adaptive MQTT throttling.
 *
 * This enum defines priority levels that determine how quickly messages
 * are delivered based on their importance and urgency.
 */
public enum MessagePriority {
    /**
     * Urgent messages that require immediate delivery (0ms delay).
     * Examples: security locks, emergency commands, device wipe.
     */
    URGENT,

    /**
     * Normal priority messages for regular operations.
     * Examples: configuration updates, application installations.
     * Uses adaptive delay based on queue size.
     */
    NORMAL,

    /**
     * Routine messages that can tolerate longer delays.
     * Examples: status updates, heartbeats, logging.
     * Uses standard delay mechanisms.
     */
    ROUTINE
}