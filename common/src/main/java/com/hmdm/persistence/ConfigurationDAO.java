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
import java.util.List;

import com.hmdm.persistence.domain.Application;
import com.hmdm.persistence.domain.ApplicationSetting;
import com.hmdm.persistence.domain.Configuration;
import com.hmdm.persistence.mapper.ApplicationMapper;
import com.hmdm.persistence.domain.ConfigurationApplicationParameters;
import com.hmdm.persistence.mapper.ConfigurationMapper;
import com.hmdm.security.SecurityException;
import org.mybatis.guice.transactional.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ConfigurationDAO extends AbstractLinkedDAO<Configuration, Application> {

    private static final Logger log = LoggerFactory.getLogger(ConfigurationDAO.class);

    private final ConfigurationMapper mapper;
    private final ApplicationMapper applicationMapper;

    private final ApplicationSettingDAO applicationSettingDAO;

    @Inject
    public ConfigurationDAO(ConfigurationMapper mapper,
                            ApplicationMapper applicationMapper, ApplicationSettingDAO applicationSettingDAO) {
        this.mapper = mapper;
        this.applicationMapper = applicationMapper;
        this.applicationSettingDAO = applicationSettingDAO;
    }

    public List<Configuration> getAllConfigurationsByType(int type) {
        return getList(customerId -> this.mapper.getAllConfigurationsByType(customerId, type));
    }

    public List<Configuration> getAllConfigurationsByTypeAndValue(int type, String value) {
        return getList(customerId -> this.mapper.getAllConfigurationsByTypeAndValue(customerId, type, "%" + value + "%"));
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
                customerId -> this.mapper.getConfigurationApplications(customerId, id),
                SecurityException::onConfigurationAccessViolation
        );
    }

    public List<Application> getPlainConfigurationApplications(Integer id) {
        return getLinkedList(
                id,
                this.mapper::getConfigurationById,
                customerId -> this.mapper.getPlainConfigurationApplications(customerId, id),
                SecurityException::onConfigurationAccessViolation
        );
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
}
