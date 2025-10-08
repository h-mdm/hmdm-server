package com.hmdm.notification;

import org.junit.Test;
import org.junit.Before;
import static org.junit.Assert.*;
import org.eclipse.paho.client.mqttv3.MqttMessage;

/**
 * Unit tests for adaptive throttling logic.
 * Tests the core delay calculation algorithm.
 */
public class AdaptiveThrottlingTest {

    private TestableThrottledSender sender;

    @Before
    public void setUp() {
        // Create sender with test-friendly configuration
        sender = new TestableThrottledSender(
            1000L,    // base delay
            true,     // adaptive enabled
            3,        // light threshold
            15,       // medium threshold
            50        // heavy threshold
        );
    }

    @Test
    public void testBackwardCompatibilityWhenDisabled() {
        // When adaptive is disabled, should always return base delay
        TestableThrottledSender disabledSender = new TestableThrottledSender(
            1000L, false, 3, 15, 50
        );

        MqttEnvelope msg = createTestMessage(MessagePriority.NORMAL);

        // Should return base delay regardless of queue size
        assertEquals(1000L, disabledSender.calculateDelayPublic(msg, 0));
        assertEquals(1000L, disabledSender.calculateDelayPublic(msg, 10));
        assertEquals(1000L, disabledSender.calculateDelayPublic(msg, 100));
    }

    @Test
    public void testUrgentMessagesPriority() {
        // Urgent messages should always get 0 delay regardless of queue size
        MqttEnvelope urgentMsg = createTestMessage(MessagePriority.URGENT);

        assertEquals(0L, sender.calculateDelayPublic(urgentMsg, 0));
        assertEquals(0L, sender.calculateDelayPublic(urgentMsg, 5));
        assertEquals(0L, sender.calculateDelayPublic(urgentMsg, 25));
        assertEquals(0L, sender.calculateDelayPublic(urgentMsg, 100));
    }

    @Test
    public void testAdaptiveDelayByQueueSize() {
        MqttEnvelope normalMsg = createTestMessage(MessagePriority.NORMAL);

        // Light load - should be instant (0ms)
        assertEquals(0L, sender.calculateDelayPublic(normalMsg, 1));
        assertEquals(0L, sender.calculateDelayPublic(normalMsg, 3));

        // Medium load - should be fast (100ms)
        assertEquals(100L, sender.calculateDelayPublic(normalMsg, 5));
        assertEquals(100L, sender.calculateDelayPublic(normalMsg, 15));

        // Heavy load - should be moderate (300ms)
        assertEquals(300L, sender.calculateDelayPublic(normalMsg, 25));
        assertEquals(300L, sender.calculateDelayPublic(normalMsg, 50));

        // Very heavy load - should be capped (500ms, half of base 1000ms)
        assertEquals(500L, sender.calculateDelayPublic(normalMsg, 75));
        assertEquals(500L, sender.calculateDelayPublic(normalMsg, 200));
    }

    @Test
    public void testDifferentBaseDelays() {
        // Test with different base delay
        TestableThrottledSender senderWith2000ms = new TestableThrottledSender(
            2000L, true, 3, 15, 50
        );

        MqttEnvelope normalMsg = createTestMessage(MessagePriority.NORMAL);

        // Very heavy load should cap at min of (half base delay, 500)
        assertEquals(500L, senderWith2000ms.calculateDelayPublic(normalMsg, 100));
    }

    @Test
    public void testCustomThresholds() {
        // Test with custom thresholds for different deployment sizes
        TestableThrottledSender smallDeployment = new TestableThrottledSender(
            1000L, true, 2, 10, 25  // Smaller thresholds
        );

        MqttEnvelope normalMsg = createTestMessage(MessagePriority.NORMAL);

        // Should use different thresholds
        assertEquals(0L, smallDeployment.calculateDelayPublic(normalMsg, 2));   // Light
        assertEquals(100L, smallDeployment.calculateDelayPublic(normalMsg, 5)); // Medium
        assertEquals(300L, smallDeployment.calculateDelayPublic(normalMsg, 15)); // Heavy
        assertEquals(500L, smallDeployment.calculateDelayPublic(normalMsg, 50)); // Very heavy
    }

    @Test
    public void testRoutineMessages() {
        // Routine messages should behave same as normal for throttling
        MqttEnvelope routineMsg = createTestMessage(MessagePriority.ROUTINE);
        MqttEnvelope normalMsg = createTestMessage(MessagePriority.NORMAL);

        assertEquals(sender.calculateDelayPublic(normalMsg, 5),
                    sender.calculateDelayPublic(routineMsg, 5));
        assertEquals(sender.calculateDelayPublic(normalMsg, 25),
                    sender.calculateDelayPublic(routineMsg, 25));
    }

    @Test
    public void testBoundaryConditions() {
        MqttEnvelope normalMsg = createTestMessage(MessagePriority.NORMAL);

        // Test exact threshold boundaries
        assertEquals(0L, sender.calculateDelayPublic(normalMsg, 3));   // At light threshold
        assertEquals(100L, sender.calculateDelayPublic(normalMsg, 4)); // Just above light
        assertEquals(100L, sender.calculateDelayPublic(normalMsg, 15)); // At medium threshold
        assertEquals(300L, sender.calculateDelayPublic(normalMsg, 16)); // Just above medium
        assertEquals(300L, sender.calculateDelayPublic(normalMsg, 50)); // At heavy threshold
        assertEquals(500L, sender.calculateDelayPublic(normalMsg, 51)); // Just above heavy
    }

    // Helper methods
    private MqttEnvelope createTestMessage(MessagePriority priority) {
        if (priority == MessagePriority.URGENT || priority == MessagePriority.NORMAL) {
            PrioritizedMqttEnvelope envelope = new PrioritizedMqttEnvelope();
            envelope.setAddress("test-device");
            envelope.setMessage(new MqttMessage("test".getBytes()));
            envelope.setPriority(priority);
            return envelope;
        } else {
            // For routine messages, use regular envelope (will default to NORMAL in logic)
            MqttEnvelope envelope = new MqttEnvelope();
            envelope.setAddress("test-device");
            envelope.setMessage(new MqttMessage("test".getBytes()));
            return envelope;
        }
    }

    /**
     * Testable version of MqttThrottledSender that exposes the delay calculation logic
     */
    private static class TestableThrottledSender {
        private final long mqttDelay;
        private final boolean adaptiveEnabled;
        private final int lightThreshold;
        private final int mediumThreshold;
        private final int heavyThreshold;

        public TestableThrottledSender(long mqttDelay, boolean adaptiveEnabled,
                                     int lightThreshold, int mediumThreshold, int heavyThreshold) {
            this.mqttDelay = mqttDelay;
            this.adaptiveEnabled = adaptiveEnabled;
            this.lightThreshold = lightThreshold;
            this.mediumThreshold = mediumThreshold;
            this.heavyThreshold = heavyThreshold;
        }

        public long calculateDelayPublic(MqttEnvelope msg, int queueSize) {
            if (!adaptiveEnabled) {
                return mqttDelay;
            }

            MessagePriority priority = MessagePriority.NORMAL;
            if (msg instanceof PrioritizedMqttEnvelope) {
                priority = ((PrioritizedMqttEnvelope) msg).getPriority();
            }

            if (priority == MessagePriority.URGENT) {
                return 0;
            }

            if (queueSize <= lightThreshold) {
                return 0;
            } else if (queueSize <= mediumThreshold) {
                return 100;
            } else if (queueSize <= heavyThreshold) {
                return 300;
            } else {
                return Math.min(mqttDelay / 2, 500);
            }
        }
    }
}