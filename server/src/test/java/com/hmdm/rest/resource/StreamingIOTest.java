package com.hmdm.rest.resource;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.StreamingOutput;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import org.glassfish.jersey.media.multipart.FormDataContentDisposition;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class StreamingIOTest {
    private List<Response> responses(Path dir, String name) throws Exception {
        FilesResource files = new FilesResource();
        var directory = FilesResource.class.getDeclaredField("filesDirectory");
        directory.setAccessible(true);
        directory.set(files, dir.toString());
        PublicResource publicResource = new PublicResource();
        var logo = PublicResource.class.getDeclaredField("appLogo");
        logo.setAccessible(true);
        logo.set(publicResource, dir.resolve(name).toString());
        return List.of(files.downloadFile(name),
                new PublicFilesResource(dir.toString()).downloadFile(name),
                new VideosResource(dir.toString(), "https://example.com").downloadVideo(name),
                publicResource.getRebrandedLogo());
    }

    @Test
    void streamsCompleteContentWithoutClosingResponseOutput(@TempDir Path dir) throws Exception {
        byte[] content = new byte[32769];
        java.util.Arrays.fill(content, (byte) 42);
        Files.write(dir.resolve("file"), content);
        for (Response response : responses(dir, "file")) {
            try (response) {
                var output = new ByteArrayOutputStream() {
                    @Override
                    public void close() {
                        fail("The container owns the output stream");
                    }
                };
                ((StreamingOutput) response.getEntity()).write(output);
                assertArrayEquals(content, output.toByteArray());
            }
        }
    }

    @Test
    void propagatesOutputFailures(@TempDir Path dir) throws Exception {
        Files.writeString(dir.resolve("file"), "content");
        IOException failure = new IOException("client disconnected");
        OutputStream broken = new OutputStream() {
            @Override
            public void write(int value) throws IOException {
                throw failure;
            }
        };
        for (Response response : responses(dir, "file")) {
            try (response) {
                assertSame(failure, assertThrows(IOException.class,
                        () -> ((StreamingOutput) response.getEntity()).write(broken)));
            }
        }
    }

    @Test
    void opensInputOnlyWhenResponseIsWritten(@TempDir Path dir) throws Exception {
        Path file = Files.writeString(dir.resolve("file"), "content");
        List<Response> responses = responses(dir, "file");
        Files.delete(file);
        for (Response response : responses) {
            try (response) {
                assertThrows(IOException.class,
                        () -> ((StreamingOutput) response.getEntity()).write(OutputStream.nullOutputStream()));
            }
        }
    }

    @Test
    void interruptedVideoUploadCannotReturnSuccess(@TempDir Path dir) throws Exception {
        var disposition = mock(FormDataContentDisposition.class);
        when(disposition.getFileName()).thenReturn("file.mp4");
        InputStream broken = new InputStream() {
            @Override
            public int read() throws IOException {
                throw new IOException("upload interrupted");
            }
        };
        assertThrows(IOException.class,
                () -> new VideosResource(dir.toString(), "https://example.com").uploadVideo(broken, disposition));
    }
}
