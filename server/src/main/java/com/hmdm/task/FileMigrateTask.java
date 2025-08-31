package com.hmdm.task;

import com.google.inject.Inject;
import com.google.inject.name.Named;
import com.hmdm.persistence.UnsecureDAO;
import com.hmdm.persistence.UploadedFileDAO;
import com.hmdm.persistence.domain.Configuration;
import com.hmdm.persistence.domain.ConfigurationFile;
import com.hmdm.persistence.domain.Customer;
import com.hmdm.persistence.domain.UploadedFile;
import com.hmdm.persistence.mapper.ConfigurationFileMapper;
import com.hmdm.persistence.mapper.UploadedFileMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.BasicFileAttributes;
import java.nio.file.attribute.FileTime;
import java.util.List;

/**
 * In version 5.36.1, a new way to work with files has been implemented
 * This task migrates old style configuration files to new way
 */
public class FileMigrateTask implements Runnable {

    private final ConfigurationFileMapper mapper;
    private final UploadedFileMapper uploadedFileMapper;
    private final UnsecureDAO unsecureDAO;
    private final String filesDirectory;

    private static final Logger logger = LoggerFactory.getLogger(FileMigrateTask.class);

    @Inject
    public FileMigrateTask(ConfigurationFileMapper configurationFileMapper,
                           UploadedFileMapper uploadedFileMapper,
                           UnsecureDAO unsecureDAO,
                           @Named("files.directory") String filesDirectory) {
        this.mapper = configurationFileMapper;
        this.uploadedFileMapper = uploadedFileMapper;
        this.unsecureDAO = unsecureDAO;
        this.filesDirectory = filesDirectory;
    }

    @Override
    public void run() {
        migrateConfigFiles();
        deleteOrphanedDuplicates();
    }

    private void migrateConfigFiles() {
        try {
            boolean isSingleCustomer = unsecureDAO.isSingleCustomer();
            Path rootDir = Paths.get(this.filesDirectory);

            List<ConfigurationFile> oldFiles = mapper.getOldStyleConfigurationFiles();
            oldFiles.forEach(cf -> {
                logger.info("Migrating config file: id=" + cf.getId() + ", filePath=" + cf.getFilePath() + ", URL=" + cf.getExternalUrl());

                Configuration configuration = unsecureDAO.getConfigurationById(cf.getConfigurationId());
                if (configuration == null) {
                    // Corrupted record?
                    logger.info("Configuration = null, deleting");
                    mapper.deleteConfigurationFile(cf.getId());
                    return;
                }
                Customer customer = unsecureDAO.getCustomerByIdUnsecure(configuration.getCustomerId());
                if (customer == null) {
                    // Corrupted record?
                    logger.info("Customer = null, deleting");
                    mapper.deleteConfigurationFile(cf.getId());
                    return;
                }

                int duplicateFileId = 0;

                if (cf.getFileId() != null) {
                    // Uploaded (non-external) file
                    UploadedFile file = uploadedFileMapper.findById(cf.getFileId());
                    if (file == null) {
                        // Corrupted record, remove it
                        logger.info("File = null (id=" + cf.getFileId() + "), deleting");
                        mapper.deleteConfigurationFile(cf.getId());
                        return;
                    }
                    file.setDescription(cf.getDescription());
                    file.setFilePath(cf.getFilePath());
                    file.setDevicePath(cf.getDevicePath());
                    file.setExternal(false);
                    file.setExternalUrl(null);
                    file.setReplaceVariables(cf.isReplaceVariables());
                    Path customerDir = isSingleCustomer ? rootDir : Paths.get(this.filesDirectory, customer.getFilesDir());
                    Path filePath = Paths.get(customerDir.toString(), cf.getFilePath());
                    if (Files.isRegularFile(filePath)) {
                        try {
                            BasicFileAttributes attrs = Files.readAttributes(filePath, BasicFileAttributes.class);
                            FileTime creationTime = attrs.creationTime();
                            long millis = creationTime.toMillis();
                            file.setUploadTime(millis);
                        } catch (IOException e) {
                            e.printStackTrace();
                            logger.info("Exception " + e.getMessage() + ", deleting");
                            mapper.deleteConfigurationFile(cf.getId());
                            return;
                        }
                    } else if (!cf.isRemove()) {
                        // File is deleted, if it's not for removal, let's delete the corrupted record
                        logger.info("File deleted, deleting");
                        mapper.deleteConfigurationFile(cf.getId());
                        return;
                    }
                    UploadedFile sameFile = uploadedFileMapper.findSame(
                            customer.getId(),
                            file.getDescription(),
                            file.getFilePath(),
                            file.getDevicePath(),
                            file.isExternal(),
                            file.getExternalUrl(),
                            file.isReplaceVariables());
                    if (sameFile == null) {
                        logger.info("Updating uploaded file with new data");
                        uploadedFileMapper.update(file);
                    } else {
                        // Remove duplicates
                        logger.info("Found duplicate, id=" + sameFile.getId());
                        duplicateFileId = file.getId();
                        cf.setFileId(sameFile.getId());
                    }
                } else {
                    // External file
                    UploadedFile file = new UploadedFile();
                    file.setCustomerId(customer.getId());
                    file.setDescription(cf.getDescription());
                    file.setUploadTime(0L);
                    file.setDevicePath(cf.getDevicePath());
                    file.setExternal(true);
                    file.setExternalUrl(cf.getExternalUrl());
                    file.setReplaceVariables(cf.isReplaceVariables());

                    UploadedFile sameFile = uploadedFileMapper.findSame(
                            customer.getId(),
                            file.getDescription(),
                            file.getFilePath(),
                            file.getDevicePath(),
                            file.isExternal(),
                            file.getExternalUrl(),
                            file.isReplaceVariables());
                    if (sameFile == null) {
                        uploadedFileMapper.insert(file);
                        logger.info("Uploaded file (external) created, id=" + file.getId());
                        cf.setFileId(file.getId());
                    } else {
                        logger.info("Found duplicate, id=" + sameFile.getId());
                        cf.setFileId(sameFile.getId());
                    }
                }

                // Reset the legacy attributes
                cf.setDescription(null);
                cf.setDevicePath(null);
                cf.setExternalUrl(null);
                cf.setFilePath(null);
                cf.setChecksum(null);
                cf.setLastUpdate(0L);
                cf.setReplaceVariables(false);
                mapper.updateOldStyleConfigurationFile(cf);
                logger.info("Configuration file updated, id=" + cf.getId() + ", fileId=" + cf.getFileId());
                if (duplicateFileId != 0 && mapper.countFileUsedAsConfigFile(duplicateFileId) == 0) {
                    logger.info("Duplicated file deleted, fileId=" + duplicateFileId);
                    uploadedFileMapper.delete(duplicateFileId);
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void deleteOrphanedDuplicates() {
        List<Customer> customers = unsecureDAO.getAllCustomersUnsecure();
        for (Customer customer : customers) {
            deleteOrphanedDuplicates(customer);
        }
    }

    private void deleteOrphanedDuplicates(Customer customer) {
        List<UploadedFile> orphans = uploadedFileMapper.findOrphaned(customer.getId());
        // Sequential execution is important here
        for (UploadedFile file : orphans) {
            if (file.isExternal() && uploadedFileMapper.countExternalDuplicates(file.getId(), customer.getId(), file.getExternalUrl()) > 0 ||
                    !file.isExternal() && uploadedFileMapper.countUploadedDuplicates(file.getId(), customer.getId(), file.getFilePath()) > 0) {
                logger.info("Orphaned uploaded file has duplicates, deleted, id=" + file.getId());
                uploadedFileMapper.delete(file.getId());
            }
        }
    }
}
