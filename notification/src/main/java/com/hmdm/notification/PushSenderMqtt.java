package com.hmdm.notification;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.hivemq.client.mqtt.MqttClient;
import com.hivemq.client.mqtt.mqtt3.Mqtt3BlockingClient;
import com.hivemq.client.mqtt.datatypes.MqttQos;
import com.hmdm.notification.guice.module.NotificationMqttTaskModule;
import com.hmdm.notification.persistence.domain.PushMessage;
import com.hmdm.persistence.UnsecureDAO;
import com.hmdm.persistence.domain.Device;
import com.hmdm.util.BackgroundTaskRunnerService;
import jakarta.inject.Named;

import java.nio.charset.StandardCharsets;

@Singleton
public class PushSenderMqtt implements PushSender {
    private String serverUri;
    private String clientTag;
    private boolean mqttAuth;
    private String mqttAdminPassword;
    private UnsecureDAO unsecureDAO;
    private Mqtt3BlockingClient client;
    private MqttThrottledSender throttledSender;
    private BackgroundTaskRunnerService taskRunner;
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
            // Parse host and port from serverUri
            String hostPort = serverUri.contains("://")
                    ? serverUri.substring(serverUri.indexOf("://") + 3)
                    : serverUri;
            String host = "localhost";
            int port = 1883;
            if (hostPort.contains(":")) {
                String[] parts = hostPort.split(":");
                host = parts[0];
                port = Integer.parseInt(parts[1]);
            } else {
                host = hostPort;
            }

            var clientBuilder = MqttClient.builder()
                    .useMqttVersion3()
                    .identifier("HMDMServer" + clientTag)
                    .serverHost(host)
                    .serverPort(port)
                    .buildBlocking();

            var connectBuilder = clientBuilder.connectWith()
                    .cleanSession(true);

            if (mqttAuth) {
                connectBuilder.simpleAuth()
                        .username(NotificationMqttTaskModule.MQTT_ADMIN_USERNAME)
                        .password(mqttAdminPassword.getBytes(StandardCharsets.UTF_8))
                        .applySimpleAuth();
            }

            connectBuilder.send();
            client = clientBuilder;

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
        if (client == null) {
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

            byte[] payload = strMessage.getBytes(StandardCharsets.UTF_8);
            String number = device.getOldNumber() == null ? device.getNumber() : device.getOldNumber();
            if (mqttDelay == 0) {
                client.publishWith()
                        .topic(number)
                        .payload(payload)
                        .qos(MqttQos.EXACTLY_ONCE)
                        .send();
            } else {
                throttledSender.send(new MqttEnvelope(number, payload, 2));
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
        return 0;
    }
}
