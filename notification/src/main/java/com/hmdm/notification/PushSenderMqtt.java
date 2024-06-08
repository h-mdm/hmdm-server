package com.hmdm.notification;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.google.inject.name.Named;
import com.hmdm.notification.guice.module.NotificationMqttTaskModule;
import com.hmdm.notification.persistence.domain.PushMessage;
import com.hmdm.persistence.UnsecureDAO;
import com.hmdm.persistence.domain.Device;
import com.hmdm.util.BackgroundTaskRunnerService;
import com.hmdm.util.CryptoUtil;
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
    private String clientTag;
    private boolean mqttAuth;
    private String mqttAdminPassword;
    private UnsecureDAO unsecureDAO;
    private MqttClient client;
    private MqttThrottledSender throttledSender;
    private BackgroundTaskRunnerService taskRunner;
    private MemoryPersistence persistence = new MemoryPersistence();
    private long mqttDelay;

    @Inject
    public PushSenderMqtt(@Named("mqtt.server.uri") String serverUri,
                          @Named("mqtt.client.tag") String clientTag,
                          @Named("mqtt.auth") boolean mqttAuth,
                          @Named("mqtt.admin.password") String mqttAdminPassword,
                          @Named("mqtt.message.delay") long mqttDelay,
                          MqttThrottledSender throttledSender,
                          BackgroundTaskRunnerService taskRunner,
                          UnsecureDAO unsecureDAO) {
        this.serverUri = serverUri;
        this.clientTag = clientTag;
        this.mqttAuth = mqttAuth;
        this.mqttAdminPassword = mqttAdminPassword;
        this.mqttDelay = mqttDelay;
        this.throttledSender = throttledSender;
        this.taskRunner = taskRunner;
        this.unsecureDAO = unsecureDAO;
    }

    @Override
    public void init() {
        try {
            client = new MqttClient("tcp://" + serverUri, "HMDMServer" + clientTag, persistence);
            MqttConnectOptions options = new MqttConnectOptions();
            options.setCleanSession(true);
            options.setAutomaticReconnect(true);
            if (mqttAuth) {
                options.setUserName(NotificationMqttTaskModule.MQTT_ADMIN_USERNAME);
                options.setPassword(mqttAdminPassword.toCharArray());
            }
            client.connect(options);

            if (mqttDelay > 0) {
                throttledSender.setClient(client);
                taskRunner.submitTask(throttledSender);
            }
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
        // Since this method is used by scheduled task service which is impersonated,
        // we use UnsecureDAO here (which doesn't check the signed user).
        Device device = unsecureDAO.getDeviceById(message.getDeviceId());
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
            String number = device.getOldNumber() == null ? device.getNumber() : device.getOldNumber();
            if (mqttDelay == 0) {
                client.publish(number, mqttMessage);
            } else {
                throttledSender.send(new MqttEnvelope(number, mqttMessage));
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
        return 0;
    }
}
