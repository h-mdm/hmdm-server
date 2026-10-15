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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

/**
 * Unit tests for the Artemis PEMCFG file generation ({@link NotificationMqttTaskModule#writePemCfg(String, String, String)}) — the on-disk artifact
 * that points the broker at the certbot PEM files. Verifies the generated contents and the fail-closed behaviour; whether Artemis actually honours
 * sslAutoReload is library behaviour covered by manual verification, not by this unit test.
 */
public class MqttPemCfgTest {

    @Test
    void writesSourceKeyAndCertLines(@TempDir Path dir) throws IOException {
        Path pemCfg = dir.resolve("hmdm-mqtt-broker.pemcfg");

        NotificationMqttTaskModule.writePemCfg(
                pemCfg.toString(), "/etc/hmdm/tls/current/privkey.pem", "/etc/hmdm/tls/current/fullchain.pem");

        assertEquals(
                "source.key=/etc/hmdm/tls/current/privkey.pem\n"
                        + "source.cert=/etc/hmdm/tls/current/fullchain.pem\n",
                Files.readString(pemCfg));
    }

    @Test
    void overwritesExistingFile(@TempDir Path dir) throws IOException {
        Path pemCfg = dir.resolve("broker.pemcfg");
        Files.writeString(pemCfg, "source.key=/old/key.pem\nsource.cert=/old/cert.pem\n");

        NotificationMqttTaskModule.writePemCfg(pemCfg.toString(), "/new/key.pem", "/new/cert.pem");

        assertEquals("source.key=/new/key.pem\nsource.cert=/new/cert.pem\n", Files.readString(pemCfg));
    }

    @Test
    void createsMissingParentDirectories(@TempDir Path dir) throws IOException {
        Path pemCfg = dir.resolve("nested/sub/broker.pemcfg");

        NotificationMqttTaskModule.writePemCfg(pemCfg.toString(), "/k.pem", "/c.pem");

        assertTrue(Files.exists(pemCfg));
        assertEquals("source.key=/k.pem\nsource.cert=/c.pem\n", Files.readString(pemCfg));
    }

    @Test
    void unwritableTarget_failsClosedWithIllegalState(@TempDir Path dir) throws IOException {
        // The parent path is a regular file, so the directory chain cannot be created. writePemCfg must
        // fail closed (IllegalStateException) so startup aborts rather than serving stale / no TLS.
        Path file = dir.resolve("not-a-dir");
        Files.writeString(file, "x");
        Path pemCfg = file.resolve("broker.pemcfg");

        assertThrows(
                IllegalStateException.class,
                () -> NotificationMqttTaskModule.writePemCfg(pemCfg.toString(), "/k.pem", "/c.pem"));
    }
}
