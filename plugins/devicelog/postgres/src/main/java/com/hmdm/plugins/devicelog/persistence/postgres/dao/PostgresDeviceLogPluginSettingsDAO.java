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

package com.hmdm.plugins.devicelog.persistence.postgres.dao;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.hmdm.persistence.AbstractDAO;
import com.hmdm.persistence.domain.Customer;
import com.hmdm.plugins.devicelog.model.DeviceLogPluginSettings;
import com.hmdm.plugins.devicelog.model.DeviceLogRule;
import com.hmdm.plugins.devicelog.persistence.DeviceLogPluginSettingsDAO;
import com.hmdm.plugins.devicelog.persistence.postgres.dao.domain.PostgresDeviceLogPluginSettings;
import com.hmdm.plugins.devicelog.persistence.postgres.dao.domain.PostgresDeviceLogRule;
import com.hmdm.plugins.devicelog.persistence.postgres.dao.mapper.PostgresDeviceLogMapper;
import com.hmdm.rest.json.LookupItem;
import com.hmdm.security.SecurityException;
import org.mybatis.guice.transactional.Transactional;

import javax.validation.constraints.NotNull;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * <p>A DAO for device log plugin settings backed by the <code>Postgres</code> database.</p>
 *
 * @author isv
 */
@Singleton
public class PostgresDeviceLogPluginSettingsDAO extends AbstractDAO<PostgresDeviceLogPluginSettings>
        implements DeviceLogPluginSettingsDAO {

    /**
     * <p>An ORM mapper for domain object type.</p>
     */
    private final PostgresDeviceLogMapper mapper;

    /**
     * <p>Constructs new <code>PostgresDeviceLogPluginSettingsDAO</code> instance. This implementation does nothing.</p>
     */
    @Inject
    public PostgresDeviceLogPluginSettingsDAO(PostgresDeviceLogMapper mapper) {
        this.mapper = mapper;
    }

    @Override
    public DeviceLogPluginSettings getPluginSettings() {
        final PostgresDeviceLogPluginSettings settings = getSingleRecord(this.mapper::findPluginSettingsByCustomerId);
        if (settings != null && settings.getRules() != null && settings.getRules().size() == 1 && settings.getRules().get(0).getIdentifier() == null) {
            settings.setRules(new ArrayList<>());
        }
        return settings;
    }

    @Override
    public DeviceLogPluginSettings getDefaultSettings() {
        return new PostgresDeviceLogPluginSettings();
    }

    @Override
    public DeviceLogPluginSettings getDefaultSettings(Customer customer) {
        PostgresDeviceLogPluginSettings settings = new PostgresDeviceLogPluginSettings();
        settings.setCustomerId(customer.getId());
        return settings;
    }

    @Override
    public void insertPluginSettings(DeviceLogPluginSettings settings) {
        PostgresDeviceLogPluginSettings postgresSettings = (PostgresDeviceLogPluginSettings) settings;
        insertRecord(postgresSettings, this.mapper::insertPluginSettings);
    }

    @Override
    public void insertPluginSettings(DeviceLogPluginSettings settings, Customer customer) {
        PostgresDeviceLogPluginSettings postgresSettings = (PostgresDeviceLogPluginSettings) settings;
        postgresSettings.setCustomerId(customer.getId());
        this.mapper.insertPluginSettings(postgresSettings);
    }

    @Override
    public void updatePluginSettings(DeviceLogPluginSettings settings) {
        PostgresDeviceLogPluginSettings postgresSettings = (PostgresDeviceLogPluginSettings) settings;
        updateRecord(
                postgresSettings,
                this.mapper::updatePluginSettings,
                s -> SecurityException.onCustomerDataAccessViolation(s.getId(), "pluginDeviceLogSettings")
        );
    }

    @Override
    @Transactional
    public void savePluginSettingsRule(@NotNull DeviceLogRule rule) {
        savePluginSettingsRule(getPluginSettings(), rule);
    }

    @Override
    @Transactional
    public void savePluginSettingsRule(@NotNull DeviceLogPluginSettings settings, @NotNull DeviceLogRule rule) {
        PostgresDeviceLogPluginSettings postgresSettings = (PostgresDeviceLogPluginSettings) settings;
        if (postgresSettings != null) {
            PostgresDeviceLogRule postgresRule = (PostgresDeviceLogRule) rule;

            postgresRule.setSettingId(postgresSettings.getId());

            if (postgresRule.getId() == null) {
                this.mapper.insertPluginSettingsRule(postgresRule);
            } else {
                this.mapper.updatePluginSettingsRule(postgresRule);
            }


            this.mapper.deletePluginSettingsRuleDevices(postgresRule.getId());
            if (postgresRule.getDevices() != null && !postgresRule.getDevices().isEmpty()) {
                final List<Integer> deviceIds = postgresRule.getDevices()
                        .stream()
                        .map(LookupItem::getId)
                        .collect(Collectors.toList());
                this.mapper.insertPluginSettingsRuleDevices(postgresRule.getId(), deviceIds);
            }

        } else {
            throw new IllegalStateException("Device Log Plugin settings record is required to be created prior " +
                    "to saving the rule: " + rule);
        }
    }

    @Transactional
    public DeviceLogRule getPluginSettingsRuleById(int id) {
        return mapper.getPluginSettingsRule(id);
    }

    @Transactional
    public void deletePluginSettingRule(int id) {
        final PostgresDeviceLogPluginSettings settings = getSingleRecord(
                () -> this.mapper.getPluginSettingsByRuleIdForAuthorization(id),
                (s) -> SecurityException.onCustomerDataAccessViolation(id, "pluginDeviceLogRule")
        );

        if (settings != null) {
            this.mapper.deletePluginSettingRule(id);
        }
    }

    @Override
    public Class<? extends DeviceLogPluginSettings> getSettingsClass() {
        return PostgresDeviceLogPluginSettings.class;
    }

    @Override
    public Class<? extends DeviceLogRule> getSettingsRuleClass() {
        return PostgresDeviceLogRule.class;
    }

    @Override
    public DeviceLogPluginSettings getPluginSettings(int customerId) {
        final PostgresDeviceLogPluginSettings settings = this.mapper.findPluginSettingsByCustomerId(customerId);
        if (settings.getRules() != null && settings.getRules().size() == 1 && settings.getRules().get(0).getIdentifier() == null) {
            settings.setRules(new ArrayList<>());
        }

        return settings;
    }
}
