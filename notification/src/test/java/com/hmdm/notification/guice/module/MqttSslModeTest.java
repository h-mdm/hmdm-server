/*
 * Headwind MDM: Open Source Android MDM Software https://h-mdm.com
 *
 * Copyright (C) 2019 Headwind Solutions LLC (https://h-mdm.com)
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License. You may obtain a
 * copy of the License at http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the specific language governing permissions and limitations
 * under the License.
 */

package com.hmdm.notification.guice.module;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

/**
 * Unit tests for the fail-closed PEM path validation ({@link NotificationMqttTaskModule#requirePemPaths(String, String)}) and for the set of URI
 * schemes that select TLS.
 */
public class MqttSslModeTest {

    @Test
    void bothPemPaths_passes() {
        assertDoesNotThrow(() -> NotificationMqttTaskModule.requirePemPaths("/c/privkey.pem", "/c/fullchain.pem"));
    }

    @Test
    void neitherPemPath_failsFast() {
        assertThrows(IllegalStateException.class, () -> NotificationMqttTaskModule.requirePemPaths(null, ""));
    }

    @Test
    void onlyKey_failsFast() {
        assertThrows(IllegalStateException.class, () -> NotificationMqttTaskModule.requirePemPaths("/c/privkey.pem", null));
    }

    @Test
    void onlyCert_failsFast() {
        assertThrows(IllegalStateException.class, () -> NotificationMqttTaskModule.requirePemPaths("", "/c/fullchain.pem"));
    }

    @Test
    void unresolvedPlaceholderPemPaths_failFast() {
        // Maven leaves a literal "${ssl.pem.key.path}" in a filtered context.xml when build.properties
        // omits the key; that must NOT be treated as configured.
        assertThrows(
                IllegalStateException.class,
                () -> NotificationMqttTaskModule.requirePemPaths("${ssl.pem.key.path}", "${ssl.pem.cert.path}"));
    }

    @Test
    void wssStillSelectsSsl() {
        // Regression guard for a deliberate deviation. The upstream change this module was ported from
        // dropped "wss" from the TLS scheme list. Dropping it here would silently downgrade any
        // deployment using wss:// to a plaintext broker, so the scheme is kept and asserted.
        assertTrue(NotificationMqttTaskModule.isSslScheme(
                NotificationMqttTaskModule.parseServerUri("wss://mdm.example.com:8883")));
    }

    @Test
    void sslAndMqttsSelectSsl() {
        assertTrue(NotificationMqttTaskModule.isSslScheme(
                NotificationMqttTaskModule.parseServerUri("ssl://mdm.example.com:8883")));
        assertTrue(NotificationMqttTaskModule.isSslScheme(
                NotificationMqttTaskModule.parseServerUri("mqtts://mdm.example.com:8883")));
    }

    @Test
    void plainSchemesDoNotSelectSsl() {
        assertFalse(NotificationMqttTaskModule.isSslScheme(
                NotificationMqttTaskModule.parseServerUri("mqtt://mdm.example.com:1883")));
        // No scheme at all is the installed default (mqtt.server.uri = "<domain>:31000"): it is
        // normalized to mqtt:// and must not select TLS.
        assertFalse(NotificationMqttTaskModule.isSslScheme(
                NotificationMqttTaskModule.parseServerUri("mdm.example.com:31000")));
    }
}
