package com.hmdm.notification.guice.module;

import com.google.inject.AbstractModule;
import com.google.inject.name.Names;
import org.apache.activemq.broker.BrokerService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.ServletContext;

public class NotificationMqttConfigModule extends AbstractModule {

    /**
     * <p>A context for module execution.</p>
     */
    private final ServletContext context;

    /**
     * <p>Constructs new <code>PhotoConfigModule</code> instance. This implementation does nothing.</p>
     */
    public NotificationMqttConfigModule(ServletContext context) {
        this.context = context;
    }

    /**
     * <p>Configures the environment.</p>
     */
    protected void configure() {
        String serverUri = context.getInitParameter("mqtt.server.uri");
        if (serverUri == null) {
            serverUri = "";
        }
        this.bindConstant().annotatedWith(Names.named("mqtt.server.uri")).to(serverUri);

        String mqttExternal = context.getInitParameter("mqtt.external");
        if (mqttExternal == null || "".equals(mqttExternal)) {
            mqttExternal = "0";
        }
        this.bindConstant().annotatedWith(Names.named("mqtt.external")).to(mqttExternal);

        String mqttClientTag = context.getInitParameter("mqtt.client.tag");
        if (mqttClientTag == null) {
            mqttClientTag = "";
        }
        this.bindConstant().annotatedWith(Names.named("mqtt.client.tag")).to(mqttClientTag);

        String mqttAuthTag = this.context.getInitParameter("mqtt.auth");
        this.bindConstant().annotatedWith(Names.named("mqtt.auth")).to(
                mqttAuthTag != null && (mqttAuthTag.equals("1") || mqttAuthTag.equalsIgnoreCase("true"))
        );

        String mqttAdminPassword = context.getInitParameter("mqtt.admin.password");
        if (mqttAdminPassword == null) {
            mqttAdminPassword = "dd3V5YDkrX";
        }
        this.bindConstant().annotatedWith(Names.named("mqtt.admin.password")).to(mqttAdminPassword);

        String mqttDelayTag = this.context.getInitParameter("mqtt.message.delay");
        long mqttDelay = 0;
        try {
            if (mqttDelayTag != null) {
                mqttDelay = Long.parseLong(mqttDelayTag);
            }
        } catch (NumberFormatException e) {
            e.printStackTrace();
        }
        this.bindConstant().annotatedWith(Names.named("mqtt.message.delay")).to(mqttDelay);

        String pollTimeoutTag = this.context.getInitParameter("polling.timeout");
        long pollTimeout = 60;
        try {
            if (pollTimeoutTag != null) {
                pollTimeout = Long.parseLong(pollTimeoutTag);
            }
        } catch (NumberFormatException e) {
            e.printStackTrace();
        }
        this.bindConstant().annotatedWith(Names.named("polling.timeout")).to(pollTimeout);

    }
}
