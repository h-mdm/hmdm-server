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

package com.hmdm.plugins.deviceinfo.rest.json;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.hmdm.plugins.deviceinfo.persistence.domain.DeviceInfoPluginSettings;

/**
 * <p>A DTO carrying the plugin setting to be transmitted to device.</p>
 *
 * @author isv
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class DeviceSettings {

    /**
     * <p>Current settings for plugin.</p>
     */
    @JsonIgnore
    private final DeviceInfoPluginSettings pluginSettings;

    /**
     * <p>Constructs new <code>DeviceSettings</code> instance. This implementation does nothing.</p>
     */
    public DeviceSettings(DeviceInfoPluginSettings settings) {
        if (settings == null) {
            settings = new DeviceInfoPluginSettings();
        }
        this.pluginSettings = settings;
    }

    public int getIntervalMins() {
        return this.pluginSettings.getIntervalMins();
    }

    public boolean isSendData() {
        return this.pluginSettings.isSendData();
    }

}
