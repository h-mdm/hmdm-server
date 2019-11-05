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

package com.hmdm.plugins.devicelog.rest.json;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.hmdm.plugins.devicelog.model.DeviceLogRule;
import com.hmdm.plugins.devicelog.model.LogLevel;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.io.Serializable;

/**
 * <p>A DTO carrying the details on a single device log rule applicable to target device.</p>
 *
 * @author isv
 */
@ApiModel(description = "The parameters for filtering the lists of device log record objects")
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_EMPTY)
public class AppliedDeviceLogRule implements Serializable {

    private static final long serialVersionUID = -1856203581931832489L;

    @ApiModelProperty("A package ID for application")
    private String packageId;

    @ApiModelProperty("A severity level")
    private int logLevel;

    @ApiModelProperty("A filter for log rule")
    private String filter;

    /**
     * <p>Constructs new <code>AppliedDeviceLogRule</code> instance. This implementation does nothing.</p>
     */
    public AppliedDeviceLogRule() {
    }

    public AppliedDeviceLogRule(DeviceLogRule rule) {
        this.packageId = rule.getApplicationPkg();
        this.logLevel = rule.getSeverity() == null ? 0 : rule.getSeverity().getId();
        this.filter = rule.getFilter();
    }

    public String getPackageId() {
        return packageId;
    }

    public void setPackageId(String packageId) {
        this.packageId = packageId;
    }

    public int getLogLevel() {
        return logLevel;
    }

    public void setLogLevel(int logLevel) {
        this.logLevel = logLevel;
    }

    public String getFilter() {
        return filter;
    }

    public void setFilter(String filter) {
        this.filter = filter;
    }

    @Override
    public String toString() {
        return "AppliedDeviceLogRule{" +
                "packageId='" + packageId + '\'' +
                ", logLevel=" + logLevel +
                ", filter='" + filter + '\'' +
                '}';
    }
}
