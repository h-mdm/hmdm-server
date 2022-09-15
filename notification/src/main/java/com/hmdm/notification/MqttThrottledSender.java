package com.hmdm.notification;

import com.google.inject.Inject;
import com.google.inject.name.Named;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttPersistenceException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

public class MqttThrottledSender implements Runnable {

    private BlockingQueue<MqttEnvelope> queue = new LinkedBlockingQueue<>();
    private long mqttDelay;
    private MqttClient mqttClient;
    private static final Logger log = LoggerFactory.getLogger(MqttThrottledSender.class);

    public MqttThrottledSender() {}

    @Inject
    public MqttThrottledSender(@Named("mqtt.message.delay") long mqttDelay) {
        this.mqttDelay = mqttDelay;
    }

    public void setClient(MqttClient mqttClient) {
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
                    mqttClient.publish(msg.getAddress(), msg.getMessage());
                    log.debug("Sending MQTT message to " + msg.getAddress());
                } else {
                    log.error("MQTT client not initialized");
                }
                Thread.sleep(mqttDelay);
            }
            catch (InterruptedException e) {
                e.printStackTrace();
                return;
            } catch (MqttPersistenceException e) {
                e.printStackTrace();
            } catch (MqttException e) {
                e.printStackTrace();
            }
        }

    }
}
