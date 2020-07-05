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

package com.hmdm.persistence.domain;

import java.io.Serializable;
import java.util.List;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.hmdm.rest.json.LookupItem;

@ApiModel(description = "A device registered to MDM server and running the MDM mobile application")
@JsonIgnoreProperties(ignoreUnknown = true)
public class Device implements CustomerData, Serializable {

    private static final long serialVersionUID = 8429040669592571351L;

    @ApiModelProperty("An ID of device")
    private Integer id;
    @ApiModelProperty("An unique textual identifier of device")
    private String number;
    @ApiModelProperty("A description of device")
    private String description;
    @ApiModelProperty("A date of last synchronization of device state")
    private Long lastUpdate;
    @ApiModelProperty("An ID of configuration for device")
    private Integer configurationId;
    @ApiModelProperty(hidden = true)
    @Deprecated
    private Integer oldConfigurationId;
    @ApiModelProperty("An info on device state submitted by device to MDM server")
    private String info;
    @ApiModelProperty("An IMEM of device")
    private String imei;
    @ApiModelProperty("A phone number of device")
    private String phone;
    @ApiModelProperty(hidden = true)
    private int customerId;
    @ApiModelProperty("A date of last IMEI change")
    private Long imeiUpdateTs;

    // Many-to-many relations
    @ApiModelProperty("A list of groups assigned to device")
    private List<LookupItem> groups;

    // Helper fields, not persisted
    @ApiModelProperty(hidden = true)
    private List<Integer> ids;
    @ApiModelProperty(hidden = true)
    private Configuration configuration;
    @ApiModelProperty(hidden = true)
    private String configName;
    @ApiModelProperty(hidden = true)
    @Deprecated
    private String oldConfigName;
    @ApiModelProperty(hidden = true)
    private boolean applied;
    @ApiModelProperty(hidden = true)
    private String launcherVersion;
    @ApiModelProperty(hidden = true)
    private String launcherPkg;
    @ApiModelProperty(hidden = true)
    private String statusCode;

    public Device() {
    }

    public Integer getId() {
        return this.id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getNumber() {
        return this.number;
    }

    public void setNumber(String number) {
        this.number = number;
    }

    public String getDescription() {
        return this.description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Long getLastUpdate() {
        return this.lastUpdate;
    }

    public void setLastUpdate(Long lastUpdate) {
        this.lastUpdate = lastUpdate;
    }

    public Configuration getConfiguration() {
        return this.configuration;
    }

    public void setConfiguration(Configuration configuration) {
        this.configuration = configuration;
    }

    public List<Integer> getIds() {
        return this.ids;
    }

    public void setIds(List<Integer> ids) {
        this.ids = ids;
    }

    @Deprecated
    public String getOldConfigName() {
        return this.oldConfigName;
    }

    @Deprecated
    public void setOldConfigName(String oldConfigName) {
        this.oldConfigName = oldConfigName;
    }

    public Integer getConfigurationId() {
        return this.configurationId;
    }

    public void setConfigurationId(Integer configurationId) {
        this.configurationId = configurationId;
    }

    @Deprecated
    public Integer getOldConfigurationId() {
        return this.oldConfigurationId;
    }

    @Deprecated
    public void setOldConfigurationId(Integer oldConfigurationId) {
        this.oldConfigurationId = oldConfigurationId;
    }

    public String getConfigName() {
        return this.configName;
    }

    public void setConfigName(String configName) {
        this.configName = configName;
    }

    public boolean isApplied() {
        return this.applied;
    }

    public void setApplied(boolean applied) {
        this.applied = applied;
    }

    public String getInfo() {
        return this.info;
    }

    public void setInfo(String info) {
        this.info = info;
    }

    public String getImei() {
        return imei;
    }

    public void setImei(String imei) {
        this.imei = imei;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    @Override
    public int getCustomerId() {
        return customerId;
    }

    @Override
    public void setCustomerId(int customerId) {
        this.customerId = customerId;
    }

    public Long getImeiUpdateTs() {
        return imeiUpdateTs;
    }

    public void setImeiUpdateTs(Long imeiChangeTs) {
        this.imeiUpdateTs = imeiChangeTs;
    }

    public List<LookupItem> getGroups() {
        return groups;
    }

    public void setGroups(List<LookupItem> groups) {
        this.groups = groups;
    }

    public String getLauncherVersion() {
        return launcherVersion;
    }

    public void setLauncherVersion(String launcherVersion) {
        this.launcherVersion = launcherVersion;
    }

    public String getLauncherPkg() {
        return launcherPkg;
    }

    public void setLauncherPkg(String launcherPkg) {
        this.launcherPkg = launcherPkg;
    }

    public String getStatusCode() {
        return statusCode;
    }

    public void setStatusCode(String statusCode) {
        this.statusCode = statusCode;
    }

    @Override
    public String toString() {
        return "Device{" +
                "id=" + id +
                ", number='" + number + '\'' +
                ", description='" + description + '\'' +
                ", lastUpdate=" + lastUpdate +
                ", configurationId=" + configurationId +
                ", oldConfigurationId=" + oldConfigurationId +
                ", info='" + info + '\'' +
                ", imei='" + imei + '\'' +
                ", phone='" + phone + '\'' +
                ", customerId=" + customerId +
                ", groups=" + groups +
                ", ids=" + ids +
                ", configuration=" + configuration +
                ", configName='" + configName + '\'' +
                ", oldConfigName='" + oldConfigName + '\'' +
                ", applied=" + applied +
                ", launcherPkg='" + launcherPkg + '\'' +
                ", launcherVersion='" + launcherVersion + '\'' +
                ", statusCode='" + statusCode + '\'' +
                '}';
    }
}
