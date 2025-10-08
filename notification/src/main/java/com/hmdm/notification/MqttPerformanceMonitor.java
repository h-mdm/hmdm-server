package com.hmdm.notification;

import java.util.concurrent.atomic.AtomicLong;

/**
 * Performance monitoring and health checking for MQTT operations.
 *
 * This class tracks performance metrics and provides health status
 * for the adaptive MQTT throttling system.
 */
public class MqttPerformanceMonitor {

    private final AtomicLong messagesProcessed = new AtomicLong(0);
    private final AtomicLong totalProcessingTime = new AtomicLong(0);
    private final AtomicLong urgentMessagesProcessed = new AtomicLong(0);
    private final AtomicLong queueOverflowCount = new AtomicLong(0);
    private final AtomicLong errorCount = new AtomicLong(0);

    private volatile long startTime = System.currentTimeMillis();
    private volatile int maxQueueSizeSeen = 0;

    public void recordMessageProcessed(long processingTimeMs, MessagePriority priority) {
        messagesProcessed.incrementAndGet();
        totalProcessingTime.addAndGet(processingTimeMs);

        if (priority == MessagePriority.URGENT) {
            urgentMessagesProcessed.incrementAndGet();
        }
    }

    public void recordQueueSize(int currentSize) {
        if (currentSize > maxQueueSizeSeen) {
            maxQueueSizeSeen = currentSize;
        }
    }

    public void recordQueueOverflow() {
        queueOverflowCount.incrementAndGet();
    }

    public void recordError() {
        errorCount.incrementAndGet();
    }

    public MqttHealthStatus getHealthStatus() {
        long uptime = System.currentTimeMillis() - startTime;
        long processed = messagesProcessed.get();

        double messagesPerSecond = processed > 0 ? (double) processed / (uptime / 1000.0) : 0;
        double averageProcessingTime = processed > 0 ?
            (double) totalProcessingTime.get() / processed : 0;

        boolean healthy = true;
        StringBuilder issues = new StringBuilder();

        // Check for health issues
        if (queueOverflowCount.get() > 0) {
            healthy = false;
            issues.append("Queue overflows detected (").append(queueOverflowCount.get()).append("). ");
        }

        if (errorCount.get() > processed * 0.1) { // More than 10% error rate
            healthy = false;
            issues.append("High error rate (").append(errorCount.get()).append(" errors). ");
        }

        if (maxQueueSizeSeen > 100) { // For 100-500 device scale, queue >100 indicates issues
            issues.append("Large queue size seen (").append(maxQueueSizeSeen).append("). ");
        }

        return new MqttHealthStatus(
            healthy,
            issues.toString(),
            processed,
            messagesPerSecond,
            averageProcessingTime,
            urgentMessagesProcessed.get(),
            maxQueueSizeSeen,
            queueOverflowCount.get(),
            errorCount.get(),
            uptime
        );
    }

    public void reset() {
        messagesProcessed.set(0);
        totalProcessingTime.set(0);
        urgentMessagesProcessed.set(0);
        queueOverflowCount.set(0);
        errorCount.set(0);
        startTime = System.currentTimeMillis();
        maxQueueSizeSeen = 0;
    }

    /**
     * Health status data class
     */
    public static class MqttHealthStatus {
        private final boolean healthy;
        private final String issues;
        private final long messagesProcessed;
        private final double messagesPerSecond;
        private final double averageProcessingTime;
        private final long urgentMessages;
        private final int maxQueueSize;
        private final long queueOverflows;
        private final long errors;
        private final long uptime;

        public MqttHealthStatus(boolean healthy, String issues, long messagesProcessed,
                               double messagesPerSecond, double averageProcessingTime,
                               long urgentMessages, int maxQueueSize, long queueOverflows,
                               long errors, long uptime) {
            this.healthy = healthy;
            this.issues = issues;
            this.messagesProcessed = messagesProcessed;
            this.messagesPerSecond = messagesPerSecond;
            this.averageProcessingTime = averageProcessingTime;
            this.urgentMessages = urgentMessages;
            this.maxQueueSize = maxQueueSize;
            this.queueOverflows = queueOverflows;
            this.errors = errors;
            this.uptime = uptime;
        }

        @Override
        public String toString() {
            return String.format(
                "MQTT Health Status: %s%n" +
                "  Healthy: %s%n" +
                "  Issues: %s%n" +
                "  Messages Processed: %d%n" +
                "  Messages/Second: %.2f%n" +
                "  Average Processing Time: %.2f ms%n" +
                "  Urgent Messages: %d%n" +
                "  Max Queue Size: %d%n" +
                "  Queue Overflows: %d%n" +
                "  Errors: %d%n" +
                "  Uptime: %d ms",
                healthy ? "HEALTHY" : "DEGRADED",
                healthy,
                issues.isEmpty() ? "None" : issues,
                messagesProcessed,
                messagesPerSecond,
                averageProcessingTime,
                urgentMessages,
                maxQueueSize,
                queueOverflows,
                errors,
                uptime
            );
        }

        // Getters
        public boolean isHealthy() { return healthy; }
        public String getIssues() { return issues; }
        public long getMessagesProcessed() { return messagesProcessed; }
        public double getMessagesPerSecond() { return messagesPerSecond; }
        public double getAverageProcessingTime() { return averageProcessingTime; }
        public long getUrgentMessages() { return urgentMessages; }
        public int getMaxQueueSize() { return maxQueueSize; }
        public long getQueueOverflows() { return queueOverflows; }
        public long getErrors() { return errors; }
        public long getUptime() { return uptime; }
    }
}