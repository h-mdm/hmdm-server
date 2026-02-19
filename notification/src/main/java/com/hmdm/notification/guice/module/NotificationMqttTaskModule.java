package com.hmdm.notification.guice.module;

import com.google.inject.Inject;
import jakarta.inject.Named;
import com.hmdm.notification.PushSender;
import com.hmdm.util.CryptoUtil;
import org.apache.activemq.artemis.api.core.TransportConfiguration;
import org.apache.activemq.artemis.core.config.Configuration;
import org.apache.activemq.artemis.core.config.impl.ConfigurationImpl;
import org.apache.activemq.artemis.core.remoting.impl.netty.NettyAcceptorFactory;
import org.apache.activemq.artemis.core.security.CheckType;
import org.apache.activemq.artemis.core.security.Role;
import org.apache.activemq.artemis.core.server.embedded.EmbeddedActiveMQ;
import org.apache.activemq.artemis.core.settings.impl.AddressSettings;
import org.apache.activemq.artemis.spi.core.protocol.RemotingConnection;
import org.apache.activemq.artemis.spi.core.security.ActiveMQSecurityManager3;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * MQTT notification task module using ActiveMQ Artemis.
 * Provides embedded MQTT broker functionality for push notifications.
 */
public class NotificationMqttTaskModule {

    private String serverUri;
    private String mqttExternal;
    private boolean mqttAuth;
    private String mqttAdminPassword;
    private String sslKeystorePassword;
    private String hashSecret;
    private EmbeddedActiveMQ embeddedBroker;
    private PushSender pushSender;
    private static final Logger log = LoggerFactory.getLogger(NotificationMqttTaskModule.class);
    public static final String MQTT_USERNAME = "hmdm";
    public static final String MQTT_ADMIN_USERNAME = "admin";

    @Inject
    public NotificationMqttTaskModule(@Named("mqtt.server.uri") String serverUri,
            @Named("mqtt.external") String mqttExternal,
            @Named("mqtt.auth") boolean mqttAuth,
            @Named("mqtt.admin.password") String mqttAdminPassword,
            @Named("ssl.keystore.password") String sslKeystorePassword,
            @Named("hash.secret") String hashSecret,
            @Named("MQTT") PushSender pushSender) {
        this.serverUri = serverUri;
        this.mqttExternal = mqttExternal;
        this.pushSender = pushSender;
        this.mqttAuth = mqttAuth;
        this.mqttAdminPassword = mqttAdminPassword;
        this.sslKeystorePassword = sslKeystorePassword;
        this.hashSecret = hashSecret;
    }

    /**
     * <p>
     * Creates the broker service
     * </p>
     */
    public void init() {
        if (!initBrokerService()) {
            return;
        }
        pushSender.init();
    }

    private boolean initBrokerService() {
        if (serverUri == null || serverUri.isEmpty()) {
            log.info("MQTT service not initialized (parameter mqtt.server.uri not set)");
            return false;
        }
        if ("1".equals(mqttExternal) || "true".equalsIgnoreCase(mqttExternal)) {
            log.info("MQTT service not started, use external MQTT server " + serverUri);
            return true;
        }

        try {
            // Parse protocol and host:port from serverUri
            boolean useSSL = false;
            String hostPort = serverUri;

            if (hostPort.contains("://")) {
                String scheme = hostPort.substring(0, hostPort.indexOf("://")).toLowerCase();
                hostPort = hostPort.substring(hostPort.indexOf("://") + 3);

                // Detect SSL/TLS protocols
                if (scheme.equals("ssl") || scheme.equals("mqtts") || scheme.equals("wss")) {
                    useSSL = true;
                }
            }

            String host = "0.0.0.0";
            int port = 1883;
            if (hostPort.contains(":")) {
                String[] parts = hostPort.split(":");
                host = parts[0];
                port = Integer.parseInt(parts[1]);
            } else {
                host = hostPort;
            }
            // Extract domain name for keystore path
            String domain = host;

            // Create Artemis configuration programmatically
            Configuration config = new ConfigurationImpl();
            config.setPersistenceEnabled(false);
            config.setJMXManagementEnabled(false);
            config.setSecurityEnabled(mqttAuth);

            // Configure MQTT acceptor
            Map<String, Object> acceptorParams = new HashMap<>();
            acceptorParams.put("host", host);
            acceptorParams.put("port", port);
            acceptorParams.put("protocols", "MQTT");

            // Configure SSL if requested
            if (useSSL) {
                if (sslKeystorePassword == null || sslKeystorePassword.isEmpty()) {
                    throw new RuntimeException("SSL protocol requested but ssl.keystore.password is not configured.");
                }
                String keystorePath = "/usr/local/tomcat/ssl/" + domain + ".jks";

                acceptorParams.put("sslEnabled", true);
                acceptorParams.put("keyStorePath", keystorePath);
                acceptorParams.put("keyStorePassword", sslKeystorePassword);
                acceptorParams.put("trustStorePath", keystorePath);
                acceptorParams.put("trustStorePassword", sslKeystorePassword);

                log.info("Configuring SSL for MQTT broker with keystore: " + keystorePath);
            }

            TransportConfiguration mqttAcceptor = new TransportConfiguration(
                    NettyAcceptorFactory.class.getName(),
                    acceptorParams,
                    useSSL ? "mqtt-ssl" : "mqtt");
            config.addAcceptorConfiguration(mqttAcceptor);

            // Configure address settings for MQTT topics
            AddressSettings addressSettings = new AddressSettings();
            addressSettings.setAutoCreateAddresses(true);
            addressSettings.setAutoCreateQueues(true);
            config.addAddressSetting("#", addressSettings);

            // Create and start embedded broker
            embeddedBroker = new EmbeddedActiveMQ();
            embeddedBroker.setConfiguration(config);

            // Configure security if authentication is enabled
            if (mqttAuth) {
                final String userPassword = CryptoUtil.getSHA1String(MQTT_USERNAME + hashSecret);
                final String adminPassword = this.mqttAdminPassword;

                embeddedBroker.setSecurityManager(new ActiveMQSecurityManager3() {
                    @Override
                    public String validateUser(String user, String password, RemotingConnection connection) {
                        if (MQTT_USERNAME.equals(user) && userPassword.equals(password)) {
                            return user;
                        }
                        if (MQTT_ADMIN_USERNAME.equals(user) && adminPassword.equals(password)) {
                            return user;
                        }
                        return null;
                    }

                    @Override
                    public String validateUserAndRole(String user, String password, Set<Role> roles,
                            CheckType checkType, String address, RemotingConnection connection) {
                        // First validate user credentials
                        String validatedUser = validateUser(user, password, connection);
                        if (validatedUser == null) {
                            return null;
                        }
                        // Admin can do anything
                        if (MQTT_ADMIN_USERNAME.equals(user)) {
                            return user;
                        }
                        // Regular users can read and admin (subscribe), but not write (publish)
                        if (MQTT_USERNAME.equals(user)) {
                            return checkType != CheckType.SEND ? user : null;
                        }
                        return null;
                    }

                    @Override
                    public boolean validateUser(String user, String password) {
                        return validateUser(user, password, null) != null;
                    }

                    @Override
                    public boolean validateUserAndRole(String user, String password, Set<Role> roles,
                            CheckType checkType) {
                        return validateUserAndRole(user, password, roles, checkType, null, null) != null;
                    }
                });
            }

            embeddedBroker.start();
            log.info("Artemis MQTT notification service started at " + serverUri);

        } catch (Exception e) {
            log.error("Failed to create Artemis MQTT broker service: " + e.getMessage(), e);
            return false;
        }
        return true;
    }

    /**
     * Stops the embedded broker on shutdown.
     */
    public void shutdown() {
        if (embeddedBroker != null) {
            try {
                embeddedBroker.stop();
                log.info("Artemis MQTT notification service stopped");
            } catch (Exception e) {
                log.error("Error stopping Artemis broker: " + e.getMessage(), e);
            }
        }
    }
}
