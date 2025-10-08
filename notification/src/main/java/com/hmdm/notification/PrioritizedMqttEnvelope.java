package com.hmdm.notification;

/**
 * Enhanced MQTT message envelope that includes priority information
 * for adaptive throttling optimization.
 */
public class PrioritizedMqttEnvelope extends MqttEnvelope {
    private MessagePriority priority;

    public PrioritizedMqttEnvelope() {
        super();
        this.priority = MessagePriority.NORMAL; // Default priority
    }

    public PrioritizedMqttEnvelope(String address, org.eclipse.paho.client.mqttv3.MqttMessage message, MessagePriority priority) {
        super(address, message);
        this.priority = priority;
    }

    public MessagePriority getPriority() {
        return priority;
    }

    public void setPriority(MessagePriority priority) {
        this.priority = priority;
    }
}