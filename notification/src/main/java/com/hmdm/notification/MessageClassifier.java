package com.hmdm.notification;

import com.hmdm.notification.persistence.domain.PushMessage;

/**
 * Classifies MQTT push messages by priority for adaptive throttling.
 *
 * This service analyzes message types and content to determine
 * appropriate delivery priority levels.
 */
public class MessageClassifier {

    /**
     * Determines the priority of a push message based on its type and content.
     *
     * @param message the push message to classify
     * @return the appropriate priority level for the message
     */
    public MessagePriority classify(PushMessage message) {
        if (message == null || message.getMessageType() == null) {
            return MessagePriority.NORMAL;
        }

        return classify(message.getMessageType());
    }

    /**
     * Determines priority based on message type string directly.
     *
     * @param messageType the message type string
     * @return the appropriate priority level
     */
    public MessagePriority classify(String messageType) {
        if (messageType == null || messageType.isEmpty()) {
            return MessagePriority.NORMAL;
        }

        String type = messageType.toUpperCase();

        if (type.contains("LOCK") ||
            type.contains("WIPE") ||
            type.contains("EMERGENCY") ||
            type.contains("SECURITY")) {
            return MessagePriority.URGENT;
        }

        if (type.contains("CONFIG") ||
            type.contains("APP") ||
            (type.contains("UPDATE") && !type.contains("STATUS"))) {  // Exclude STATUS_UPDATE
            return MessagePriority.NORMAL;
        }

        return MessagePriority.ROUTINE;
    }
}