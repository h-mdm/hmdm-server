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

import com.hmdm.rest.json.LookupItem;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.io.Serializable;
import java.util.List;

/**
 * <p>A domain object representing a single rule for device log.</p>
 *
 * @author isv
 */
@JsonIgnoreProperties(ignoreUnknown = true)
@ApiModel(description = "A single rule for device log")
public abstract class DeviceLogRule implements Serializable {

    private static final long serialVersionUID = -6168623766690469626L;
    @ApiModelProperty(value = "A name of the rule", required = true)
    private String name;

    @ApiModelProperty(value = "A flag indicating if rule is active", required = true)
    private boolean active;

    @ApiModelProperty(value = "An ID referencing the application", required = true)
    private Integer applicationId;

    @ApiModelProperty("A severity level")
    private LogLevel severity;

    @ApiModelProperty("A filter for log rule")
    private String filter;

    @ApiModelProperty(value = "An ID referencing the device group", required = true)
    private Integer groupId;

    @ApiModelProperty(value = "An ID referencing the configuration", required = true)
    private Integer configurationId;

    @ApiModelProperty("A package ID for application")
    private String applicationPkg;

    @ApiModelProperty("A name of the device group")
    private String groupName;

    @ApiModelProperty("A name of the configuration")
    private String configurationName;

    @ApiModelProperty("A list of devices related to rules")
    private List<LookupItem> devices;

    /**
     * <p>Constructs new <code>DeviceLogRule</code> instance. This implementation does nothing.</p>
     */
    public DeviceLogRule() {
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public Integer getApplicationId() {
        return applicationId;
    }

    public void setApplicationId(Integer applicationId) {
        this.applicationId = applicationId;
    }

    public LogLevel getSeverity() {
        return severity;
    }

    public void setSeverity(LogLevel severity) {
        this.severity = severity;
    }

    public String getFilter() {
        return filter;
    }

    public void setFilter(String filter) {
        this.filter = filter;
    }

    public Integer getGroupId() {
        return groupId;
    }

    public void setGroupId(Integer groupId) {
        this.groupId = groupId;
    }

    public Integer getConfigurationId() {
        return configurationId;
    }

    public void setConfigurationId(Integer configurationId) {
        this.configurationId = configurationId;
    }

    public String getApplicationPkg() {
        return applicationPkg;
    }

    public void setApplicationPkg(String applicationPkg) {
        this.applicationPkg = applicationPkg;
    }

    public String getGroupName() {
        return groupName;
    }

    public void setGroupName(String groupName) {
        this.groupName = groupName;
    }

    public String getConfigurationName() {
        return configurationName;
    }

    public void setConfigurationName(String configurationName) {
        this.configurationName = configurationName;
    }

    public List<LookupItem> getDevices() {
        return devices;
    }

    public void setDevices(List<LookupItem> devices) {
        this.devices = devices;
    }

    /**
     * <p>Gets the unique identifier for this record within underlying persistence layer.</p>
     *
     * @return an identifier for this record.
     */
    public abstract String getIdentifier();

    @Override
    public String toString() {
        return "DeviceLogRule{" +
                "name='" + name + '\'' +
                ", active=" + active +
                ", applicationId=" + applicationId +
                ", severity=" + severity +
                ", filter='" + filter + '\'' +
                ", groupId=" + groupId +
                ", configurationId=" + configurationId +
                ", applicationPkg='" + applicationPkg + '\'' +
                ", groupName='" + groupName + '\'' +
                ", configurationName='" + configurationName + '\'' +
                ", devices=" + devices +
                '}';
    }
}
