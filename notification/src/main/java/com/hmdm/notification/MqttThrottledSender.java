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
    private Mqtt3BlockingClient mqttClient;
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
            e.printStackTrace();
        }
    }

    @Override
    public void run() {
        log.info("Push message sending throttled, delay=" + mqttDelay + "ms");
        while (true) {
            try {
                MqttEnvelope msg = queue.take();
                if (mqttClient != null) {
                    MqttQos qos = msg.getQos() == 2 ? MqttQos.EXACTLY_ONCE
                            : msg.getQos() == 1 ? MqttQos.AT_LEAST_ONCE : MqttQos.AT_MOST_ONCE;
                    mqttClient.publishWith()
                            .topic(msg.getAddress())
                            .payload(msg.getPayload())
                            .qos(qos)
                            .send();
                    log.debug("Sending MQTT message to " + msg.getAddress());
                } else {
                    log.error("MQTT client not initialized");
                }
                Thread.sleep(mqttDelay);
            }
            catch (InterruptedException e) {
                e.printStackTrace();
                return;
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

    }
}
