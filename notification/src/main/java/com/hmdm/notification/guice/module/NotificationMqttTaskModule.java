package com.hmdm.notification.guice.module;

import com.google.inject.Inject;
import com.google.inject.name.Named;
import com.hmdm.notification.MqttUriUtil;
import com.hmdm.notification.PushSender;
import com.hmdm.util.CryptoUtil;
import org.apache.activemq.broker.BrokerPlugin;
import org.apache.activemq.broker.BrokerService;
import org.apache.activemq.filter.DestinationMapEntry;
import org.apache.activemq.security.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.LinkedList;
import java.util.List;

public class NotificationMqttTaskModule {

    private final MqttUriUtil.MqttUri mqttUri;
    private final String mqttExternal;
    private final boolean mqttAuth;
    private final String mqttAdminPassword;
    private final String sslKeystorePassword;
    private final String hashSecret;
    private final PushSender pushSender;
    private BrokerService brokerService;
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
        this.mqttUri = MqttUriUtil.parse(serverUri);
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
        if (mqttUri == null) {
            log.info("MQTT service not initialized (parameter mqtt.server.uri not set)");
            return false;
        }
        if (MqttUriUtil.isExternalEnabled(mqttExternal)) {
            log.info("MQTT service not started, use external MQTT server {}", mqttUri);
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
            if (mqttUri.isSecure()) {
                MqttUriUtil.configureSSL(mqttUri, sslKeystorePassword);
            }
            String brokerBindUri = mqttUri.toBrokerBindUri();
            // ActiveMQ requires explicit MQTT transport protocol for MQTT clients
            String mqttTransportUri;
            if (mqttUri.isSecure()) {
                // ssl:// or mqtts:// -> mqtt+nio+ssl://
                mqttTransportUri = brokerBindUri.replace("ssl://", "mqtt+nio+ssl://")
                                               .replace("mqtts://", "mqtt+nio+ssl://");
            } else {
                // tcp:// or mqtt:// -> mqtt+nio://
                mqttTransportUri = brokerBindUri.replace("tcp://", "mqtt+nio://")
                                               .replace("mqtt://", "mqtt+nio://");
            }
            log.info("Starting ActiveMQ MQTT transport on: {}", mqttTransportUri);
            brokerService.addConnector(mqttTransportUri);
            brokerService.start();
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

}
