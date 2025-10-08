import java.net.URI;

/**
 * Simple test class to validate MQTT URI validation logic
 * This tests the URI validation functionality from PushSenderMqtt
 */
public class test_uri_validation {
    
    private static void validateServerUri(String serverUri) {
        System.out.println("\n=== Testing URI: " + serverUri + " ===");
        
        if (serverUri == null || serverUri.trim().isEmpty()) {
            throw new IllegalArgumentException("MQTT server URI cannot be null or empty");
        }

        try {
            URI uri = new URI(serverUri);
            String scheme = uri.getScheme();
            String host = uri.getHost();
            int port = uri.getPort();

            System.out.println("Original - Scheme: " + scheme + ", Host: " + host + ", Port: " + port);

            // Apply defaults and build the final URI
            if (scheme == null) {
                scheme = "tcp";
            } else if (!scheme.equals("tcp") && !scheme.equals("ssl") &&
                !scheme.equals("mqtt") && !scheme.equals("mqtts")) {
                throw new IllegalArgumentException("Invalid MQTT protocol scheme: " + scheme +
                    ". Supported protocols: tcp://, ssl://, mqtt://, mqtts://");
            }

            if (host == null || host.trim().isEmpty()) {
                host = "localhost";
            }

            if (port == -1) {
                port = 31000;
            } else if (port < 1 || port > 65535) {
                throw new IllegalArgumentException("MQTT server port must be between 1 and 65535, got: " + port);
            }
            
            // Reconstruct the URI with defaults applied
            String finalUri = scheme + "://" + host + ":" + port;
            System.out.println("Final URI: " + finalUri);
            System.out.println("✓ PASS");

        } catch (Exception e) {
            if (e instanceof IllegalArgumentException) {
                System.out.println("✗ FAIL: " + e.getMessage());
                return; // Don't throw, just report failure
            }
            System.out.println("✗ FAIL: Invalid MQTT server URI format: " + serverUri +
                ". Expected format: protocol://host:port (e.g., tcp://localhost:1883, ssl://mqtt.example.com:8883). Error: " + e.getMessage());
        }
    }
    
    public static void main(String[] args) {
        System.out.println("MQTT URI Validation Tests");
        System.out.println("=========================");
        
        // Test cases covering various scenarios
        String[] testUris = {
            // Valid URIs
            "tcp://localhost:1883",
            "ssl://mqtt.example.com:8883",
            "mqtt://broker.hivemq.com:1883",
            "mqtts://test.mosquitto.org:8884",
            
            // URIs requiring defaults
            "tcp://example.com",           // Missing port
            "ssl://192.168.1.100",         // Missing port
            "localhost:1883",              // Missing scheme
            "example.com",                 // Missing scheme and port
            "",                            // Empty URI (should fail)
            
            // Invalid URIs
            "http://localhost:1883",       // Wrong protocol
            "ftp://example.com:21",        // Wrong protocol
            "tcp://localhost:99999",       // Invalid port
            "ssl://localhost:0",           // Invalid port
        };
        
        for (String testUri : testUris) {
            try {
                validateServerUri(testUri);
            } catch (Exception e) {
                System.out.println("✗ FAIL: " + e.getMessage());
            }
        }
        
        System.out.println("\n=== URI Validation Tests Complete ===");
    }
}