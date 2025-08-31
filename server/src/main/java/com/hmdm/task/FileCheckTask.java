package com.hmdm.task;

import com.google.inject.Inject;
import com.google.inject.name.Named;
import com.hmdm.event.EventService;
import com.hmdm.persistence.UnsecureDAO;
import com.hmdm.persistence.UploadedFileDAO;
import com.hmdm.persistence.domain.Customer;
import com.hmdm.persistence.domain.UploadedFile;
import com.hmdm.service.EmailService;
import com.hmdm.service.MailchimpService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.BasicFileAttributes;
import java.nio.file.attribute.FileTime;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class FileCheckTask implements Runnable {
    private final UnsecureDAO unsecureDAO;
    private final String filesDirectory;

    private static final Logger logger = LoggerFactory.getLogger(FileCheckTask.class);

    @Inject
    public FileCheckTask(UnsecureDAO unsecureDAO,
                         @Named("files.directory") String filesDirectory) {
        this.unsecureDAO = unsecureDAO;
        this.filesDirectory = filesDirectory;
    }

    private Integer customerId;
    private Path rootDir;

    @Override
    public void run() {
        try {
            if (unsecureDAO.isSingleCustomer()) {
                customerId = 1;
                rootDir = Paths.get(this.filesDirectory);
                checkFiles(rootDir);
            } else {
                List<Customer> customers = unsecureDAO.getAllCustomersUnsecure();
                for (Customer customer : customers) {
                    if (!customer.isMaster()) {
                        customerId = customer.getId();
                        rootDir = Paths.get(this.filesDirectory, customer.getFilesDir());
                        checkFiles(rootDir);
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void checkFiles(Path directory) {
        try {
            Files.list(directory).forEach(path -> {
                if (Files.isDirectory(path)) {
                    checkFiles(path);
                } else if (Files.isRegularFile(path)) {
                    checkFile(path);
                }
            });
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void checkFile(Path file) {
        if (file.getFileName().toString().endsWith(".apk") || file.getFileName().toString().endsWith(".xapk")) {
            // For simplicity, do not process APK files as we presume they're always added as applications
            return;
        }

        String filePath = file.toString();
        String rootPath = rootDir.toString();
        if (filePath.startsWith(rootPath) && filePath.length() > rootPath.length()) {
            // +1 to remove path separator
            filePath = filePath.substring(rootPath.length() + 1);
        }

        UploadedFile uploadedFile = unsecureDAO.getUploadedFileByPath(customerId, filePath);
        if (uploadedFile == null) {
            // A file has been added externally; add the database record with default values
            uploadedFile = new UploadedFile();
            uploadedFile.setCustomerId(customerId);
            uploadedFile.setFilePath(filePath);
            unsecureDAO.insertUploadedFile(uploadedFile);
        }

        // Check the creation date and update the DB if a file was changed externally
        try {
            BasicFileAttributes attrs = Files.readAttributes(file, BasicFileAttributes.class);
            FileTime creationTime = attrs.creationTime();
            long millis = creationTime.toMillis();
            if (uploadedFile.getUploadTime() != millis) {
                uploadedFile.setUploadTime(millis);
                unsecureDAO.updateUploadedFile(uploadedFile);
            }
        } catch (IOException e) {
            logger.error("Failed to get attributes of " + file.toString() + ": " + e.getMessage());
            e.printStackTrace();
            return;
        }
    }
}
