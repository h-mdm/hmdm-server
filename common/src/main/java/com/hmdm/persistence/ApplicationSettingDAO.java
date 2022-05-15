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
import com.hmdm.persistence.domain.ApplicationSetting;
import com.hmdm.persistence.mapper.ApplicationSettingMapper;

import java.util.List;

/**
 * <p>$</p>
 *
 * @author isv
 */
@Singleton
public class ApplicationSettingDAO {

    private final ApplicationSettingMapper mapper;

    /**
     * <p>Constructs new <code>ApplicationSettingDAO</code> instance. This implementation does nothing.</p>
     */
    @Inject
    public ApplicationSettingDAO(ApplicationSettingMapper mapper) {
        this.mapper = mapper;
    }

    public List<ApplicationSetting> getApplicationSettingsByConfigurationId(int id) {
        return this.mapper.getApplicationSettingsByConfigurationId(id);
    }

    public List<ApplicationSetting> getApplicationSettingsByDeviceId(int deviceId) {
        return this.mapper.getApplicationSettingsByDeviceId(deviceId);
    }

    public List<ApplicationSetting> getConfigAppSettings(int configId, String pkg) {
        return this.mapper.getConfigAppSettings(configId, pkg);
    }

    public List<ApplicationSetting> getDeviceAppSettings(int deviceId, String pkg) {
        return this.mapper.getDeviceAppSettings(deviceId, pkg);
    }

    public void insertApplicationSetting(int configurationId, ApplicationSetting setting) {
        this.mapper.deleteApplicationSettingByName(configurationId, setting.getApplicationId(), setting.getName());
        this.mapper.insertApplicationSetting(configurationId, setting);
    }

    public void deleteApplicationSetting(int configurationId, int applicationId, String name) {
        this.mapper.deleteApplicationSettingByName(configurationId, applicationId, name);
    }

    public void deleteApplicationSettingByApp(int configurationId, int applicationId) {
        this.mapper.deleteApplicationSettingByApp(configurationId, applicationId);
    }
}
