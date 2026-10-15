package com.hmdm.notification;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mockStatic;

import com.hivemq.client.mqtt.MqttClient;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class PushSenderMqttSchemeTest {
    @ParameterizedTest
    @CsvSource({
            "wss://localhost, true, 8883",
            "wss://localhost:31000, true, 31000",
            "mqtts://localhost, true, 8883",
            "ssl://localhost, true, 8883",
            "mqtt://localhost, false, 1883",
            "localhost:31000, false, 31000"
    })
    void senderSelectsTlsAndPortConsistentlyWithBroker(String uri, boolean tls, int port) throws Exception {
        var sender = new PushSenderMqtt(uri, "test", "true", false, "", 0, null, null, null);
        // Stop after URI parsing, before creating sockets or starting reconnect threads.
        try (var client = mockStatic(MqttClient.class)) {
            client.when(MqttClient::builder).thenThrow(new IllegalStateException("Stop before network connection"));
            sender.init();
        }
        var sslField = PushSenderMqtt.class.getDeclaredField("connectSSL");
        sslField.setAccessible(true);
        var portField = PushSenderMqtt.class.getDeclaredField("connectPort");
        portField.setAccessible(true);
        assertEquals(tls, sslField.getBoolean(sender));
        assertEquals(port, portField.getInt(sender));
    }
}
