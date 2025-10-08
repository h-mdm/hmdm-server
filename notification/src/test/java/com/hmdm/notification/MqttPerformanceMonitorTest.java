package com.hmdm.notification;

import org.junit.Test;
import org.junit.Before;
import static org.junit.Assert.*;

/**
 * Unit tests for MqttPerformanceMonitor to ensure proper metrics tracking.
 */
public class MqttPerformanceMonitorTest {

    private MqttPerformanceMonitor monitor;

    @Before
    public void setUp() {
        monitor = new MqttPerformanceMonitor();
    }

    @Test
    public void testInitialState() {
        MqttPerformanceMonitor.MqttHealthStatus status = monitor.getHealthStatus();

        assertTrue("Should start healthy", status.isHealthy());
        assertEquals("No messages processed initially", 0L, status.getMessagesProcessed());
        assertEquals("No urgent messages initially", 0L, status.getUrgentMessages());
        assertEquals("No queue overflows initially", 0L, status.getQueueOverflows());
        assertEquals("No errors initially", 0L, status.getErrors());
        assertEquals("Max queue size should be 0", 0, status.getMaxQueueSize());
    }

    @Test
    public void testMessageProcessingTracking() {
        // Record some message processing
        monitor.recordMessageProcessed(100, MessagePriority.NORMAL);
        monitor.recordMessageProcessed(200, MessagePriority.URGENT);
        monitor.recordMessageProcessed(150, MessagePriority.ROUTINE);

        MqttPerformanceMonitor.MqttHealthStatus status = monitor.getHealthStatus();

        assertEquals("Should track total messages", 3L, status.getMessagesProcessed());
        assertEquals("Should track urgent messages", 1L, status.getUrgentMessages());
        assertEquals("Should calculate average processing time", 150.0, status.getAverageProcessingTime(), 0.1);
    }

    @Test
    public void testQueueSizeTracking() {
        // Record various queue sizes
        monitor.recordQueueSize(5);
        monitor.recordQueueSize(15);
        monitor.recordQueueSize(25);
        monitor.recordQueueSize(10); // Lower size

        MqttPerformanceMonitor.MqttHealthStatus status = monitor.getHealthStatus();

        assertEquals("Should track maximum queue size seen", 25, status.getMaxQueueSize());
    }

    @Test
    public void testErrorTracking() {
        // Record some errors
        monitor.recordError();
        monitor.recordError();
        monitor.recordMessageProcessed(100, MessagePriority.NORMAL);

        MqttPerformanceMonitor.MqttHealthStatus status = monitor.getHealthStatus();

        assertEquals("Should track error count", 2L, status.getErrors());
        assertFalse("Should be unhealthy with high error rate", status.isHealthy());
        assertTrue("Should mention error rate in issues", status.getIssues().contains("error rate"));
    }

    @Test
    public void testQueueOverflowDetection() {
        // Record queue overflow
        monitor.recordQueueOverflow();
        monitor.recordQueueOverflow();

        MqttPerformanceMonitor.MqttHealthStatus status = monitor.getHealthStatus();

        assertEquals("Should track overflow count", 2L, status.getQueueOverflows());
        assertFalse("Should be unhealthy with queue overflows", status.isHealthy());
        assertTrue("Should mention queue overflows in issues", status.getIssues().contains("Queue overflows"));
    }

    @Test
    public void testLargeQueueSizeWarning() {
        // Record large queue size (warning condition)
        monitor.recordQueueSize(150);

        MqttPerformanceMonitor.MqttHealthStatus status = monitor.getHealthStatus();

        // Should still be healthy but mention the large queue size
        assertTrue("Should still be healthy (warning only)", status.isHealthy());
        assertTrue("Should mention large queue size", status.getIssues().contains("Large queue size"));
    }

    @Test
    public void testHealthySystemWithGoodMetrics() {
        // Simulate a healthy system
        for (int i = 0; i < 100; i++) {
            monitor.recordMessageProcessed(50 + (i % 100), MessagePriority.NORMAL);
            monitor.recordQueueSize(i % 10); // Queue size varies but stays small
        }

        // Add a few urgent messages
        monitor.recordMessageProcessed(10, MessagePriority.URGENT);
        monitor.recordMessageProcessed(5, MessagePriority.URGENT);

        MqttPerformanceMonitor.MqttHealthStatus status = monitor.getHealthStatus();

        assertTrue("Should be healthy with good metrics", status.isHealthy());
        assertEquals("Should track all messages", 102L, status.getMessagesProcessed());
        assertEquals("Should track urgent messages", 2L, status.getUrgentMessages());
        assertTrue("Should have reasonable processing rate", status.getMessagesPerSecond() > 0);
        assertEquals("Issues should be empty or mention queue size only", true,
                    status.getIssues().isEmpty() || status.getIssues().contains("queue size"));
    }

    @Test
    public void testPerformanceMetricsCalculation() throws InterruptedException {
        long startTime = System.currentTimeMillis();

        // Process messages with small delay to test timing
        monitor.recordMessageProcessed(100, MessagePriority.NORMAL);
        Thread.sleep(10);
        monitor.recordMessageProcessed(200, MessagePriority.URGENT);
        Thread.sleep(10);

        MqttPerformanceMonitor.MqttHealthStatus status = monitor.getHealthStatus();

        assertTrue("Should have positive uptime", status.getUptime() > 0);
        assertTrue("Should calculate messages per second", status.getMessagesPerSecond() > 0);
        assertEquals("Should calculate average processing time", 150.0, status.getAverageProcessingTime(), 0.1);
    }

    @Test
    public void testReset() {
        // Add some data
        monitor.recordMessageProcessed(100, MessagePriority.NORMAL);
        monitor.recordError();
        monitor.recordQueueOverflow();
        monitor.recordQueueSize(50);

        // Verify data is there
        MqttPerformanceMonitor.MqttHealthStatus statusBefore = monitor.getHealthStatus();
        assertTrue("Should have data before reset", statusBefore.getMessagesProcessed() > 0);

        // Reset
        monitor.reset();

        // Verify reset worked
        MqttPerformanceMonitor.MqttHealthStatus statusAfter = monitor.getHealthStatus();
        assertEquals("Messages should be reset", 0L, statusAfter.getMessagesProcessed());
        assertEquals("Errors should be reset", 0L, statusAfter.getErrors());
        assertEquals("Queue overflows should be reset", 0L, statusAfter.getQueueOverflows());
        assertEquals("Max queue size should be reset", 0, statusAfter.getMaxQueueSize());
        assertTrue("Should be healthy after reset", statusAfter.isHealthy());
    }

    @Test
    public void testHealthStatusToString() {
        // Add some data for a meaningful status
        monitor.recordMessageProcessed(100, MessagePriority.NORMAL);
        monitor.recordMessageProcessed(50, MessagePriority.URGENT);
        monitor.recordQueueSize(25);

        MqttPerformanceMonitor.MqttHealthStatus status = monitor.getHealthStatus();
        String statusString = status.toString();

        // Verify the string contains key information
        assertTrue("Should contain healthy status", statusString.contains("HEALTHY"));
        assertTrue("Should contain message count", statusString.contains("Messages Processed: 2"));
        assertTrue("Should contain urgent messages", statusString.contains("Urgent Messages: 1"));
        assertTrue("Should contain queue size info", statusString.contains("Max Queue Size: 25"));
    }
}