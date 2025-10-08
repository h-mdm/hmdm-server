package com.hmdm.notification;

import com.hmdm.notification.persistence.domain.PushMessage;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 * Unit tests for MessageClassifier to ensure proper message priority classification.
 */
public class MessageClassifierTest {

    private final MessageClassifier classifier = new MessageClassifier();

    @Test
    public void testUrgentMessageClassification() {
        // Test urgent message types
        assertEquals(MessagePriority.URGENT, classifier.classify("DEVICE_LOCK"));
        assertEquals(MessagePriority.URGENT, classifier.classify("device_lock"));
        assertEquals(MessagePriority.URGENT, classifier.classify("EMERGENCY_WIPE"));
        assertEquals(MessagePriority.URGENT, classifier.classify("SECURITY_ALERT"));
        assertEquals(MessagePriority.URGENT, classifier.classify("some_wipe_command"));
    }

    @Test
    public void testNormalMessageClassification() {
        // Test normal priority message types
        assertEquals(MessagePriority.NORMAL, classifier.classify("CONFIG_UPDATED"));
        assertEquals(MessagePriority.NORMAL, classifier.classify("APP_CONFIG_UPDATED"));
        assertEquals(MessagePriority.NORMAL, classifier.classify("APP_UPDATED"));
        assertEquals(MessagePriority.NORMAL, classifier.classify("configuration_change"));
    }

    @Test
    public void testRoutineMessageClassification() {
        // Test routine message types - these are messages that don't match URGENT or CONFIG/APP patterns
        assertEquals(MessagePriority.ROUTINE, classifier.classify("STATUS_UPDATE"));
        assertEquals(MessagePriority.ROUTINE, classifier.classify("HEARTBEAT"));
        assertEquals(MessagePriority.ROUTINE, classifier.classify("LOG_ENTRY"));
        assertEquals(MessagePriority.ROUTINE, classifier.classify("random_message"));
    }

    @Test
    public void testNullAndEmptyMessages() {
        // Test edge cases
        assertEquals(MessagePriority.NORMAL, classifier.classify((String) null));
        assertEquals(MessagePriority.NORMAL, classifier.classify(""));
        assertEquals(MessagePriority.NORMAL, classifier.classify((PushMessage) null));
    }

    @Test
    public void testPushMessageClassification() {
        // Test with PushMessage objects
        PushMessage urgentMsg = new PushMessage();
        urgentMsg.setMessageType("DEVICE_LOCK");
        assertEquals(MessagePriority.URGENT, classifier.classify(urgentMsg));

        PushMessage normalMsg = new PushMessage();
        normalMsg.setMessageType("CONFIG_UPDATED");
        assertEquals(MessagePriority.NORMAL, classifier.classify(normalMsg));

        PushMessage statusMsg = new PushMessage();
        statusMsg.setMessageType("STATUS_UPDATE");
        assertEquals(MessagePriority.ROUTINE, classifier.classify(statusMsg)); // STATUS_UPDATE is routine
    }

    @Test
    public void testCaseInsensitivity() {
        // Ensure case insensitive matching
        assertEquals(MessagePriority.URGENT, classifier.classify("device_LOCK"));
        assertEquals(MessagePriority.URGENT, classifier.classify("Device_Lock"));
        assertEquals(MessagePriority.NORMAL, classifier.classify("config_UPDATED"));
        assertEquals(MessagePriority.NORMAL, classifier.classify("Config_Updated"));
    }
}