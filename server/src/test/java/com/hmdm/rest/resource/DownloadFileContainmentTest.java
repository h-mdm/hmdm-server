package com.hmdm.rest.resource;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

import com.hmdm.persistence.ApplicationDAO;
import com.hmdm.rest.filter.PublicIPFilter;
import jakarta.servlet.ServletOutputStream;
import jakarta.servlet.WriteListener;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.ws.rs.core.StreamingOutput;
import java.io.ByteArrayOutputStream;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class DownloadFileContainmentTest {
    @TempDir
    Path directory;

    @Test
    void rejectsTraversalIncludingSiblingWithSamePrefix() throws Exception {
        Path root = Files.createDirectory(directory.resolve("files"));
        Files.writeString(directory.resolve("secret.txt"), "secret");
        Path sibling = Files.createDirectory(directory.resolve("files-other"));
        Files.writeString(sibling.resolve("secret.txt"), "secret");
        assertDownload(root, "../secret.txt", false);
        assertDownload(root, "../files-other/secret.txt", false);
    }

    @Test
    void rejectsSymlinksToFilesAndDirectoriesOutsideRoot() throws Exception {
        Path root = Files.createDirectory(directory.resolve("files"));
        Path secret = Files.writeString(directory.resolve("secret.txt"), "secret");
        Files.createSymbolicLink(root.resolve("escape.txt"), secret);
        Files.createSymbolicLink(root.resolve("escape-dir"), directory);
        assertDownload(root, "escape.txt", false);
        assertDownload(root, "escape-dir/secret.txt", false);
    }

    @Test
    void allowsNestedFilesAndLiteralUrlCharactersWithSymlinkedStorageRoot() throws Exception {
        Path root = Files.createDirectory(directory.resolve("files"));
        Files.createDirectory(root.resolve("tenant"));
        Path alias = Files.createSymbolicLink(directory.resolve("storage"), root);
        for (String name : List.of("a b.txt", "a+b.txt", "100%.txt", "literal%20.txt")) {
            Files.writeString(root.resolve("tenant").resolve(name), "content");
            assertDownload(alias, "tenant/" + name, true);
        }
    }

    @Test
    void missingFilesAndDirectoriesReturnNotFound() throws Exception {
        Path root = Files.createDirectory(directory.resolve("files"));
        Files.createDirectory(root.resolve("tenant"));
        assertDownload(root, "missing.txt", false);
        assertDownload(root, "tenant", false);
    }

    private void assertDownload(Path root, String path, boolean allowed) throws Exception {
        FilesResource files = new FilesResource();
        var field = FilesResource.class.getDeclaredField("filesDirectory");
        field.setAccessible(true);
        field.set(files, root.toString());
        for (var response : List.of(files.downloadFile(path), new PublicFilesResource(root.toString()).downloadFile(path))) {
            try (response) {
                assertEquals(allowed ? 200 : 404, response.getStatus(), path);
                if (allowed) {
                    var output = new ByteArrayOutputStream();
                    ((StreamingOutput) response.getEntity()).write(output);
                    assertEquals("content", output.toString(StandardCharsets.UTF_8));
                }
            }
        }

        PublicIPFilter filter = mock(PublicIPFilter.class);
        HttpServletRequest request = mock(HttpServletRequest.class);
        HttpServletResponse response = mock(HttpServletResponse.class);
        when(filter.match(request)).thenReturn(true);
        when(request.getRequestURI()).thenReturn("/files/" + URLEncoder.encode(path, StandardCharsets.UTF_8));
        when(request.getDateHeader("If-Modified-Since")).thenReturn(-1L);
        var output = new ByteArrayOutputStream();
        if (allowed) {
            when(response.getOutputStream()).thenReturn(new ServletOutputStream() {
                public boolean isReady() { return true; }
                public void setWriteListener(WriteListener listener) {}
                public void write(int value) { output.write(value); }
            });
        } else {
            // Containment must be enforced before the partial-content branch, too.
            when(request.getHeader("Range")).thenReturn("bytes=0-5");
        }
        new DownloadFilesServlet(mock(ApplicationDAO.class), filter, root.toString(), false, "")
                .doGet(request, response);
        if (allowed) {
            verify(response, never()).sendError(anyInt());
            assertEquals("content", output.toString(StandardCharsets.UTF_8));
        } else {
            verify(response).sendError(404);
            verify(response, never()).getOutputStream();
        }
    }
}
