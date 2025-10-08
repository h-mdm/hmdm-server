package com.hmdm.notification;

import com.hmdm.notification.persistence.domain.PushMessage;
import org.junit.Test;
import org.junit.Before;
import static org.junit.Assert.*;
import static org.mockito.Mockito.*;
import org.eclipse.paho.client.mqttv3.MqttMessage;

/**
 * Integration tests for the MQTT system components working together.
 * Tests the integration between PushSenderMqtt, MessageClassifier, and prioritized envelopes.
 */
public class MqttIntegrationTest {

    private MessageClassifier classifier;
    private MockMqttThrottledSender mockThrottledSender;

    @Before
    public void setUp() {
        classifier = new MessageClassifier();
        mockThrottledSender = new MockMqttThrottledSender();
    }

    @Test
    public void testMessagePriorityIntegration() {
        // Test that different message types get classified correctly
        // and create appropriate envelope types

        // Test urgent message
        PushMessage urgentMessage = createPushMessage("DEVICE_LOCK", null);
        MessagePriority urgentPriority = classifier.classify(urgentMessage);
        assertEquals("Urgent message should be classified as URGENT", MessagePriority.URGENT, urgentPriority);

        PrioritizedMqttEnvelope urgentEnvelope = createPrioritizedEnvelope("device123", urgentMessage, urgentPriority);
        assertEquals("Envelope should have URGENT priority", MessagePriority.URGENT, urgentEnvelope.getPriority());

        // Test normal message
        PushMessage normalMessage = createPushMessage("CONFIG_UPDATED", "{\"config\": \"wifi\"}");
        MessagePriority normalPriority = classifier.classify(normalMessage);
        assertEquals("Config message should be classified as NORMAL", MessagePriority.NORMAL, normalPriority);

        PrioritizedMqttEnvelope normalEnvelope = createPrioritizedEnvelope("device456", normalMessage, normalPriority);
        assertEquals("Envelope should have NORMAL priority", MessagePriority.NORMAL, normalEnvelope.getPriority());

        // Test routine message
        PushMessage routineMessage = createPushMessage("STATUS_UPDATE", "{\"battery\": \"75%\"}");
        MessagePriority routinePriority = classifier.classify(routineMessage);
        assertEquals("Status message should be classified as ROUTINE", MessagePriority.ROUTINE, routinePriority);
    }

    @Test
    public void testEnvelopeCreationAndSending() {
        // Test that envelopes are created correctly with proper message content
        PushMessage message = createPushMessage("CONFIG_UPDATED", "{\"app\": \"launcher\"}");
        String deviceAddress = "test-device-789";

        // Create MQTT message content (simulating PushSenderMqtt logic)
        String expectedContent = "{messageType: \"CONFIG_UPDATED\", payload: {\"app\": \"launcher\"}}";
        MqttMessage mqttMessage = new MqttMessage(expectedContent.getBytes());
        mqttMessage.setQos(2); // QoS level used in PushSenderMqtt

        // Create prioritized envelope
        MessagePriority priority = classifier.classify(message);
        PrioritizedMqttEnvelope envelope = new PrioritizedMqttEnvelope(deviceAddress, mqttMessage, priority);

        // Verify envelope properties
        assertEquals("Address should match", deviceAddress, envelope.getAddress());
        assertEquals("Priority should match", MessagePriority.NORMAL, envelope.getPriority());
        assertEquals("Message content should match", expectedContent, new String(envelope.getMessage().getPayload()));
        assertEquals("QoS should be preserved", 2, envelope.getMessage().getQos());

        // Test sending through throttled sender
        mockThrottledSender.send(envelope);
        assertEquals("Mock sender should have received the message", 1, mockThrottledSender.getMessagesSent());
        assertEquals("Last message should match", envelope, mockThrottledSender.getLastMessage());
    }

    @Test
    public void testBulkMessageProcessing() {
        // Test processing multiple messages with different priorities
        // This simulates a bulk configuration rollout scenario

        PushMessage[] messages = {
            createPushMessage("CONFIG_UPDATED", "{\"wifi\": \"new-network\"}"),
            createPushMessage("DEVICE_LOCK", null),  // Security emergency
            createPushMessage("APP_CONFIG_UPDATED", "{\"app\": \"browser\"}"),
            createPushMessage("STATUS_UPDATE", "{\"battery\": \"80%\"}"),
            createPushMessage("CONFIG_UPDATED", "{\"policy\": \"strict\"}")
        };

        String[] deviceAddresses = {"device001", "device002", "device003", "device004", "device005"};

        // Process all messages
        for (int i = 0; i < messages.length; i++) {
            MessagePriority priority = classifier.classify(messages[i]);
            String content = createMqttMessageContent(messages[i]);
            MqttMessage mqttMessage = new MqttMessage(content.getBytes());
            mqttMessage.setQos(2);

            PrioritizedMqttEnvelope envelope = new PrioritizedMqttEnvelope(
                deviceAddresses[i], mqttMessage, priority);
            mockThrottledSender.send(envelope);
        }

        // Verify all messages were processed
        assertEquals("Should have processed all messages", 5, mockThrottledSender.getMessagesSent());

        // Verify priority distribution
        int urgentCount = 0, normalCount = 0, routineCount = 0;
        for (MqttEnvelope envelope : mockThrottledSender.getAllMessages()) {
            if (envelope instanceof PrioritizedMqttEnvelope) {
                MessagePriority priority = ((PrioritizedMqttEnvelope) envelope).getPriority();
                switch (priority) {
                    case URGENT: urgentCount++; break;
                    case NORMAL: normalCount++; break;
                    case ROUTINE: routineCount++; break;
                }
            }
        }

        assertEquals("Should have 1 urgent message (DEVICE_LOCK)", 1, urgentCount);
        assertEquals("Should have 3 normal messages (CONFIG and APP_CONFIG)", 3, normalCount);
        assertEquals("Should have 1 routine message (STATUS)", 1, routineCount);
    }

    @Test
    public void testBackwardCompatibilityWithRegularEnvelopes() {
        // Test that system still works with non-prioritized envelopes (backward compatibility)
        MqttMessage mqttMessage = new MqttMessage("test message".getBytes());
        MqttEnvelope regularEnvelope = new MqttEnvelope("device999", mqttMessage);

        mockThrottledSender.send(regularEnvelope);

        assertEquals("Should handle regular envelopes", 1, mockThrottledSender.getMessagesSent());
        assertEquals("Should store regular envelope", regularEnvelope, mockThrottledSender.getLastMessage());
        assertFalse("Should not be a prioritized envelope",
                   mockThrottledSender.getLastMessage() instanceof PrioritizedMqttEnvelope);
    }

    @Test
    public void testEdgeCasesInMessageClassification() {
        // Test edge cases that could occur in real system

        // Empty message type
        PushMessage emptyTypeMessage = new PushMessage();
        emptyTypeMessage.setMessageType("");
        assertEquals("Empty message type should default to NORMAL",
                    MessagePriority.NORMAL, classifier.classify(emptyTypeMessage));

        // Null message
        assertEquals("Null message should default to NORMAL",
                    MessagePriority.NORMAL, classifier.classify((PushMessage) null));

        // Mixed case with multiple keywords
        assertEquals("Multiple urgent keywords should be URGENT",
                    MessagePriority.URGENT, classifier.classify("EMERGENCY_DEVICE_LOCK"));
        assertEquals("Config with update should be NORMAL",
                    MessagePriority.NORMAL, classifier.classify("NEW_CONFIG_UPDATE_REQUIRED"));
    }

    // Helper methods
    private PushMessage createPushMessage(String messageType, String payload) {
        PushMessage message = new PushMessage();
        message.setMessageType(messageType);
        message.setPayload(payload);
        message.setDeviceId(123); // Mock device ID
        return message;
    }

    private PrioritizedMqttEnvelope createPrioritizedEnvelope(String address, PushMessage pushMessage, MessagePriority priority) {
        String content = createMqttMessageContent(pushMessage);
        MqttMessage mqttMessage = new MqttMessage(content.getBytes());
        mqttMessage.setQos(2);
        return new PrioritizedMqttEnvelope(address, mqttMessage, priority);
    }

    private String createMqttMessageContent(PushMessage message) {
        String content = "{messageType: \"" + message.getMessageType() + "\"";
        if (message.getPayload() != null) {
            content += ", payload: " + message.getPayload();
        }
        content += "}";
        return content;
    }

    /**
     * Mock implementation of MqttThrottledSender for testing
     */
    private static class MockMqttThrottledSender {
        private java.util.List<MqttEnvelope> messages = new java.util.ArrayList<>();

        public void send(MqttEnvelope msg) {
            messages.add(msg);
        }

        public int getMessagesSent() {
            return messages.size();
        }

        public MqttEnvelope getLastMessage() {
            return messages.isEmpty() ? null : messages.get(messages.size() - 1);
        }

        public java.util.List<MqttEnvelope> getAllMessages() {
            return new java.util.ArrayList<>(messages);
        }
    }
}