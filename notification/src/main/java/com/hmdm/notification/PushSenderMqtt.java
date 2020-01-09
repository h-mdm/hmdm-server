package com.hmdm.notification;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.google.inject.name.Named;
import com.hmdm.notification.persistence.domain.PushMessage;
import com.hmdm.persistence.ConfigurationDAO;
import com.hmdm.persistence.DeviceDAO;
import com.hmdm.persistence.domain.Device;
import org.apache.activemq.ActiveMQConnectionFactory;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;
import org.fusesource.mqtt.client.MQTTException;

import javax.jms.*;
import java.util.HashMap;
import java.util.Map;

@Singleton
public class PushSenderMqtt implements PushSender {
    private String serverUri;
    private DeviceDAO deviceDAO;
    private MqttClient client;
    private MemoryPersistence persistence = new MemoryPersistence();

    @Inject
    public PushSenderMqtt(@Named("mqtt.server.uri") String serverUri, DeviceDAO deviceDAO) {
        this.serverUri = serverUri;
        this.deviceDAO = deviceDAO;
    }

    @Override
    public void init() {
        try {
            client = new MqttClient("tcp://" + serverUri, "HMDMServer", persistence);
            MqttConnectOptions options = new MqttConnectOptions();
            options.setCleanSession(true);
            options.setAutomaticReconnect(true);
            client.connect(options);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public int send(PushMessage message) {
        if (client == null || !client.isConnected()) {
            // Not initialized
            return 0;
        }
        Device device = deviceDAO.getDeviceById(message.getDeviceId());
        if (device == null) {
            // We shouldn't be here!
            return 0;
        }
        try {
            String strMessage = "{messageType: \"" + message.getMessageType() + "\"";
            if (message.getPayload() != null) {
                strMessage += ", payload: " + message.getPayload();
            }
            strMessage += "}";

            MqttMessage mqttMessage = new MqttMessage(strMessage.getBytes());
            mqttMessage.setQos(2);
            client.publish(device.getNumber(), mqttMessage);

        } catch (Exception e) {
            e.printStackTrace();
        }
        return 0;
    }
}
