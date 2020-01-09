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
    }
}
