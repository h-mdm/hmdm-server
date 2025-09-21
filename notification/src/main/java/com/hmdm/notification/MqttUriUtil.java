package com.hmdm.notification;

import java.net.URI;
import java.net.URISyntaxException;
import java.io.FileInputStream;
import java.security.KeyStore;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManagerFactory;

/**
 * Utility class for MQTT URI validation and transformation.
 * Provides consistent URI handling for both MQTT broker and client implementations.
 */
public final class MqttUriUtil {

    private MqttUriUtil() {
        // Utility class - prevent instantiation
    }

    /**
     * Parsed and validated MQTT URI with transformation capabilities.
     */
    public static final class MqttUri {
        private final String scheme;
        private final String host;
        private final int port;
        private final String originalUri;

        private MqttUri(String scheme, String host, int port, String originalUri) {
            this.scheme = scheme;
            this.host = host;
            this.port = port;
            this.originalUri = originalUri;
        }

        public String getScheme() { return scheme; }
        public String getHost() { return host; }
        public int getPort() { return port; }
        public String getOriginalUri() { return originalUri; }

        /**
         * Generate URI for broker binding (binds to all interfaces).
         * Used by embedded MQTT broker to accept connections from any interface.
         */
        public String toBrokerBindUri() {
            return scheme + "://0.0.0.0:" + port;
        }

        /**
         * Check if this URI requires SSL/TLS configuration.
         */
        public boolean isSecure() {
            return "ssl".equals(scheme) || "mqtts".equals(scheme);
        }

        @Override
        public String toString() {
            return originalUri;
        }
    }

    /**
     * Parse and validate MQTT URI.
     * @param serverUri the MQTT server URI to parse
     * @return validated MqttUri instance
     * @throws IllegalArgumentException if URI is invalid
     */
    public static MqttUri parse(String serverUri) {
        if (serverUri == null || serverUri.trim().isEmpty()) {
            throw new IllegalArgumentException("MQTT server URI cannot be null or empty");
        }

        try {
            String normalizedUri = serverUri.trim();

            // If no scheme is present, prepend tcp://
            if (!normalizedUri.contains("://")) {
                normalizedUri = "tcp://" + normalizedUri;
            }

            URI uri = new URI(normalizedUri);
            String scheme = uri.getScheme();
            String host = uri.getHost();
            int port = uri.getPort();

            // Validate scheme
            if (!isValidScheme(scheme)) {
                throw new IllegalArgumentException("Invalid MQTT protocol scheme: " + scheme +
                        ". Supported protocols: tcp://, ssl://, mqtt://, mqtts://");
            }

            // Validate and normalize host
            if (host == null || host.trim().isEmpty()) {
                host = "localhost";
            }

            // Validate and set default port
            if (port <= 0 || port > 65535) {
                throw new IllegalArgumentException("MQTT server port must be between 1 and 65535, got: " + port);
            }

            // Reconstruct final normalized URI
            String finalUri = scheme + "://" + host + ":" + port;

            return new MqttUri(scheme, host, port, finalUri);

        } catch (URISyntaxException e) {
            throw new IllegalArgumentException("Invalid MQTT server URI format: " + serverUri +
                    ". Expected format: protocol://host:port (e.g., tcp://localhost:1883, ssl://mqtt.example.com:8883)",
                    e);
        }
    }

    /**
     * Check if external MQTT broker is enabled based on configuration value.
     * @param mqttExternalConfig the configuration value for mqtt.external parameter
     * @return true if external broker is enabled, false for embedded broker
     */
    public static boolean isExternalEnabled(String mqttExternalConfig) {
        return mqttExternalConfig != null &&
               (mqttExternalConfig.equals("1") || mqttExternalConfig.equalsIgnoreCase("true"));
    }

    private static boolean isValidScheme(String protocolScheme) {
        return "tcp".equals(protocolScheme) || "ssl".equals(protocolScheme) ||
               "mqtt".equals(protocolScheme) || "mqtts".equals(protocolScheme);
    }

    /**
     * Unified SSL configuration for both broker and client.
     * Ensures consistent SSL setup between broker and MQTT client.
     *
     * @param mqttUri the MQTT URI requiring SSL configuration
     * @param keystorePassword the keystore password
     * @return SSLSocketFactory for client use, null if not needed
     * @throws Exception if SSL configuration fails
     */
    public static SSLSocketFactory configureSSL(MqttUri mqttUri, String keystorePassword) throws Exception {
        if (!mqttUri.isSecure()) {
            return null; // No SSL configuration needed
        }

        String domain = mqttUri.getHost();
        String tomcatHome = System.getProperty("catalina.home");
        String keystorePath = tomcatHome + "/ssl/" + domain + ".jks";

        // Validate keystore file
        java.io.File keystoreFile = new java.io.File(keystorePath);
        if (!keystoreFile.exists()) {
            throw new IllegalStateException("SSL keystore not found at: " + keystorePath +
                    ". Ensure SSL certificates are properly configured for domain: " + domain);
        }
        if (!keystoreFile.canRead()) {
            throw new IllegalStateException("Cannot read SSL keystore at: " + keystorePath +
                    ". Check file permissions.");
        }

        // Load keystore
        KeyStore keyStore = KeyStore.getInstance("JKS");
        try (FileInputStream fis = new FileInputStream(keystorePath)) {
            keyStore.load(fis, keystorePassword.toCharArray());
        } catch (Exception e) {
            throw new IllegalStateException("Failed to load SSL keystore from: " + keystorePath +
                    ". Check keystore password and file integrity.", e);
        }

        // Configure JVM-wide SSL properties for broker
        System.setProperty("javax.net.ssl.keyStore", keystorePath);
        System.setProperty("javax.net.ssl.keyStorePassword", keystorePassword);

        // Create SSL socket factory for client
        try {
            TrustManagerFactory tmf = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
            tmf.init(keyStore);
            SSLContext sslContext = SSLContext.getInstance("TLS");
            sslContext.init(null, tmf.getTrustManagers(), null);
            return sslContext.getSocketFactory();
        } catch (Exception e) {
            throw new IllegalStateException("Failed to create SSL socket factory for MQTT connection: " +
                    mqttUri + ". " + e.getMessage(), e);
        }
    }
}