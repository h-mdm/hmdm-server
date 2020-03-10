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
import com.hmdm.persistence.mapper.ConfigurationFileMapper;

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

    private final UploadedFileDAO uploadedFileDAO;

    /**
     * <p>Constructs new <code>ConfigurationFileDAO</code> instance. This implementation does nothing.</p>
     */
    @Inject
    public ConfigurationFileDAO(ConfigurationFileMapper configurationFileMapper, UploadedFileDAO uploadedFileDAO) {
        this.configurationFileMapper = configurationFileMapper;
        this.uploadedFileDAO = uploadedFileDAO;
    }

    public List<ConfigurationFile> getConfigurationFiles(Integer configurationId) {
        return this.configurationFileMapper.getConfigurationFiles(configurationId);
    }

    public boolean isFileUsed(String fileName) {
        return this.configurationFileMapper.countFileUsedAsConfigFile(fileName) > 0;
    }

    public List<String> getUsingConfigurations(Integer customerId, String fileName) {
        return this.configurationFileMapper.getUsingConfigurations(customerId, fileName);
    }

    public void removeFileFromDisk(Integer fileId) {
        this.uploadedFileDAO.removeFile(fileId);
    }
}
