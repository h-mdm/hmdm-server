package com.hmdm.notification;

import com.google.inject.Inject;
import com.google.inject.name.Named;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttPersistenceException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.atomic.AtomicLong;

public class MqttThrottledSender implements Runnable {

    private BlockingQueue<MqttEnvelope> queue = new LinkedBlockingQueue<>();
    private long mqttDelay;
    private MqttClient mqttClient;
    private static final Logger log = LoggerFactory.getLogger(MqttThrottledSender.class);

    // Adaptive throttling configuration
    private boolean adaptiveEnabled;
    private int lightThreshold;
    private int mediumThreshold;
    private int heavyThreshold;
    private final MessageClassifier classifier = new MessageClassifier();

    // Performance metrics
    private final AtomicLong messagesSentCounter = new AtomicLong(0);
    private final AtomicLong totalDelayTime = new AtomicLong(0);
    private final MqttPerformanceMonitor performanceMonitor = new MqttPerformanceMonitor();
    private long lastQueueSizeWarning = 0;

    public MqttThrottledSender() {}

    @Inject
    public MqttThrottledSender(@Named("mqtt.message.delay") long mqttDelay,
                              @Named("mqtt.adaptive.enabled") boolean adaptiveEnabled,
                              @Named("mqtt.adaptive.light.threshold") int lightThreshold,
                              @Named("mqtt.adaptive.medium.threshold") int mediumThreshold,
                              @Named("mqtt.adaptive.heavy.threshold") int heavyThreshold) {
        this.mqttDelay = mqttDelay;
        this.adaptiveEnabled = adaptiveEnabled;
        this.lightThreshold = lightThreshold;
        this.mediumThreshold = mediumThreshold;
        this.heavyThreshold = heavyThreshold;
    }

    public void setClient(MqttClient mqttClient) {
        this.mqttClient = mqttClient;
    }

    public void send(MqttEnvelope msg) {
        try {
            queue.put(msg);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void run() {
        String mode = adaptiveEnabled ? "adaptive" : "fixed";
        log.info("Push message sending throttled, mode={}, base delay={}ms", mode, mqttDelay);
        while (true) {
            MqttEnvelope msg = null;
            try {
                msg = queue.take();
                long startTime = System.currentTimeMillis();

                if (mqttClient != null) {
                    mqttClient.publish(msg.getAddress(), msg.getMessage());
                    log.debug("Sending MQTT message to " + msg.getAddress());
                } else {
                    log.error("MQTT client not initialized");
                    performanceMonitor.recordError();
                }

                long delay = calculateDelay(msg);
                if (delay > 0) {
                    Thread.sleep(delay);
                    totalDelayTime.addAndGet(delay);
                }

                // Record performance metrics
                long processingTime = System.currentTimeMillis() - startTime;
                MessagePriority priority = (msg instanceof PrioritizedMqttEnvelope) ?
                    ((PrioritizedMqttEnvelope) msg).getPriority() : MessagePriority.NORMAL;

                performanceMonitor.recordMessageProcessed(processingTime, priority);
                performanceMonitor.recordQueueSize(queue.size());
                messagesSentCounter.incrementAndGet();

                // Log queue size warnings if it gets too large
                int currentQueueSize = queue.size();
                if (currentQueueSize > 100 && System.currentTimeMillis() - lastQueueSizeWarning > 60000) {
                    log.warn("MQTT queue size is high: {} messages pending", currentQueueSize);
                    lastQueueSizeWarning = System.currentTimeMillis();
                }
            }
            catch (InterruptedException e) {
                e.printStackTrace();
                return;
            } catch (MqttPersistenceException e) {
                e.printStackTrace();
            } catch (MqttException e) {
                log.error("Failed to send MQTT message to {}: {}",
                    msg != null ? msg.getAddress() : "unknown", e.getMessage());
                performanceMonitor.recordError();
            }
        }

    }

    /**
     * Calculates the appropriate delay for a message based on current queue size,
     * message priority, and adaptive throttling configuration.
     *
     * @param msg the message to calculate delay for
     * @return the delay in milliseconds
     */
    private long calculateDelay(MqttEnvelope msg) {
        if (!adaptiveEnabled) {
            return mqttDelay; // Backward compatibility - use original fixed delay
        }

        int queueSize = queue.size();

        // Determine message priority if we have an enhanced envelope
        MessagePriority priority = MessagePriority.NORMAL;
        if (msg instanceof PrioritizedMqttEnvelope) {
            priority = ((PrioritizedMqttEnvelope) msg).getPriority();
        }

        // Urgent messages always get immediate delivery
        if (priority == MessagePriority.URGENT) {
            log.debug("Urgent message - no delay for {}", msg.getAddress());
            return 0;
        }

        // Adaptive delay based on queue size (optimized for 100-500 device scale)
        if (queueSize <= lightThreshold) {
            // Light load: instant delivery for responsive user experience
            return 0;
        } else if (queueSize <= mediumThreshold) {
            // Medium load: very fast delivery (100ms)
            return 100;
        } else if (queueSize <= heavyThreshold) {
            // Heavy load: fast delivery (300ms, still much better than 1000ms)
            return 300;
        } else {
            // Very heavy load: fall back to half the original delay
            return Math.min(mqttDelay / 2, 500);
        }
    }

    /**
     * Gets performance statistics for monitoring.
     *
     * @return formatted statistics string
     */
    public String getPerformanceStats() {
        long messagesSent = messagesSentCounter.get();
        long totalDelay = totalDelayTime.get();
        double avgDelayMs = messagesSent > 0 ? (double) totalDelay / messagesSent : 0;
        int currentQueueSize = queue.size();

        return String.format("MQTT Performance: sent=%d, avgDelay=%.1fms, queueSize=%d, adaptive=%s",
                messagesSent, avgDelayMs, currentQueueSize, adaptiveEnabled);
    }

    /**
     * Gets detailed health status including performance metrics.
     *
     * @return health status object
     */
    public MqttPerformanceMonitor.MqttHealthStatus getHealthStatus() {
        return performanceMonitor.getHealthStatus();
    }

    /**
     * Resets all performance counters.
     */
    public void resetPerformanceCounters() {
        performanceMonitor.reset();
        messagesSentCounter.set(0);
        totalDelayTime.set(0);
    }
}
