package com.hmdm.notification;

import com.google.inject.Inject;
import jakarta.inject.Singleton;
import com.hivemq.client.mqtt.MqttClient;
import com.hivemq.client.mqtt.mqtt3.Mqtt3BlockingClient;
import com.hivemq.client.mqtt.datatypes.MqttQos;
import com.hmdm.notification.guice.module.NotificationMqttTaskModule;
import com.hmdm.notification.persistence.domain.PushMessage;
import com.hmdm.persistence.UnsecureDAO;
import com.hmdm.persistence.domain.Device;
import com.hmdm.util.BackgroundTaskRunnerService;
import jakarta.inject.Named;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URI;
import java.nio.charset.StandardCharsets;

@Singleton
public class PushSenderMqtt implements PushSender {
    private String serverUri;
    private String clientTag;
    private String mqttExternal;
    private boolean mqttAuth;
    private String mqttAdminPassword;
    private UnsecureDAO unsecureDAO;
    private volatile Mqtt3BlockingClient client;
    private MqttThrottledSender throttledSender;
    private BackgroundTaskRunnerService taskRunner;
    private long mqttDelay;

    // Connection parameters saved for reconnection
    private String connectHost;
    private int connectPort;
    private boolean connectSSL;
    private boolean isExternal;

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

    private static final Logger log = LoggerFactory.getLogger(PushSenderMqtt.class);
    private static final int MAX_CONNECT_RETRIES = 5;
    private static final int EXTERNAL_CONNECT_RETRIES = 3;
    private static final long RETRY_DELAY_MS = 1000;

    @Override
    public void init() {
        isExternal = "1".equals(mqttExternal) || "true".equalsIgnoreCase(mqttExternal);

        try {
            // Parse protocol, host and port using URI for IPv6 safety
            URI uri = NotificationMqttTaskModule.parseServerUri(serverUri);
            String scheme = uri.getScheme() != null ? uri.getScheme() : "mqtt";
            connectSSL = "ssl".equals(scheme) || "mqtts".equals(scheme);
            connectPort = uri.getPort() > 0 ? uri.getPort() : (connectSSL ? 8883 : 1883);
            connectHost = uri.getHost();

            // For embedded broker, always connect to localhost
            if (!isExternal) {
                connectHost = "localhost";
            }

            connectAndStart();
        } catch (Exception e) {
            log.error("Failed to initialize MQTT client: " + e.getMessage(), e);
        }
    }

    private Mqtt3BlockingClient buildClient() {
        var clientBuilder = MqttClient.builder()
                .useMqttVersion3()
                .identifier("HMDMServer" + clientTag)
                .serverHost(connectHost)
                .serverPort(connectPort);

        if (connectSSL) {
            if (!isExternal) {
                clientBuilder.sslConfig()
                        .hostnameVerifier((hostname, session) -> true)
                        .applySslConfig();
            } else {
                clientBuilder.sslWithDefaultConfig();
            }
        }

        return clientBuilder.buildBlocking();
    }

    private void connectClient(Mqtt3BlockingClient mqttClient) {
        var connectBuilder = mqttClient.connectWith()
                .cleanSession(true);

        if (mqttAuth) {
            connectBuilder.simpleAuth()
                    .username(NotificationMqttTaskModule.MQTT_ADMIN_USERNAME)
                    .password(mqttAdminPassword.getBytes(StandardCharsets.UTF_8))
                    .applySimpleAuth();
        }

        connectBuilder.send();
    }

    private void connectAndStart() throws InterruptedException {
        var mqttClient = buildClient();

        // Retry connection to handle embedded broker startup delay
        int maxRetries = isExternal ? EXTERNAL_CONNECT_RETRIES : MAX_CONNECT_RETRIES;
        for (int attempt = 1; attempt <= maxRetries; attempt++) {
            try {
                connectClient(mqttClient);
                log.info("MQTT client connected successfully to " + connectHost + ":" + connectPort
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

        // Set throttled sender client before assigning the field so that
        // concurrent send() calls see a fully initialized state
        if (mqttDelay > 0) {
            throttledSender.setClient(mqttClient);
            if (client == null) {
                // Only start the sender thread on initial connect, not on reconnect
                taskRunner.submitTask(throttledSender);
            }
        }

        client = mqttClient;
    }

    /**
     * Attempts to reconnect to the MQTT broker.
     * Returns true if reconnection succeeded.
     */
    private synchronized boolean reconnect() {
        try {
            log.info("Attempting MQTT reconnection to " + connectHost + ":" + connectPort);
            connectAndStart();
            return true;
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            log.error("MQTT reconnection interrupted");
            return false;
        } catch (Exception e) {
            log.error("MQTT reconnection failed: " + e.getMessage());
            return false;
        }
    }

    @Override
    public synchronized int send(PushMessage message) {
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
            String strMessage = "{\"messageType\": \"" + message.getMessageType() + "\"";
            if (message.getPayload() != null) {
                strMessage += ", \"payload\": " + message.getPayload();
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
                throttledSender.send(new MqttEnvelope(number, payload, MqttQos.EXACTLY_ONCE));
            }

        } catch (Exception e) {
            log.error("Failed to send MQTT message: " + e.getMessage(), e);
            // Attempt reconnection on publish failure
            reconnect();
        }
        return 0;
    }
}
