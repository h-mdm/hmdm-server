package com.hmdm.notification.guice.module;

import com.google.inject.Inject;
import com.google.inject.name.Named;
import com.hmdm.notification.PushSender;
import com.hmdm.util.CryptoUtil;
import org.apache.activemq.broker.BrokerPlugin;
import org.apache.activemq.broker.BrokerService;
import org.apache.activemq.filter.DestinationMapEntry;
import org.apache.activemq.security.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.LinkedList;
import java.util.List;

public class NotificationMqttTaskModule {

    private String serverUri;
    private String mqttExternal;
    private boolean mqttAuth;
    private String mqttAdminPassword;
    private String sslKeystorePassword;
    private String sslProtocols;
    private String hashSecret;
    private BrokerService brokerService;
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
        if (mqttExternal != null && (mqttExternal.equals("1") || mqttExternal.toLowerCase().equals("true"))) {
            log.info("MQTT service not started, use external MQTT server {}", serverUri);
            return true;
        }

        brokerService = new BrokerService();
        brokerService.setPersistent(false);
        brokerService.setUseJmx(false);

        if (mqttAuth) {
            SimpleAuthenticationPlugin authPlugin = new SimpleAuthenticationPlugin();
            authPlugin.setAnonymousAccessAllowed(false);
            AuthenticationUser user = new AuthenticationUser(MQTT_USERNAME,
                    CryptoUtil.getSHA1String(MQTT_USERNAME + hashSecret), "users");
            AuthenticationUser admin = new AuthenticationUser(MQTT_ADMIN_USERNAME, mqttAdminPassword, "admins");
            List<AuthenticationUser> users = new LinkedList<>();
            users.add(user);
            users.add(admin);
            authPlugin.setUsers(users);

            AuthorizationPlugin authorizationPlugin = new AuthorizationPlugin();
            try {
                List<DestinationMapEntry> entries = new LinkedList<>();

                AuthorizationEntry authorizationEntry = new AuthorizationEntry();
                authorizationEntry.setTopic(">");
                authorizationEntry.setRead("users,admins");
                authorizationEntry.setWrite("admins");
                authorizationEntry.setAdmin("users,admins");
                entries.add(authorizationEntry);

                authorizationEntry = new AuthorizationEntry();
                authorizationEntry.setTopic("ActiveMQ.Advisory.>");
                authorizationEntry.setRead("users,admins");
                authorizationEntry.setWrite("users,admins");
                authorizationEntry.setAdmin("users,admins");
                entries.add(authorizationEntry);

                AuthorizationMap authorizationMap = new DefaultAuthorizationMap(entries);
                authorizationPlugin.setMap(authorizationMap);
            } catch (Exception e) {
                log.error("Failed to configure MQTT authorization", e);
            }
            brokerService.setPlugins(new BrokerPlugin[] { authPlugin, authorizationPlugin });
        }

        try {
            URI uri = new URI(serverUri);
            String scheme = uri.getScheme();
            String host = uri.getHost();
            if (!scheme.equals("tcp") && !scheme.equals("ssl") &&
                    !scheme.equals("mqtt") && !scheme.equals("mqtts")) {
                throw new IllegalArgumentException("Invalid MQTT protocol scheme: " + scheme +
                        ". Supported protocols: tcp://, ssl://, mqtt://, mqtts://");
            } else if (scheme.equals("ssl") || scheme.equals("mqtts")) {
                configureSSLContext(host);
            }
            brokerService.addConnector(serverUri);
            brokerService.start();
            log.info("MQTT notification service started at {}", serverUri);
        } catch (URISyntaxException e) {
            log.error("Invalid MQTT server URI format: {}", serverUri, e);
            return false;
        } catch (Exception e) {
            log.error("Failed to create MQTT broker service", e);
            return false;
        }
        return true;
    }

    public void destroy() {
        if (brokerService != null) {
            try {
                if (brokerService.isStarted()) {
                    brokerService.stop();
                    log.info("MQTT broker service stopped successfully");
                }
            } catch (Exception e) {
                log.error("Failed to stop MQTT broker service", e);
            }
        }
    }

    private void configureSSLContext(String domain) throws Exception {
        try {
            String tomcatHome = System.getProperty("catalina.home");
            if (tomcatHome == null) {
                tomcatHome = "/var/lib/tomcat9"; // Default fallback
            }

            String keystorePath = tomcatHome + "/ssl/" + domain + ".jks";
            java.io.File keystoreFile = new java.io.File(keystorePath);
            if (!keystoreFile.exists()) {
                throw new IllegalStateException("SSL keystore not found at: " + keystorePath +
                        ". Ensure SSL certificates are properly configured for domain: " + domain);
            }
            if (!keystoreFile.canRead()) {
                throw new IllegalStateException("Cannot read SSL keystore at: " + keystorePath +
                        ". Check file permissions.");
            }
            System.setProperty("javax.net.ssl.keyStore", keystorePath);
            System.setProperty("javax.net.ssl.keyStorePassword", sslKeystorePassword);
            System.setProperty("javax.net.ssl.trustStore", keystorePath);
            System.setProperty("javax.net.ssl.trustStorePassword", sslKeystorePassword);
            if (sslProtocols != null && !sslProtocols.trim().isEmpty() && !sslProtocols.equals("TLS")) {
                System.setProperty("https.protocols", sslProtocols);
            }
        } catch (Exception e) {
            throw new IllegalStateException("Failed to configure SSL context for embedded broker: " +
                    serverUri + ". " + e.getMessage(), e);
        }
    }
}
