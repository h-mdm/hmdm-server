package com.hmdm.notification.guice.module;

import com.google.inject.Inject;
import com.google.inject.name.Named;
import com.hmdm.notification.PushSender;
import com.hmdm.notification.PushSenderMqtt;
import org.apache.activemq.broker.BrokerService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class NotificationMqttTaskModule {

    private String serverUri;
    private BrokerService brokerService;
    private PushSender pushSender;
    private static final Logger log = LoggerFactory.getLogger(NotificationMqttTaskModule.class);

    @Inject
    public NotificationMqttTaskModule(@Named("mqtt.server.uri") String serverUri, @Named("MQTT") PushSender pushSender) {
        this.serverUri = serverUri;
        this.pushSender = pushSender;
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
        brokerService = new BrokerService();
        brokerService.setPersistent(false);
        brokerService.setUseJmx(false);
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
