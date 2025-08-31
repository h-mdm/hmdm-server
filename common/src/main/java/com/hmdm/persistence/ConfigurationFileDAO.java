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
import com.hmdm.persistence.domain.Configuration;
import com.hmdm.persistence.domain.ConfigurationFile;
import com.hmdm.persistence.mapper.ConfigurationFileMapper;
import com.hmdm.persistence.mapper.ConfigurationMapper;
import com.hmdm.security.SecurityContext;
import com.hmdm.security.SecurityException;

import java.util.List;

/**
 * <p>A DAO for managing the configuration files in DB.</p>
 */
@Singleton
public class ConfigurationFileDAO {

    /**
     * <p>An interface to persistence layer.</p>
     */
    private final ConfigurationFileMapper configurationFileMapper;

    private final ConfigurationMapper configurationMapper;

    private final UploadedFileDAO uploadedFileDAO;

    /**
     * <p>Constructs new <code>ConfigurationFileDAO</code> instance. This implementation does nothing.</p>
     */
    @Inject
    public ConfigurationFileDAO(ConfigurationFileMapper configurationFileMapper,
                                ConfigurationMapper configurationMapper,
                                UploadedFileDAO uploadedFileDAO) {
        this.configurationFileMapper = configurationFileMapper;
        this.configurationMapper = configurationMapper;
        this.uploadedFileDAO = uploadedFileDAO;
    }

    public List<ConfigurationFile> getConfigurationFiles(Integer configurationId) {
        return this.configurationFileMapper.getConfigurationFiles(configurationId);
    }

    @Deprecated
    public ConfigurationFile getConfigurationFileByPath(Integer configurationId, String path) {
        return this.configurationFileMapper.getConfigurationFileByPath(configurationId, path);
    }

    public void insertConfigurationFile(ConfigurationFile configurationFile) {
        // Check access to configuration prior to making changes
        SecurityContext.get()
                .getCurrentUser()
                .ifPresent(u -> {
                    Configuration configuration = configurationMapper.getConfigurationById(configurationFile.getConfigurationId());
                    if (configuration == null) {
                        throw new IllegalArgumentException("Configuration id " + configurationFile.getConfigurationId() + " does not exist");
                    }
                    if (u.isSuperAdmin() || u.getCustomerId() == configuration.getCustomerId()) {
                        this.configurationFileMapper.insertConfigurationFile(configurationFile);
                    } else {
                        throw SecurityException.onConfigurationAccessViolation(configurationFile.getConfigurationId());
                    }
                });
    }

    public boolean isFileUsed(Integer fileId) {
        return this.configurationFileMapper.countFileUsedAsConfigFile(fileId) > 0;
    }

    public List<String> getUsingConfigurations(Integer customerId, Integer fileId) {
        return this.configurationFileMapper.getUsingConfigurations(customerId, fileId);
    }

    // Deprecated and not used any more
/*    public void removeFileFromDisk(Integer fileId) {
        this.uploadedFileDAO.remove(fileId);
    } */
}
