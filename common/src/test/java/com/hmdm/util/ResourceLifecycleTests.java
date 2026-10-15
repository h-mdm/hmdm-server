package com.hmdm.util;

import static org.junit.jupiter.api.Assertions.*;

import com.hmdm.persistence.domain.Customer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.TimeUnit;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class ResourceLifecycleTests {
    @Test
    void shutdownCancelsPeriodicWorkAndRejectsNewTasks() throws Exception {
        BackgroundTaskRunnerService service = new BackgroundTaskRunnerService();
        CountDownLatch ran = new CountDownLatch(1);
        try {
            var periodic = service.submitRepeatableTask(ran::countDown, 0, 1, TimeUnit.HOURS);
            assertTrue(ran.await(5, TimeUnit.SECONDS));
            service.shutdown();
            assertTrue(periodic.isCancelled());
            assertThrows(RejectedExecutionException.class, () -> service.submitTask(() -> {
            }));
            assertThrows(RejectedExecutionException.class,
                    () -> service.submitRepeatableTask(() -> {
                    }, 0, 1, TimeUnit.SECONDS));
            service.shutdown(); // repeated context cleanup is harmless
        } finally {
            service.shutdown();
        }
    }

    @Test
    void failedMoveKeepsOriginalUpload(@TempDir Path dir) throws Exception {
        Customer customer = new Customer();
        customer.setFilesDir("tenant");
        Path source = Files.writeString(dir.resolve("upload"), "original upload");
        // A regular file blocks creation of the destination directory, on any OS.
        Path blocked = Files.writeString(dir.resolve("blocked"), "not a directory");
        assertNull(FileUtil.moveFile(customer, blocked.toString(), null, source.toString(), "file.apk"));
        assertEquals("original upload", Files.readString(source));
    }

    @Test
    void moveDoesNotOverwriteExistingDestination(@TempDir Path dir) throws Exception {
        Customer customer = new Customer();
        customer.setFilesDir("tenant");
        Path source = Files.writeString(dir.resolve("upload"), "new");
        Path target = Files.createDirectories(dir.resolve("tenant")).resolve("file.apk");
        Files.writeString(target, "old");
        assertThrows(FileExistsException.class,
                () -> FileUtil.moveFile(customer, dir.toString(), null, source.toString(), "file.apk"));
        assertEquals("new", Files.readString(source));
        assertEquals("old", Files.readString(target));
    }
}
