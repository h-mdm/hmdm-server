package com.hmdm.util;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class NativeUtilityTests {
    @Test
    void preservesDigestCaseLeadingZerosAndUrlSafePadding() throws Exception {
        assertEquals("000F80FF", CryptoUtil.getHexString(new byte[] {0, 15, (byte) 128, (byte) 255}));
        assertEquals("D41D8CD98F00B204E9800998ECF8427E", CryptoUtil.getMD5String(""));
        assertEquals("A9993E364706816ABA3E25717850C26C9CD0D89D", CryptoUtil.getSHA1String("abc"));
        assertEquals("-_8=", CryptoUtil.getBase64String(new byte[] {(byte) 251, (byte) 255}));
        assertEquals("", CryptoUtil.getBase64String(new byte[0]));
        // MD5("a") starts with zero; checksum output must retain all 32 lowercase digits.
        assertEquals("0cc175b9c0f1b6a831c399e269772661",
                CryptoUtil.calculateChecksum(new ByteArrayInputStream("a".getBytes(StandardCharsets.UTF_8))));
    }

    @Test
    void downloadsUtf8AcrossBufferBoundaryWithoutTrailingBytes(@TempDir Path dir) throws Exception {
        String expected = "a".repeat(1023) + "€\n更新";
        Path manifest = dir.resolve("manifest.json");
        Files.writeString(manifest, expected, StandardCharsets.UTF_8);

        assertEquals(expected, FileUtil.downloadTextFile(manifest.toUri().toURL()));
    }
}
