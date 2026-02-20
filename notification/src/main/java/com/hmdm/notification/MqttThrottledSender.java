package com.hmdm.notification;

import jakarta.inject.Inject;
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
    private static final int MAX_PUBLISH_RETRIES = 3;
    private static final long RETRY_BACKOFF_MS = 2000;

    public MqttThrottledSender() {
    }

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
        log.info("Push message sending throttled, delay={}ms", mqttDelay);
        while (!Thread.currentThread().isInterrupted()) {
            try {
                MqttEnvelope msg = queue.take();
                if (mqttClient != null) {
                    publishWithRetry(msg);
                } else {
                    log.error("MQTT client not initialized, message to {} discarded", msg.getAddress());
                }
                Thread.sleep(mqttDelay);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                log.info("MQTT throttled sender interrupted, shutting down");
                return;
            } catch (Exception e) {
                log.error("Failed to publish MQTT message: {}", e.getMessage(), e);
            }
        }
    }

    private void publishWithRetry(MqttEnvelope msg) throws InterruptedException {
        for (int attempt = 1; attempt <= MAX_PUBLISH_RETRIES; attempt++) {
            try {
                mqttClient.publishWith()
                        .topic(msg.getAddress())
                        .payload(msg.getPayload())
                        .qos(msg.getQos())
                        .send();
                log.debug("Sent MQTT message to {}", msg.getAddress());
                return;
            } catch (Exception e) {
                if (attempt < MAX_PUBLISH_RETRIES) {
                    log.warn("MQTT publish attempt {} failed for {}, retrying in {}ms: {}",
                            attempt, msg.getAddress(), RETRY_BACKOFF_MS, e.getMessage());
                    Thread.sleep(RETRY_BACKOFF_MS);
                } else {
                    log.error("MQTT publish failed after {} attempts for {}: {}",
                            MAX_PUBLISH_RETRIES, msg.getAddress(), e.getMessage(), e);
                }
            }
        }
    }
}
