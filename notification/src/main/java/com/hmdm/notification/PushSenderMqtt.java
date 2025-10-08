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

import javax.jms.*;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManagerFactory;
import java.io.FileInputStream;
import java.net.URI;
import java.security.KeyStore;
import java.util.HashMap;
import java.util.Map;

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
    private final MessageClassifier messageClassifier = new MessageClassifier();
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
                          @Named("mqtt.ssl.keystore.password") String sslKeystorePassword,
                          @Named("mqtt.ssl.protocols") String sslProtocols,
                          MqttThrottledSender throttledSender,
                          BackgroundTaskRunnerService taskRunner,
                          UnsecureDAO unsecureDAO) {
        this.serverUri = serverUri;
        this.clientTag = clientTag;
        this.mqttAuth = mqttAuth;
        this.mqttAdminPassword = mqttAdminPassword;
        this.mqttExternal = mqttExternal;
        this.mqttDelay = mqttDelay;
        this.sslKeystorePassword = sslKeystorePassword;
        this.sslProtocols = sslProtocols;
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

            // Apply defaults and build the final URI
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
            // Reconstruct the URI with defaults applied
            this.serverUri = scheme + "://" + host + ":" + port;

        } catch (Exception e) {
            if (e instanceof IllegalArgumentException) {
                throw e; // Re-throw our validation exceptions
            }
            throw new IllegalArgumentException("Invalid MQTT server URI format: " + serverUri +
                ". Expected format: protocol://host:port (e.g., tcp://localhost:1883, ssl://mqtt.example.com:8883)", e);
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

            // Parse and validate TLS protocols
            String[] requestedProtocols = sslProtocols.split(",");
            for (int i = 0; i < requestedProtocols.length; i++) {
                requestedProtocols[i] = requestedProtocols[i].trim();
            }

            // Determine SSL context protocol (use first specified or "TLS")
            String contextProtocol = requestedProtocols[0];
            if ("TLS".equals(contextProtocol)) {
                // Use default TLS (let JVM decide the best version)
                contextProtocol = "TLS";
            }

            SSLContext sslContext = SSLContext.getInstance(contextProtocol);
            sslContext.init(null, tmf.getTrustManagers(), null);

            SSLSocketFactory socketFactory = sslContext.getSocketFactory();

            // Apply best practices: Create wrapper to configure enabled protocols
            return new ConfiguredSSLSocketFactory(socketFactory, requestedProtocols);

        } catch (Exception e) {
            // Provide clear error message for SSL configuration issues
            if (e instanceof IllegalStateException) {
                throw e; // Re-throw our SSL validation exceptions
            }
            throw new IllegalStateException("Failed to create SSL socket factory for MQTT connection: " +
                serverUri + ". " + e.getMessage(), e);
        }
    }

    // Inner class to configure SSL socket protocols
    private static class ConfiguredSSLSocketFactory extends javax.net.ssl.SSLSocketFactory {
        private final SSLSocketFactory delegate;
        private final String[] enabledProtocols;

        public ConfiguredSSLSocketFactory(SSLSocketFactory delegate, String[] enabledProtocols) {
            this.delegate = delegate;
            this.enabledProtocols = enabledProtocols.clone();
        }

        @Override
        public java.net.Socket createSocket() throws java.io.IOException {
            javax.net.ssl.SSLSocket socket = (javax.net.ssl.SSLSocket) delegate.createSocket();
            configureSocket(socket);
            return socket;
        }

        @Override
        public java.net.Socket createSocket(String host, int port) throws java.io.IOException {
            javax.net.ssl.SSLSocket socket = (javax.net.ssl.SSLSocket) delegate.createSocket(host, port);
            configureSocket(socket);
            return socket;
        }

        @Override
        public java.net.Socket createSocket(String host, int port, java.net.InetAddress localHost, int localPort) throws java.io.IOException {
            javax.net.ssl.SSLSocket socket = (javax.net.ssl.SSLSocket) delegate.createSocket(host, port, localHost, localPort);
            configureSocket(socket);
            return socket;
        }

        @Override
        public java.net.Socket createSocket(java.net.InetAddress host, int port) throws java.io.IOException {
            javax.net.ssl.SSLSocket socket = (javax.net.ssl.SSLSocket) delegate.createSocket(host, port);
            configureSocket(socket);
            return socket;
        }

        @Override
        public java.net.Socket createSocket(java.net.InetAddress address, int port, java.net.InetAddress localAddress, int localPort) throws java.io.IOException {
            javax.net.ssl.SSLSocket socket = (javax.net.ssl.SSLSocket) delegate.createSocket(address, port, localAddress, localPort);
            configureSocket(socket);
            return socket;
        }

        @Override
        public java.net.Socket createSocket(java.net.Socket s, String host, int port, boolean autoClose) throws java.io.IOException {
            javax.net.ssl.SSLSocket socket = (javax.net.ssl.SSLSocket) delegate.createSocket(s, host, port, autoClose);
            configureSocket(socket);
            return socket;
        }

        @Override
        public String[] getDefaultCipherSuites() {
            return delegate.getDefaultCipherSuites();
        }

        @Override
        public String[] getSupportedCipherSuites() {
            return delegate.getSupportedCipherSuites();
        }

        private void configureSocket(javax.net.ssl.SSLSocket socket) {
            // Apply TLS protocol configuration - only enable requested protocols if not "TLS"
            if (!enabledProtocols[0].equals("TLS")) {
                // Validate requested protocols against supported ones
                String[] supportedProtocols = socket.getSupportedProtocols();
                java.util.List<String> validProtocols = new java.util.ArrayList<>();

                for (String requested : enabledProtocols) {
                    boolean supported = false;
                    for (String supported_protocol : supportedProtocols) {
                        if (supported_protocol.equals(requested)) {
                            validProtocols.add(requested);
                            supported = true;
                            break;
                        }
                    }
                    if (!supported) {
                        System.err.println("Warning: TLS protocol " + requested + " is not supported by JVM. Supported: " + java.util.Arrays.toString(supportedProtocols));
                    }
                }

                if (!validProtocols.isEmpty()) {
                    socket.setEnabledProtocols(validProtocols.toArray(new String[0]));
                }
            }
            // If "TLS", let JVM use default protocols (best practice)
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
                    throw new IllegalStateException("Failed to create SSL socket factory for secure MQTT connection: " + serverUri);
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

            client = new MqttClient(mqttUri.toString(), "HMDMServer" + clientTag, persistence);
            client.connect(options);
            if (mqttDelay > 0) {
                throttledSender.setClient(client);
                taskRunner.submitTask(throttledSender);
            }

            // Log successful connection
            System.out.println("MQTT client successfully connected to: " + serverUri);

        } catch (Exception e) {
            System.err.println("Failed to initialize MQTT client for URI: " + serverUri + ". Error: " + e.getMessage());
            e.printStackTrace();
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
                // Create prioritized envelope for adaptive throttling
                MessagePriority priority = messageClassifier.classify(message);
                PrioritizedMqttEnvelope envelope = new PrioritizedMqttEnvelope(number, mqttMessage, priority);
                throttledSender.send(envelope);
            }

        } catch (Exception e) {
            log.error("Failed to send MQTT message to device {}: {}", message.getDeviceId(), e.getMessage(), e);
        }
        return 0;
    }
}
