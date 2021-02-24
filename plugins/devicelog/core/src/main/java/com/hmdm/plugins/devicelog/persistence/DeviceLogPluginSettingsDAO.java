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

package com.hmdm.plugins.devicelog.persistence;

import com.hmdm.persistence.domain.Customer;
import com.hmdm.plugins.devicelog.model.DeviceLogPluginSettings;
import com.hmdm.plugins.devicelog.model.DeviceLogRule;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * <p>A DAO for {@link com.hmdm.plugins.devicelog.model.DeviceLogPluginSettings} domain objects.</p>
 *
 * @author isv
 */
public interface DeviceLogPluginSettingsDAO {

    DeviceLogPluginSettings getPluginSettings();

    DeviceLogPluginSettings getDefaultSettings();

    public DeviceLogPluginSettings getDefaultSettings(Customer customer);

    void insertPluginSettings(DeviceLogPluginSettings settings);

    void insertPluginSettings(DeviceLogPluginSettings settings, Customer customer);

    void updatePluginSettings(DeviceLogPluginSettings settings);

    void savePluginSettingsRule(DeviceLogRule rule);

    void savePluginSettingsRule(DeviceLogPluginSettings settings, DeviceLogRule rule);

    DeviceLogPluginSettings getPluginSettings(int customerId);

    Class<? extends DeviceLogPluginSettings> getSettingsClass();

    Class<? extends DeviceLogRule> getSettingsRuleClass();

    DeviceLogRule getPluginSettingsRuleById(int id);

    void deletePluginSettingRule(int id);

}
