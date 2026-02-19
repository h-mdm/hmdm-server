package com.hmdm.notification;

import com.google.inject.Inject;
import com.hivemq.client.mqtt.mqtt3.Mqtt3BlockingClient;
import com.hivemq.client.mqtt.datatypes.MqttQos;
import jakarta.inject.Named;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

public class MqttThrottledSender implements Runnable {

    private BlockingQueue<MqttEnvelope> queue = new LinkedBlockingQueue<>();
    private long mqttDelay;
    private volatile Mqtt3BlockingClient mqttClient;
    private static final Logger log = LoggerFactory.getLogger(MqttThrottledSender.class);

    public MqttThrottledSender() {}

    @Inject
    public MqttThrottledSender(@Named("mqtt.message.delay") long mqttDelay) {
        this.mqttDelay = mqttDelay;
    }

    public void setClient(Mqtt3BlockingClient mqttClient) {
        this.mqttClient = mqttClient;
    }

    public void send(MqttEnvelope msg) {
        try {
            queue.put(msg);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            log.warn("Interrupted while enqueuing MQTT message", e);
        }
    }

    @Override
    public void run() {
        log.info("Push message sending throttled, delay=" + mqttDelay + "ms");
        while (!Thread.currentThread().isInterrupted()) {
            try {
                MqttEnvelope msg = queue.take();
                if (mqttClient != null) {
                    mqttClient.publishWith()
                            .topic(msg.getAddress())
                            .payload(msg.getPayload())
                            .qos(msg.getQos())
                            .send();
                    log.debug("Sent MQTT message to " + msg.getAddress());
                } else {
                    log.error("MQTT client not initialized, message to {} discarded", msg.getAddress());
                }
                Thread.sleep(mqttDelay);
            }
            catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                log.info("MQTT throttled sender interrupted, shutting down");
                return;
            } catch (Exception e) {
                log.error("Failed to publish MQTT message: " + e.getMessage(), e);
            }
        }

    }
}
