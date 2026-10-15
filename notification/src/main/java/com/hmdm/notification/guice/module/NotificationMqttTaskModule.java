package com.hmdm.notification.guice.module;

import com.hmdm.notification.PushSender;
import com.hmdm.util.CryptoUtil;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import jakarta.inject.Singleton;
import java.io.IOException;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
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

/**
 * MQTT notification task module using ActiveMQ Artemis. Provides embedded MQTT broker functionality for push notifications.
 *
 * <p>A single broker belongs to each application injector. {@code Initializer} retains this instance for {@link #shutdown()} when the servlet context
 * is destroyed.</p>
 */
@Singleton
public class NotificationMqttTaskModule {

    private String serverUri;
    private String mqttExternal;
    private boolean mqttAuth;
    private String mqttAdminPassword;
    private String sslPemKeyPath;
    private String sslPemCertPath;
    private String sslPemConfigPath;
    private String hashSecret;
    private EmbeddedActiveMQ embeddedBroker;
    private PushSender pushSender;
    private static final Logger log = LoggerFactory.getLogger(NotificationMqttTaskModule.class);
    public static final String MQTT_USERNAME = "hmdm";
    public static final String MQTT_ADMIN_USERNAME = "admin";

    @Inject
    public NotificationMqttTaskModule(
            @Named("mqtt.server.uri") String serverUri,
            @Named("mqtt.external") String mqttExternal,
            @Named("mqtt.auth") boolean mqttAuth,
            @Named("mqtt.admin.password") String mqttAdminPassword,
            @Named("ssl.pem.key.path") String sslPemKeyPath,
            @Named("ssl.pem.cert.path") String sslPemCertPath,
            @Named("ssl.pem.config.path") String sslPemConfigPath,
            @Named("hash.secret") String hashSecret,
            @Named("MQTT") PushSender pushSender) {
        this.serverUri = serverUri;
        this.mqttExternal = mqttExternal;
        this.pushSender = pushSender;
        this.mqttAuth = mqttAuth;
        this.mqttAdminPassword = mqttAdminPassword;
        this.sslPemKeyPath = sslPemKeyPath;
        this.sslPemCertPath = sslPemCertPath;
        this.sslPemConfigPath = sslPemConfigPath;
        this.hashSecret = hashSecret;
    }

    /**
     * Validates PEM cert config when SSL is requested. Fail-closed: throws {@link IllegalStateException} if paths are missing or half-configured, so
     * startup aborts rather than silently using stale or missing certificate material.
     */
    static void requirePemPaths(String pemKeyPath, String pemCertPath) {
        boolean keySet = isConfigured(pemKeyPath);
        boolean certSet = isConfigured(pemCertPath);
        if (keySet && certSet) {
            return;
        }
        if (keySet != certSet) {
            throw new IllegalStateException("ssl.pem.key.path and ssl.pem.cert.path must be set together");
        }
        throw new IllegalStateException(
                "MQTT SSL requested but ssl.pem.key.path and ssl.pem.cert.path are not configured");
    }

    /**
     * A config value counts as "set" only if it is non-empty and is not an unresolved Maven filter placeholder (e.g. a literal "${ssl.pem.key.path}"
     * left in a filtered context.xml when build.properties omits the key). Treating placeholders as unset stops a filtered context.xml from
     * accidentally selecting PEM mode.
     */
    static boolean isConfigured(String v) {
        return v != null && !v.isEmpty() && !(v.startsWith("${") && v.endsWith("}"));
    }

    /**
     * The URI schemes that select TLS for the embedded broker.
     *
     * <p>Three schemes, and {@code wss} is one of them. The upstream change this module was ported from narrowed the list to {@code ssl} and
     * {@code mqtts}; that narrowing is unrelated to the PEM migration and is not taken here, because no deployment outside this repository can be
     * shown not to use {@code wss://}. Extracted into its own method so the choice is covered by a test rather than resting on a comment.</p>
     */
    public static boolean isSslScheme(URI uri) {
        String scheme = uri.getScheme();
        return "ssl".equals(scheme) || "mqtts".equals(scheme) || "wss".equals(scheme);
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
        if (serverUri == null || serverUri.isEmpty()) {
            log.info("MQTT service not initialized (parameter mqtt.server.uri not set)");
            return false;
        }
        if ("1".equals(mqttExternal) || "true".equalsIgnoreCase(mqttExternal)) {
            log.info("MQTT service not started, use external MQTT server {}", serverUri);
            return true;
        }

        // Tracked outside the try so the catch can enforce fail-closed when SSL was requested.
        boolean useSSL = false;
        try {
            // Parse protocol, host and port using URI for IPv6 safety
            URI uri = parseServerUri(serverUri);
            // Three schemes, deliberately: "wss" is retained. Narrowing this list is unrelated to the
            // PEM migration, and no deployment outside this repository can be proven not to use wss://.
            useSSL = isSslScheme(uri);
            int port = uri.getPort() > 0 ? uri.getPort() : (useSSL ? 8883 : 1883);

            // Create Artemis configuration programmatically
            Configuration config = new ConfigurationImpl();
            config.setPersistenceEnabled(false);
            config.setJMXManagementEnabled(false);
            config.setSecurityEnabled(mqttAuth);

            // Configure MQTT acceptor - always bind to all interfaces
            Map<String, Object> acceptorParams = new HashMap<>();
            acceptorParams.put("host", "0.0.0.0");
            acceptorParams.put("port", port);
            acceptorParams.put("protocols", "MQTT");

            // Configure SSL if requested. Fail-closed: config errors throw IllegalStateException,
            // which propagates (see catch below) and aborts startup rather than silently skipping the broker.
            if (useSSL) {
                requirePemPaths(sslPemKeyPath, sslPemCertPath);
                requireReadable(sslPemKeyPath, "ssl.pem.key.path");
                requireReadable(sslPemCertPath, "ssl.pem.cert.path");
                String pemCfgPath = !isConfigured(sslPemConfigPath)
                        ? System.getProperty("java.io.tmpdir") + "/hmdm-mqtt-broker.pemcfg"
                        : sslPemConfigPath;
                writePemCfg(pemCfgPath, sslPemKeyPath, sslPemCertPath);

                acceptorParams.put("sslEnabled", true);
                acceptorParams.put("keyStoreType", "PEMCFG");
                acceptorParams.put("keyStorePath", pemCfgPath);
                acceptorParams.put("sslAutoReload", true);

                log.info("Configuring MQTT broker SSL in PEM mode (auto-reload enabled), pemcfg: {}", pemCfgPath);
            }

            TransportConfiguration mqttAcceptor = new TransportConfiguration(
                    NettyAcceptorFactory.class.getName(), acceptorParams, useSSL ? "mqtt-ssl" : "mqtt");
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
                    public String validateUserAndRole(
                            String user,
                            String password,
                            Set<Role> roles,
                            CheckType checkType,
                            String address,
                            RemotingConnection connection) {
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
                    public boolean validateUserAndRole(
                            String user, String password, Set<Role> roles, CheckType checkType) {
                        return validateUserAndRole(user, password, roles, checkType, null, null) != null;
                    }
                });
            }

            embeddedBroker.start();
            log.info("Artemis MQTT notification service started at {}", serverUri);

        } catch (IllegalStateException e) {
            // SSL/config error (bad or half-configured SSL, unreadable cert material, pemcfg write
            // failure): fail closed — propagate so the servlet context fails to start rather than
            // silently skipping the broker.
            throw e;
        } catch (Exception e) {
            if (useSSL) {
                // SSL was requested but the broker failed to start (e.g. Artemis rejected a readable
                // but invalid PEM). Fail closed — abort startup rather than silently running no TLS.
                throw new IllegalStateException(
                        "MQTT SSL broker failed to start (SSL requested but cert/config rejected): " + e.getMessage(),
                        e);
            }
            log.error("Failed to create Artemis MQTT broker service: {}", e.getMessage(), e);
            return false;
        }
        return true;
    }

    /** Fail-closed guard: the configured cert material must exist and be readable, else abort startup. */
    private static void requireReadable(String path, String what) {
        if (path == null || path.isEmpty() || !Files.isReadable(Path.of(path))) {
            throw new IllegalStateException("MQTT SSL requested but " + what + " is missing or unreadable: " + path);
        }
    }

    /**
     * Writes the Artemis PEMCFG file referencing the certbot PEM key + cert chain. The temp file is created in the target directory (same filesystem)
     * then atomically moved into place; a write failure is fatal ({@link IllegalStateException}) so startup aborts rather than serving no or stale
     * TLS.
     *
     * <p>Note this needs write permission on the CONTAINING DIRECTORY, not merely on the file, which is why the pemcfg is configured to live under a
     * Tomcat-owned runtime directory rather than beside the root-owned keys.</p>
     */
    static void writePemCfg(String pemCfgPath, String keyPath, String certPath) {
        try {
            Path target = Path.of(pemCfgPath);
            Path dir = target.toAbsolutePath().getParent();
            Files.createDirectories(dir);
            Path tmp = Files.createTempFile(dir, "hmdm-mqtt", ".pemcfg");
            Files.writeString(tmp, "source.key=" + keyPath + "\nsource.cert=" + certPath + "\n");
            Files.move(tmp, target, StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.ATOMIC_MOVE);
        } catch (IOException e) {
            throw new IllegalStateException("Failed to write MQTT pemcfg at " + pemCfgPath, e);
        }
    }

    /**
     * Parses the server URI, handling IPv6 addresses safely. Normalizes URIs without a scheme by prepending "mqtt://".
     */
    public static URI parseServerUri(String serverUri) {
        String normalized = serverUri;
        if (!normalized.contains("://")) {
            normalized = "mqtt://" + normalized;
        }
        URI uri = URI.create(normalized);
        if (uri.getHost() == null) {
            throw new IllegalArgumentException("Invalid MQTT server URI (no host): " + serverUri);
        }
        return uri;
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
                log.error("Error stopping Artemis broker: {}", e.getMessage(), e);
            }
        }
    }
}
