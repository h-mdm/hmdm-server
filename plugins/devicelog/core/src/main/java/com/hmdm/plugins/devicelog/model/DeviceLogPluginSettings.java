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

package com.hmdm.plugins.devicelog.model;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.io.Serializable;
import java.util.List;

/**
 * <p>A domain object representing a single collection of <code>Device Log</code> plugin settings per customer account.
 * </p>
 *
 * @author isv
 */
@JsonIgnoreProperties(ignoreUnknown = true)
@ApiModel(description = "A collection of 'Device Log' plugin settings")
public abstract class DeviceLogPluginSettings implements Serializable {

    private static final long serialVersionUID = -4806434589453165014L;

    @ApiModelProperty(value = "A period for preserving the log records in persistent data store (in days)", required = true)
    private int logsPreservePeriod = 3;

    @ApiModelProperty(value = "A list of device log rules", required = true)
    private List<DeviceLogRule> rules;

    /**
     * <p>Constructs new <code>DeviceLogPluginSettings</code> instance. This implementation does nothing.</p>
     */
    public DeviceLogPluginSettings() {
    }

    public int getLogsPreservePeriod() {
        return logsPreservePeriod;
    }

    public void setLogsPreservePeriod(int logsPreservePeriod) {
        this.logsPreservePeriod = logsPreservePeriod;
    }

    public List<DeviceLogRule> getRules() {
        return rules;
    }

    public void setRules(List<DeviceLogRule> rules) {
        this.rules = rules;
    }

    /**
     * <p>Gets the unique identifier for this record within underlying persistence layer.</p>
     *
     * @return an identifier for this record.
     */
    public abstract String getIdentifier();

}
