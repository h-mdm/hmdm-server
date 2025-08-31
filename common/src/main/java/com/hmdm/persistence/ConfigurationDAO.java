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

import java.io.IOException;
import java.net.URL;
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.google.inject.Singleton;
import com.hmdm.event.ConfigurationUpdatedEvent;
import com.hmdm.event.DeviceInfoUpdatedEvent;
import com.hmdm.event.EventService;
import com.hmdm.persistence.domain.Application;
import com.hmdm.persistence.domain.ApplicationSetting;
import com.hmdm.persistence.domain.Configuration;
import com.hmdm.persistence.domain.ConfigurationFile;
import com.hmdm.persistence.mapper.ApplicationMapper;
import com.hmdm.persistence.domain.ConfigurationApplicationParameters;
import com.hmdm.persistence.mapper.ConfigurationMapper;
import com.hmdm.security.SecurityException;
import com.hmdm.util.CryptoUtil;
import org.mybatis.guice.transactional.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Named;

@Singleton
public class ConfigurationDAO extends AbstractLinkedDAO<Configuration, Application> {

    private static final Logger log = LoggerFactory.getLogger(ConfigurationDAO.class);

    private final ConfigurationMapper mapper;
    private final ApplicationMapper applicationMapper;

    private final ApplicationSettingDAO applicationSettingDAO;
    private final ConfigurationFileDAO configurationFileDAO;
    private String baseUrl;
    private final EventService eventService;


    @Inject
    public ConfigurationDAO(ConfigurationMapper mapper,
                            ApplicationMapper applicationMapper,
                            ApplicationSettingDAO applicationSettingDAO,
                            ConfigurationFileDAO configurationFileDAO,
                            @Named("base.url") String baseUrl,
                            EventService eventService) {
        this.mapper = mapper;
        this.applicationMapper = applicationMapper;
        this.applicationSettingDAO = applicationSettingDAO;
        this.configurationFileDAO = configurationFileDAO;
        this.baseUrl = baseUrl;
        this.eventService = eventService;
        log.info("Base URL: " + baseUrl);
    }

    public List<Configuration> getAllConfigurations() {
        return getListWithCurrentUser(currentUser -> this.mapper.getAllConfigurations(currentUser.getCustomerId(), currentUser.getId()));
    }

    public List<Configuration> getAllConfigurationsByValue(String value) {
        return getListWithCurrentUser(currentUser ->
                this.mapper.getAllConfigurationsByValue(currentUser.getCustomerId(), "%" + value + "%", currentUser.getId()));
    }

    public Configuration getConfigurationByName(String name) {
        return getSingleRecord(customerId -> this.mapper.getConfigurationByName(customerId, name));
    }

    public void insertConfiguration(Configuration config) {
        insertRecord(config, configuration -> {
            this.mapper.insertConfiguration(configuration);
            if (configuration.getApplications().size() > 0) {
                configuration.getApplications().forEach(app -> app.setRemove(app.getAction() == 2));
                this.mapper.insertConfigurationApplications(configuration.getId(), configuration.getApplications());
            }
            if (configuration.getApplicationSettings() != null && !configuration.getApplicationSettings().isEmpty()) {
                this.mapper.insertConfigurationApplicationSettings(configuration.getId(), configuration.getApplicationSettings());
            }
            if (configuration.getApplicationUsageParameters() != null && !configuration.getApplicationUsageParameters().isEmpty()) {
                this.mapper.saveConfigurationApplicationUsageParameters(configuration.getId(), configuration.getApplicationUsageParameters());
            }

            final List<ConfigurationFile> files = configuration.getFiles();
            if (files != null && !files.isEmpty()) {
                this.mapper.insertConfigurationFiles(configuration.getId(), files);
            }
        });
    }

    @Transactional
    public void updateConfiguration(Configuration config) {
        updateRecord(
                config,
                configuration -> {
                    this.mapper.updateConfiguration(configuration);
                    this.mapper.removeConfigurationApplicationsById(configuration.getId());
                    if (configuration.getApplications().size() > 0) {
                        configuration.getApplications().forEach(app -> app.setRemove(app.getAction() == 2));
                        this.mapper.insertConfigurationApplications(configuration.getId(), configuration.getApplications());
                    }

                    this.applicationMapper.recheckConfigurationMainApplication(configuration.getId());
                    this.applicationMapper.recheckConfigurationContentApplication(configuration.getId());
                    // #6159: When updating the configuration via configuration editor page the state of Kiosk Mode flag
                    // is specified explicitly by the submitted form value, so there is no need to re-check the state of
                    // that flag based on presence of content app in configuration
//                    this.applicationMapper.recheckConfigurationKioskModes(configuration.getCustomerId());

                    this.mapper.removeConfigurationApplicationSettingsById(configuration.getId());
                    if (configuration.getApplicationSettings() != null && !configuration.getApplicationSettings().isEmpty()) {
                        this.mapper.insertConfigurationApplicationSettings(configuration.getId(), configuration.getApplicationSettings());
                    }
                    if (configuration.getApplicationUsageParameters() != null && !configuration.getApplicationUsageParameters().isEmpty()) {
                        this.mapper.saveConfigurationApplicationUsageParameters(configuration.getId(), configuration.getApplicationUsageParameters());
                    }

                    List<ConfigurationFile> legacyFiles = this.configurationFileDAO.getConfigurationFiles(configuration.getId());
                    Map<Integer,ConfigurationFile> legacyFilesMap = new HashMap<Integer, ConfigurationFile>();
                    for (ConfigurationFile file : legacyFiles) {
                        legacyFilesMap.put(file.getId(), file);
                    }

                    this.mapper.removeConfigurationFilesById(configuration.getId());
                    final List<ConfigurationFile> files = configuration.getFiles();
                    if (files != null && !files.isEmpty()) {
                        files.stream()
                                .filter(file -> file.getExternalUrl() != null)
                                .forEach(file -> {
                                    try {
                                        ConfigurationFile legacyFile = legacyFilesMap.get(file.getId());
                                        if (legacyFile != null && file.getExternalUrl().equals(legacyFile.getExternalUrl())) {
                                            file.setChecksum(legacyFile.getChecksum());
                                        } else {
                                            final String checksum = CryptoUtil.calculateChecksum(new URL(file.getExternalUrl()).openStream());
                                            file.setChecksum(checksum);
                                        }
                                    } catch (NoSuchAlgorithmException | IOException e) {
                                        log.error("Failed to calculate checksum for content URL: {}", file.getExternalUrl(), e);
                                        file.setChecksum("");
                                    }
                                });
                        this.mapper.insertConfigurationFiles(configuration.getId(), files);
                    }

                    // Deprecated and not used any more
/*                    final List<Integer> filesToRemove = config.getFilesToRemove();
                    if (filesToRemove != null) {
                        filesToRemove.forEach(fileId -> {
                            this.configurationFileDAO.removeFileFromDisk(fileId);
                        });
                    } */

                    this.eventService.fireEvent(new ConfigurationUpdatedEvent(configuration.getId()));
                },
                SecurityException::onConfigurationAccessViolation
        );
    }

    @Transactional
    public void removeConfigurationById(Integer id) {
        long count = this.mapper.countDevices(id);
        if (count > 0) {
            throw new ConfigurationReferenceExistsException(id, "devices");
        }

        updateById(
                id,
                this.mapper::getConfigurationById,
                configuration -> this.mapper.removeConfigurationById(configuration.getId()),
                SecurityException::onConfigurationAccessViolation
        );
    }

    public List<Application> getConfigurationApplications(Integer id) {
        return getLinkedList(
                id,
                this.mapper::getConfigurationById,
                customerId -> this.mapper.getConfigurationApplications(customerId, id, "ca" + CryptoUtil.randomHexString(8)),
                SecurityException::onConfigurationAccessViolation
        );
    }

    @Transactional
    public List<Application> getPlainConfigurationApplications(Integer id) {
        String tblName = "ca" + CryptoUtil.randomHexString(8);
        mapper.createTempConfigAppTable(tblName, id);
        return getLinkedList(
                id,
                this.mapper::getConfigurationById,
                customerId -> this.mapper.getPlainConfigurationApplications(customerId, tblName, id),
                SecurityException::onConfigurationAccessViolation
        );
    }

    public boolean isAppInstalledInConfiguration(String pkg, Integer configurationId) {
        return this.mapper.isAppInstalledInConfiguration(pkg, configurationId);
    }

    public Configuration getConfigurationById(Integer id) {
        return getSingleRecord(() -> this.mapper.getConfigurationById(id), SecurityException::onConfigurationAccessViolation);
    }

    @Transactional
    public Configuration getConfigurationByIdFull(Integer id) {
        final Configuration configuration = getSingleRecord(() -> this.mapper.getConfigurationById(id), SecurityException::onConfigurationAccessViolation);
        if (configuration != null) {
            final List<ApplicationSetting> appSettings = this.applicationSettingDAO.getApplicationSettingsByConfigurationId(id);
            configuration.setApplicationSettings(appSettings);

            final List<ConfigurationApplicationParameters> applicationParameters = this.mapper.getApplicationParameters(id);
            configuration.setApplicationUsageParameters(applicationParameters);

            final List<ConfigurationFile> files = this.configurationFileDAO.getConfigurationFiles(id);
            configuration.setFiles(files);

        }

        return configuration;
    }

    /**
     * <p>Upgrades the useage of specified application by specified configuration to most recent version available for
     * application.</p>
     *
     * @param configurationId an ID of a configuration to upgrade application version for.
     * @param applicationId an ID of application to upgrade.
     */
    @Transactional
    public void upgradeConfigurationApplication(Integer configurationId, Integer applicationId) {
        updateLinkedData(configurationId,
                this.mapper::getConfigurationById,
                configuration -> {
                    this.mapper.upgradeConfigurationApplication(configuration.getId(), applicationId);
                    log.debug("Upgraded application #{} to most recent version for configuration #{}",
                            applicationId, configurationId);
                },
                SecurityException::onConfigurationAccessViolation
        );
    }

    // Moved baseUrl here from resources due to a weird Guice issue (bug?):
    // the resource singleton initializes multiple times and (for DeviceResource)
    // baseUrl is injected incorrectly (either an empty string or a wrong context parameter)
    // This is apparently due to call the constructor from a background thread
    // Nevermind, looks like injection of baseUrl in this DAO object works well!
    public String getBaseUrl() {
        return baseUrl;
    }
}
