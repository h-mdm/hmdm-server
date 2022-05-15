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

package com.hmdm.plugins.deviceinfo.persistence;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.hmdm.persistence.AbstractDAO;
import com.hmdm.plugins.deviceinfo.persistence.domain.DeviceInfoPluginSettings;
import com.hmdm.plugins.deviceinfo.persistence.mapper.DeviceInfoMapper;

/**
 * <p>A DAO for {@link DeviceInfoPluginSettings} domain objects.</p>
 *
 * @author isv
 */
@Singleton
public class DeviceInfoSettingsDAO extends AbstractDAO<DeviceInfoPluginSettings> {

    /**
     * <p>An ORM mapper for domain object type.</p>
     */
    private final DeviceInfoMapper deviceInfoMapper;

    /**
     * <p>Constructs new <code>DeviceInfoSettingsDAO</code> instance. This implementation does nothing.</p>
     */
    @Inject
    public DeviceInfoSettingsDAO(DeviceInfoMapper deviceInfoMapper) {
        this.deviceInfoMapper = deviceInfoMapper;
    }

    /**
     * <p>Gets the plugin settings for the customer account associated with the current user.</p>
     *
     * @return plugin settings for current customer account or <code>null</code> if there are no such settings found.
     */
    public DeviceInfoPluginSettings getPluginSettings() {
        return getSingleRecord(this.deviceInfoMapper::findPluginSettingsByCustomerId);
    }

    /**
     * <p>Saves the specified plugin settings applying the current security context.</p>
     *
     * @param settings plugin settings to be saved.
     */
    public void savePluginSettings(DeviceInfoPluginSettings settings) {
        insertRecord(settings, this.deviceInfoMapper::savePluginSettings);
    }

    /**
     * <p>Gets the plugin settings for the specified customer account.</p>
     *
     * @return plugin settings for customer account or <code>null</code> if there are no such settings found.
     */
    public DeviceInfoPluginSettings getPluginSettings(int customerId) {
        return this.deviceInfoMapper.findPluginSettingsByCustomerId(customerId);
    }

    /**
     * <p>Saves the specified plugin settings without checking the user.
     * This function should not be used in REST methods, only in task methods.</p>
     *
     * @param settings plugin settings to be saved.
     */
    public void savePluginSettingsUnsecure(DeviceInfoPluginSettings settings) {
        this.deviceInfoMapper.savePluginSettings(settings);
    }
}
