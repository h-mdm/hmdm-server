package com.hmdm.notification.guice.module;

import com.google.inject.Inject;
import com.google.inject.name.Named;
import com.hmdm.notification.PushSender;
import com.hmdm.util.CryptoUtil;
import org.apache.activemq.broker.BrokerPlugin;
import org.apache.activemq.broker.BrokerService;
import org.apache.activemq.security.AuthenticationUser;
import org.apache.activemq.security.SimpleAuthenticationPlugin;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.LinkedList;
import java.util.List;

public class NotificationMqttTaskModule {

    private String serverUri;
    private String mqttExternal;
    private boolean mqttAuth;
    private String hashSecret;
    private BrokerService brokerService;
    private PushSender pushSender;
    private static final Logger log = LoggerFactory.getLogger(NotificationMqttTaskModule.class);
    public static final String MQTT_USERNAME = "hmdm";

    @Inject
    public NotificationMqttTaskModule(@Named("mqtt.server.uri") String serverUri,
                                      @Named("mqtt.external") String mqttExternal,
                                      @Named("mqtt.auth") boolean mqttAuth,
                                      @Named("hash.secret") String hashSecret,
                                      @Named("MQTT") PushSender pushSender) {
        this.serverUri = serverUri;
        this.mqttExternal = mqttExternal;
        this.pushSender = pushSender;
        this.mqttAuth = mqttAuth;
        this.hashSecret = hashSecret;
    }

    /**
     * <p>Creates the broker service</p>
     */
    public void init() {
        if (!initBrokerService()) {
            return;
        }
        pushSender.init();
    }

    private boolean initBrokerService() {
        if (serverUri == null || serverUri.equals("")) {
            log.info("MQTT service not initialized (parameter mqtt.server.uri not set)");
            return false;
        }
        if (mqttExternal.equals("1") || mqttExternal.toLowerCase().equals("true")) {
            log.info("MQTT service not started, use external MQTT server " + serverUri);
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
            List<AuthenticationUser> users = new LinkedList<>();
            users.add(user);
            authPlugin.setUsers(users);
            brokerService.setPlugins(new BrokerPlugin[]{authPlugin});
        }

        try {
            brokerService.addConnector("mqtt://" + serverUri);
            brokerService.start();
            log.info("MQTT notification service started at " + serverUri);
        } catch (Exception e) {
            log.error("Failed to create MQTT broker service");
            e.printStackTrace();
            return false;
        }
        return true;
    }

}
