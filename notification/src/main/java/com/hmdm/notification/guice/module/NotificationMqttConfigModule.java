package com.hmdm.notification.guice.module;

import com.google.inject.AbstractModule;
import com.google.inject.name.Names;

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
                mqttAuthTag != null && (mqttAuthTag.equals("1") || mqttAuthTag.equalsIgnoreCase("true")));

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

        String sslKeystorePassword = context.getInitParameter("mqtt.ssl.keystore.password");
        if (sslKeystorePassword == null) {
            sslKeystorePassword = "123456"; // Default fallback matching letsencrypt-ssl.sh
        }
        this.bindConstant().annotatedWith(Names.named("mqtt.ssl.keystore.password")).to(sslKeystorePassword);

        String sslProtocols = context.getInitParameter("mqtt.ssl.protocols");
        if (sslProtocols == null || sslProtocols.trim().isEmpty()) {
            sslProtocols = "TLS"; // Default to JVM-decided TLS version
        }
        this.bindConstant().annotatedWith(Names.named("mqtt.ssl.protocols")).to(sslProtocols);

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

        // Adaptive throttling configuration (backward compatible)
        String adaptiveEnabledTag = this.context.getInitParameter("mqtt.adaptive.enabled");
        boolean adaptiveEnabled = adaptiveEnabledTag != null &&
                (adaptiveEnabledTag.equals("1") || adaptiveEnabledTag.equalsIgnoreCase("true"));
        this.bindConstant().annotatedWith(Names.named("mqtt.adaptive.enabled")).to(adaptiveEnabled);

        String adaptiveLightThresholdTag = this.context.getInitParameter("mqtt.adaptive.light.threshold");
        int adaptiveLightThreshold = 3; // Default: instant for ≤3 messages
        try {
            if (adaptiveLightThresholdTag != null) {
                adaptiveLightThreshold = Integer.parseInt(adaptiveLightThresholdTag);
            }
        } catch (NumberFormatException e) {
            e.printStackTrace();
        }
        this.bindConstant().annotatedWith(Names.named("mqtt.adaptive.light.threshold")).to(adaptiveLightThreshold);

        String adaptiveMediumThresholdTag = this.context.getInitParameter("mqtt.adaptive.medium.threshold");
        int adaptiveMediumThreshold = 15; // Default: fast for ≤15 messages
        try {
            if (adaptiveMediumThresholdTag != null) {
                adaptiveMediumThreshold = Integer.parseInt(adaptiveMediumThresholdTag);
            }
        } catch (NumberFormatException e) {
            e.printStackTrace();
        }
        this.bindConstant().annotatedWith(Names.named("mqtt.adaptive.medium.threshold")).to(adaptiveMediumThreshold);

        String adaptiveHeavyThresholdTag = this.context.getInitParameter("mqtt.adaptive.heavy.threshold");
        int adaptiveHeavyThreshold = 50; // Default: normal for ≤50 messages
        try {
            if (adaptiveHeavyThresholdTag != null) {
                adaptiveHeavyThreshold = Integer.parseInt(adaptiveHeavyThresholdTag);
            }
        } catch (NumberFormatException e) {
            e.printStackTrace();
        }
        this.bindConstant().annotatedWith(Names.named("mqtt.adaptive.heavy.threshold")).to(adaptiveHeavyThreshold);

    }
}
