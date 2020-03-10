/*
 *
 * Headwind MDM: Open Source Android MDM Software
 * https://h-mdm.com
 *
 * Copyright (C) 2019 Headwind Solutions LLC (http://h-sms.com)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package com.hmdm.persistence;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.hmdm.persistence.domain.Customer;
import com.hmdm.persistence.domain.UploadedFile;
import com.hmdm.persistence.mapper.UploadedFileMapper;
import com.hmdm.security.SecurityException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Named;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

/**
 * <p>A DAO used for managing the icon data.</p>
 *
 * @author isv
 */
@Singleton
public class UploadedFileDAO extends AbstractDAO<UploadedFile> {

    private static final Logger logger = LoggerFactory.getLogger(UploadedFileDAO.class);

    /**
     * <p>An interface to file data persistence layer.</p>
     */
    private final UploadedFileMapper fileMapper;
    private final CustomerDAO customerDAO;
    private String filesDirectory;

    /**
     * <p>Constructs new <code>UploadedFileDAO</code> instance. This implementation does nothing.</p>
     */
    @Inject
    public UploadedFileDAO(UploadedFileMapper fileMapper,
                           CustomerDAO customerDAO,
                           @Named("files.directory") String filesDirectory) {
        this.fileMapper = fileMapper;
        this.customerDAO = customerDAO;
        this.filesDirectory = filesDirectory;
    }

    /**
     * <p>Inserts new record for the specified uploaded file.</p>
     *
     * @param file an uploaded filed to be inserted into DB.
     * @return a created file.
     */
    public UploadedFile insertFile(UploadedFile file) {
        insertRecord(file, this.fileMapper::insertFile);
        return getSingleRecord(() -> this.fileMapper.getFileById(file.getId()), SecurityException::onUploadedFileAccessViolation);
    }

    /**
     * <p>Removes new record for the specified uploaded file.</p>
     *
     * @param fileId an uploaded filed to be removed from DB and disk.
     */
    public void removeFile(int fileId) {
        final UploadedFile file = getSingleRecord(() -> this.fileMapper.getFileById(fileId), SecurityException::onUploadedFileAccessViolation);
        final Customer customer = this.customerDAO.findById(file.getCustomerId());

        java.nio.file.Path filePath;
        if (customer.getFilesDir() == null || customer.getFilesDir().isEmpty()) {
            filePath = Paths.get( this.filesDirectory, file.getFilePath());
        } else {
            filePath = Paths.get(this.filesDirectory, customer.getFilesDir(), file.getFilePath());
        }
        try {
            logger.debug("Deleting file: {}", filePath);
            Files.delete(filePath);
        } catch (IOException e) {
            logger.error("Failed to delete file: {}", filePath, e);
        }

        this.fileMapper.deleteFile(fileId);
    }


}
