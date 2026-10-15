package com.hmdm.util;

import static org.junit.jupiter.api.Assertions.*;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class UploadIOTests {
    @Test
    void copiesAllBytesWithoutClosingCallerInput(@TempDir Path dir) throws Exception {
        byte[] content = new byte[32769];
        java.util.Arrays.fill(content, (byte) 123);
        var input = new ByteArrayInputStream(content) {
            @Override
            public void close() {
                fail("The caller owns the input stream");
            }
        };
        Path target = dir.resolve("upload");
        FileUtil.writeToFile(input, target.toString());
        assertArrayEquals(content, Files.readAllBytes(target));
    }

    @Test
    void reportsReadAndDestinationFailures(@TempDir Path dir) throws Exception {
        IOException failure = new IOException("upload interrupted");
        InputStream broken = new InputStream() {
            @Override
            public int read() throws IOException {
                throw failure;
            }
        };
        assertSame(failure, assertThrows(IOException.class,
                () -> FileUtil.writeToFile(broken, dir.resolve("upload").toString())));
        assertThrows(IOException.class,
                () -> FileUtil.writeToFile(new ByteArrayInputStream(new byte[] {1}), dir.toString()));
    }
}
