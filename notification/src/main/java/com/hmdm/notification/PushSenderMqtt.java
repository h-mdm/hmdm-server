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

import javax.jms.*;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManagerFactory;
import java.io.FileInputStream;
import java.lang.IllegalStateException;
import java.net.URI;
import java.security.KeyStore;

@Singleton
public class PushSenderMqtt implements PushSender {
    private static final Logger log = LoggerFactory.getLogger(PushSenderMqtt.class);
    private static final int MQTT_QOS_LEVEL = 2;
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
    private String sslKeystorePassword;
    private String sslProtocols;

    @Inject
    public PushSenderMqtt(@Named("mqtt.server.uri") String serverUri,
            @Named("mqtt.client.tag") String clientTag,
            @Named("mqtt.auth") boolean mqttAuth,
            @Named("mqtt.admin.password") String mqttAdminPassword,
            @Named("mqtt.message.delay") long mqttDelay,
            @Named("ssl.keystore.password") String sslKeystorePassword,
            MqttThrottledSender throttledSender,
            BackgroundTaskRunnerService taskRunner,
            UnsecureDAO unsecureDAO) {
        this.serverUri = serverUri;
        this.clientTag = clientTag;
        this.mqttAuth = mqttAuth;
        this.mqttAdminPassword = mqttAdminPassword;
        this.mqttDelay = mqttDelay;
        this.sslKeystorePassword = sslKeystorePassword;
        this.throttledSender = throttledSender;
        this.taskRunner = taskRunner;
        this.unsecureDAO = unsecureDAO;

        // Validate serverUri format
        validateServerUri();
    }

    private void validateServerUri() {
        if (serverUri == null || serverUri.trim().isEmpty()) {
            throw new IllegalArgumentException("MQTT server URI cannot be null or empty");
        }
        try {
            URI uri = new URI(serverUri);
            String scheme = uri.getScheme();
            String host = uri.getHost();
            int port = uri.getPort();
            if (scheme == null) {
                scheme = "tcp";
            } else if (!scheme.equals("tcp") && !scheme.equals("ssl") &&
                    !scheme.equals("mqtt") && !scheme.equals("mqtts")) {
                throw new IllegalArgumentException("Invalid MQTT protocol scheme: " + scheme +
                        ". Supported protocols: tcp://, ssl://, mqtt://, mqtts://");
            }
            if (host == null || host.trim().isEmpty()) {
                host = "localhost";
            }
            if (port == -1) {
                port = 31000;
            } else if (port < 1 || port > 65535) {
                throw new IllegalArgumentException("MQTT server port must be between 1 and 65535, got: " + port);
            }
            this.serverUri = scheme + "://" + host + ":" + port;
        } catch (Exception e) {
            throw new IllegalArgumentException("Invalid MQTT server URI format: " + serverUri +
                    ". Expected format: protocol://host:port (e.g., tcp://localhost:1883, ssl://mqtt.example.com:8883)",
                    e);
        }
    }

    private SSLSocketFactory createSSLSocketFactory() throws Exception {
        try {
            URI uri = new URI(serverUri);
            String domain = uri.getHost();

            // Use JKS keystore following letsencrypt-ssl.sh pattern
            String tomcatHome = System.getProperty("catalina.home");
            if (tomcatHome == null) {
                tomcatHome = "/var/lib/tomcat9"; // Default fallback
            }

            String keystorePath = tomcatHome + "/ssl/" + domain + ".jks";

            // Validate keystore file exists
            java.io.File keystoreFile = new java.io.File(keystorePath);
            if (!keystoreFile.exists()) {
                throw new IllegalStateException("SSL keystore not found at: " + keystorePath +
                        ". Ensure SSL certificates are properly configured for domain: " + domain);
            }
            if (!keystoreFile.canRead()) {
                throw new IllegalStateException("Cannot read SSL keystore at: " + keystorePath +
                        ". Check file permissions.");
            }
            KeyStore keyStore = KeyStore.getInstance("JKS");
            try (FileInputStream fis = new FileInputStream(keystorePath)) {
                keyStore.load(fis, sslKeystorePassword.toCharArray());
            } catch (Exception e) {
                throw new IllegalStateException("Failed to load SSL keystore from: " + keystorePath +
                        ". Check keystore password and file integrity.", e);
            }
            TrustManagerFactory tmf = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
            tmf.init(keyStore);

            String[] requestedProtocols = sslProtocols.split(",");
            for (int i = 0; i < requestedProtocols.length; i++) {
                requestedProtocols[i] = requestedProtocols[i].trim();
            }
            String contextProtocol = requestedProtocols[0];
            if ("TLS".equals(contextProtocol)) {
                contextProtocol = "TLS";
            }
            SSLContext sslContext = SSLContext.getInstance(contextProtocol);
            sslContext.init(null, tmf.getTrustManagers(), null);

            return sslContext.getSocketFactory();
        } catch (Exception e) {
            if (e instanceof IllegalStateException) {
                throw e;
            }
            throw new IllegalStateException("Failed to create SSL socket factory for MQTT connection: " +
                    serverUri + ". " + e.getMessage(), e);
        }
    }

    @Override
    public void init() {
        try {
            MqttConnectOptions options = new MqttConnectOptions();
            options.setCleanSession(true);
            options.setAutomaticReconnect(true);

            // Configure SSL based on serverUri protocol
            if (serverUri.startsWith("ssl://") || serverUri.startsWith("mqtts://")) {
                SSLSocketFactory sslSocketFactory = createSSLSocketFactory();
                if (sslSocketFactory == null) {
                    throw new IllegalStateException(
                            "Failed to create SSL socket factory for secure MQTT connection: " + serverUri);
                }
                options.setSocketFactory(sslSocketFactory);
            }

            // Configure authentication
            if (mqttAuth) {
                options.setUserName(NotificationMqttTaskModule.MQTT_ADMIN_USERNAME);
                options.setPassword(mqttAdminPassword.toCharArray());
            }

            // Use serverUri directly (validated in constructor)
            client = new MqttClient(serverUri, "HMDMServer" + clientTag, persistence);
            client.connect(options);

            if (mqttDelay > 0) {
                throttledSender.setClient(client);
                taskRunner.submitTask(throttledSender);
            }
            log.info("MQTT client successfully connected to: {}", serverUri);
        } catch (Exception e) {
            log.error("Failed to initialize MQTT client for URI: {}. Error: {}", serverUri, e.getMessage(), e);
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
