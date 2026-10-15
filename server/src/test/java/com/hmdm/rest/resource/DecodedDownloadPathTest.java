package com.hmdm.rest.resource;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.hmdm.persistence.domain.Video;
import java.io.ByteArrayInputStream;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import org.glassfish.jersey.media.multipart.FormDataContentDisposition;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class DecodedDownloadPathTest {
    @Test
    void legacyVideoSpacesFallBackWithoutOverridingLiteralPlus(@TempDir Path dir) throws Exception {
        VideosResource videos = new VideosResource(dir.toString(), "https://example.com");
        Files.writeString(dir.resolve("my video.mp4"), "legacy");
        assertVideoContent(videos, "my+video.mp4", "legacy");
        Files.writeString(dir.resolve("my+video.mp4"), "literal plus");
        assertVideoContent(videos, "my+video.mp4", "literal plus");
        Files.writeString(dir.resolve("literal%20 name.mp4"), "percent preserved");
        assertVideoContent(videos, "literal%20+name.mp4", "percent preserved");
        try (var response = videos.downloadVideo("missing+video.mp4")) {
            assertEquals(404, response.getStatus());
        }
    }

    private static void assertVideoContent(VideosResource videos, String name, String expected) throws Exception {
        try (var response = videos.downloadVideo(name)) {
            assertEquals(200, response.getStatus());
            var output = new java.io.ByteArrayOutputStream();
            ((jakarta.ws.rs.core.StreamingOutput) response.getEntity()).write(output);
            assertEquals(expected, output.toString(java.nio.charset.StandardCharsets.UTF_8));
        }
    }

    @Test
    void videoUploadUsesPathEncodingForSpacesPlusAndPercent(@TempDir Path dir) throws Exception {
        String name = "a b+100%.mp4";
        var disposition = mock(FormDataContentDisposition.class);
        when(disposition.getFileName()).thenReturn(name);
        VideosResource videos = new VideosResource(dir.toString(), "https://example.com");
        var result = videos.uploadVideo(new ByteArrayInputStream(new byte[] {1, 2, 3}), disposition);
        String url = ((Video) result.getData()).getPath();
        assertEquals("https://example.com/rest/public/videos/a%20b%2B100%25.mp4", url);
        String decodedPath = URI.create(url).getPath();
        try (var response = videos.downloadVideo(decodedPath.substring(decodedPath.lastIndexOf('/') + 1))) {
            assertEquals(200, response.getStatus());
        }
    }

    @Test
    void resourcesPreserveAlreadyDecodedPlusAndPercent(@TempDir Path dir) throws Exception {
        FilesResource files = new FilesResource();
        var directory = FilesResource.class.getDeclaredField("filesDirectory");
        directory.setAccessible(true);
        directory.set(files, dir.toString());
        PublicFilesResource publicFiles = new PublicFilesResource(dir.toString());
        VideosResource videos = new VideosResource(dir.toString(), "https://example.com");
        for (String name : new String[] {"a+b.txt", "literal%20name.txt", "100%.txt", "a b.txt"}) {
            Files.writeString(dir.resolve(name), "content");
            // Jakarta REST has already decoded @PathParam before invoking these methods.
            try (var first = files.downloadFile(name);
                    var second = publicFiles.downloadFile(name);
                    var third = videos.downloadVideo(name)) {
                assertEquals(200, first.getStatus(), name);
                assertEquals(200, second.getStatus(), name);
                assertEquals(200, third.getStatus(), name);
            }
        }
    }
}
