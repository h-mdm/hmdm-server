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
import com.hmdm.persistence.domain.ConfigurationFile;
import com.hmdm.persistence.domain.Customer;
import com.hmdm.persistence.domain.UploadedFile;
import com.hmdm.persistence.mapper.ConfigurationFileMapper;
import com.hmdm.persistence.mapper.UploadedFileMapper;
import com.hmdm.rest.json.FileConfigurationLink;
import com.hmdm.security.SecurityContext;
import com.hmdm.security.SecurityException;
import org.mybatis.guice.transactional.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Named;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;

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
    private final ConfigurationFileMapper configurationFileMapper;
    private final CustomerDAO customerDAO;
    private String filesDirectory;

    /**
     * <p>Constructs new <code>UploadedFileDAO</code> instance. This implementation does nothing.</p>
     */
    @Inject
    public UploadedFileDAO(UploadedFileMapper fileMapper,
                           ConfigurationFileMapper configurationFileMapper,
                           CustomerDAO customerDAO,
                           @Named("files.directory") String filesDirectory) {
        this.fileMapper = fileMapper;
        this.configurationFileMapper = configurationFileMapper;
        this.customerDAO = customerDAO;
        this.filesDirectory = filesDirectory;
    }

    /**
     * <p>Inserts new record for the specified uploaded file.</p>
     *
     * @param file an uploaded filed to be inserted into DB.
     * @return a created file.
     */
    public UploadedFile insert(UploadedFile file) {
        insertRecord(file, this.fileMapper::insert);
        return getSingleRecord(() -> this.fileMapper.findById(file.getId()), SecurityException::onUploadedFileAccessViolation);
    }


    public List<UploadedFile> getAll() {
        return getListWithCurrentUser(currentUser -> this.fileMapper.getAll(currentUser.getCustomerId()));
    }

    public List<UploadedFile> getAllByValue(String value) {
        return getListWithCurrentUser(currentUser -> this.fileMapper.getAllByValue(currentUser.getCustomerId(), "%" + value + "%"));
    }

    public void update(UploadedFile file) {
        updateRecord(file, this.fileMapper::update, SecurityException::onUploadedFileAccessViolation);
    }

    public UploadedFile getById(Integer id) {
        return getSingleRecord(() -> this.fileMapper.findById(id), SecurityException::onUploadedFileAccessViolation);
    }

    public UploadedFile getByPath(Integer customerId, String filePath) {
        return getSingleRecord(() -> this.fileMapper.findByPath(customerId, filePath), SecurityException::onUploadedFileAccessViolation);
    }

    /**
     * <p>Removes new record for the specified uploaded file.</p>
     *
     * @param fileId an uploaded filed to be removed from DB and disk.
     */
    public void remove(int fileId) {
        updateById(
                fileId,
                this.fileMapper::findById,
                file -> this.fileMapper.delete(file.getId()),
                SecurityException::onUploadedFileAccessViolation);
    }

    public List<FileConfigurationLink> getFileConfigurations(Integer id) {
        final UploadedFile file = getById(id);
        if (file == null) {
            return new LinkedList<>();
        }
        return SecurityContext.get()
                .getCurrentUser()
                .filter(u -> u.getCustomerId() == file.getCustomerId())
                .map(u -> configurationFileMapper.getConfigurationFileLinks(u.getCustomerId(), u.getId(), id))
                .orElseThrow(SecurityException::onAnonymousAccess);
    }

    @Transactional
    public void updateFileConfigurations(List<FileConfigurationLink> linkList) {

        final List<FileConfigurationLink> noUploadLinks = linkList
                .stream()
                .filter(c -> c.getId() != null && !c.isUpload())
                .collect(Collectors.toList());
        noUploadLinks.forEach(link -> {
            configurationFileMapper.deleteConfigurationFile(link.getId());
        });

        final List<FileConfigurationLink> newUploadLinks = linkList
                .stream()
                .filter(c -> c.getId() == null && c.isUpload())
                .collect(Collectors.toList());
        if (newUploadLinks.size() > 0) {
            UploadedFile file = getById(newUploadLinks.get(0).getFileId());
            newUploadLinks.forEach(link -> {
                ConfigurationFile cf = new ConfigurationFile(link);
                configurationFileMapper.insertConfigurationFile(cf);
            });
        }
    }

}
