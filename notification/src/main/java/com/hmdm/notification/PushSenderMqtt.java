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
    private String mqttExternal;
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
            @Named("mqtt.external") String mqttExternal,
            @Named("mqtt.auth") boolean mqttAuth,
            @Named("mqtt.admin.password") String mqttAdminPassword,
            @Named("mqtt.message.delay") long mqttDelay,
            MqttThrottledSender throttledSender,
            BackgroundTaskRunnerService taskRunner,
            UnsecureDAO unsecureDAO) {
        this.serverUri = serverUri;
        this.clientTag = clientTag;
        this.mqttExternal = mqttExternal;
        this.mqttAuth = mqttAuth;
        this.mqttAdminPassword = mqttAdminPassword;
        this.mqttDelay = mqttDelay;
        this.throttledSender = throttledSender;
        this.taskRunner = taskRunner;
        this.unsecureDAO = unsecureDAO;
    }

    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(PushSenderMqtt.class);
    private static final int MAX_CONNECT_RETRIES = 5;
    private static final long RETRY_DELAY_MS = 1000;

    @Override
    public void init() {
        boolean isExternal = "1".equals(mqttExternal) || "true".equalsIgnoreCase(mqttExternal);

        try {
            // Parse protocol, host and port from serverUri
            boolean useSSL = false;
            String hostPort = serverUri;

            if (hostPort.contains("://")) {
                String scheme = hostPort.substring(0, hostPort.indexOf("://")).toLowerCase();
                hostPort = hostPort.substring(hostPort.indexOf("://") + 3);
                if (scheme.equals("ssl") || scheme.equals("mqtts") || scheme.equals("wss")) {
                    useSSL = true;
                }
            }

            String host;
            int port = useSSL ? 8883 : 1883;
            if (hostPort.contains(":")) {
                String[] parts = hostPort.split(":");
                host = parts[0];
                port = Integer.parseInt(parts[1]);
            } else {
                host = hostPort;
            }

            // For embedded broker, always connect to localhost
            if (!isExternal) {
                host = "localhost";
            }

            var clientBuilder = MqttClient.builder()
                    .useMqttVersion3()
                    .identifier("HMDMServer" + clientTag)
                    .serverHost(host)
                    .serverPort(port);

            if (useSSL) {
                if (!isExternal) {
                    // Embedded broker on localhost: disable hostname verification
                    // since the cert is issued for the domain, not "localhost"
                    clientBuilder.sslConfig()
                            .hostnameVerifier((hostname, session) -> true)
                            .applySslConfig();
                } else {
                    clientBuilder.sslWithDefaultConfig();
                }
            }

            var mqttClient = clientBuilder.buildBlocking();

            // Retry connection to handle embedded broker startup delay
            int maxRetries = isExternal ? 1 : MAX_CONNECT_RETRIES;
            for (int attempt = 1; attempt <= maxRetries; attempt++) {
                try {
                    var connectBuilder = mqttClient.connectWith()
                            .cleanSession(true);

                    if (mqttAuth) {
                        connectBuilder.simpleAuth()
                                .username(NotificationMqttTaskModule.MQTT_ADMIN_USERNAME)
                                .password(mqttAdminPassword.getBytes(StandardCharsets.UTF_8))
                                .applySimpleAuth();
                    }

                    connectBuilder.send();
                    log.info("MQTT client connected successfully to " + host + ":" + port
                            + (isExternal ? " (external)" : " (embedded)"));
                    break;
                } catch (Exception e) {
                    if (attempt < maxRetries) {
                        log.warn("MQTT connection attempt " + attempt + " failed, retrying in " + RETRY_DELAY_MS
                                + "ms: " + e.getMessage());
                        Thread.sleep(RETRY_DELAY_MS);
                    } else {
                        throw e;
                    }
                }
            }

            client = mqttClient;

            if (mqttDelay > 0) {
                throttledSender.setClient(client);
                taskRunner.submitTask(throttledSender);
            }
        } catch (Exception e) {
            log.error("Failed to initialize MQTT client: " + e.getMessage(), e);
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
