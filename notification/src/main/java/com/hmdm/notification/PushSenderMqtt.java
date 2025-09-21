package com.hmdm.notification;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.google.inject.name.Named;
import com.hmdm.notification.guice.module.NotificationMqttTaskModule;
import com.hmdm.notification.persistence.domain.PushMessage;
import com.hmdm.persistence.UnsecureDAO;
import com.hmdm.persistence.domain.Device;
import com.hmdm.util.BackgroundTaskRunnerService;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import javax.net.ssl.SSLSocketFactory;

@Singleton
public class PushSenderMqtt implements PushSender {
    private static final Logger log = LoggerFactory.getLogger(PushSenderMqtt.class);
    private static final int MQTT_QOS_LEVEL = 2;
    private final MqttUriUtil.MqttUri mqttUri;
    private final String clientTag;
    private final boolean mqttAuth;
    private final String mqttAdminPassword;
    private final String mqttExternal;
    private final UnsecureDAO unsecureDAO;
    private final MqttThrottledSender throttledSender;
    private final BackgroundTaskRunnerService taskRunner;
    private final MemoryPersistence persistence = new MemoryPersistence();
    private final long mqttDelay;
    private final String sslKeystorePassword;
    private MqttClient client;

    @Inject
    public PushSenderMqtt(@Named("mqtt.server.uri") String serverUri,
            @Named("mqtt.client.tag") String clientTag,
            @Named("mqtt.auth") boolean mqttAuth,
            @Named("mqtt.admin.password") String mqttAdminPassword,
            @Named("mqtt.external") String mqttExternal,
            @Named("mqtt.message.delay") long mqttDelay,
            @Named("ssl.keystore.password") String sslKeystorePassword,
            MqttThrottledSender throttledSender,
            BackgroundTaskRunnerService taskRunner,
            UnsecureDAO unsecureDAO) {
        this.mqttUri = MqttUriUtil.parse(serverUri);
        this.clientTag = clientTag;
        this.mqttAuth = mqttAuth;
        this.mqttAdminPassword = mqttAdminPassword;
        this.mqttExternal = mqttExternal;
        this.mqttDelay = mqttDelay;
        this.sslKeystorePassword = sslKeystorePassword;
        this.throttledSender = throttledSender;
        this.taskRunner = taskRunner;
        this.unsecureDAO = unsecureDAO;
    }


    @Override
    public void init() {
        try {
            boolean useExternal = MqttUriUtil.isExternalEnabled(mqttExternal);

            log.info("Connecting to {} MQTT broker: {}",
                    useExternal ? "external" : "local embedded", mqttUri.toString());

            MqttConnectOptions options = new MqttConnectOptions();
            options.setCleanSession(true);
            options.setAutomaticReconnect(true);
            options.setMqttVersion(MqttConnectOptions.MQTT_VERSION_3_1_1);

            options.setKeepAliveInterval(600);
            log.debug("MQTT server client keepalive interval set to 600 seconds (10 minutes)");

            // Configure SSL if required
            if (mqttUri.isSecure()) {
                SSLSocketFactory sslSocketFactory = MqttUriUtil.configureSSL(mqttUri, sslKeystorePassword);
                if (sslSocketFactory == null) {
                    throw new IllegalStateException(
                            "Failed to create SSL socket factory for secure MQTT connection: " + mqttUri.toString());
                }
                options.setSocketFactory(sslSocketFactory);
            }
            if (mqttAuth) {
                options.setUserName(NotificationMqttTaskModule.MQTT_ADMIN_USERNAME);
                options.setPassword(mqttAdminPassword.toCharArray());
            }

            client = new MqttClient(mqttUri.toString(), "HMDMServer" + clientTag, persistence);
            client.connect(options);
            if (mqttDelay > 0) {
                throttledSender.setClient(client);
                taskRunner.submitTask(throttledSender);
            }
            log.info("MQTT client successfully connected to: {}", mqttUri.toString());
        } catch (Exception e) {
            log.error("Failed to initialize MQTT client. Error: {}", e.getMessage(), e);
            throw new RuntimeException("MQTT initialization failed", e);
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
            mqttMessage.setQos(MQTT_QOS_LEVEL);
            String number = device.getOldNumber() == null ? device.getNumber() : device.getOldNumber();
            if (mqttDelay == 0) {
                client.publish(number, mqttMessage);
            } else {
                throttledSender.send(new MqttEnvelope(number, mqttMessage));
            }

        } catch (Exception e) {
            log.error("Failed to send MQTT message to device {}: {}", message.getDeviceId(), e.getMessage(), e);
        }
        return 0;
    }
}
