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

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.hmdm.rest.json.LookupItem;
import io.swagger.v3.oas.annotations.media.Schema;
import java.io.Serializable;
import java.util.List;

@Schema(description = "A device registered to MDM server and running the MDM mobile application")
@JsonIgnoreProperties(ignoreUnknown = true)
public class Device implements CustomerData, Serializable {

    private static final long serialVersionUID = 8429040669592571351L;

    @Schema(description = "An ID of device")
    private Integer id;

    @Schema(description = "An unique textual identifier of device")
    private String number;

    @Schema(description = "A description of device")
    private String description;

    @Schema(description = "A date of last synchronization of device state")
    private Long lastUpdate;

    @Schema(description = "An ID of configuration for device")
    private Integer configurationId;

    @Schema(hidden = true)
    @Deprecated
    private Integer oldConfigurationId;

    @Schema(description = "An info on device state submitted by device to MDM server")
    private String info;

    @Schema(description = "An IMEM of device")
    private String imei;

    @Schema(description = "A phone number of device")
    private String phone;

    @Schema(hidden = true)
    private int customerId;

    @Schema(description = "A date of last IMEI change")
    private Long imeiUpdateTs;

    @Schema(description = "Public IP address")
    private String publicIp;

    @Schema(description = "Custom property #1")
    private String custom1;

    @Schema(description = "Custom property #2")
    private String custom2;

    @Schema(description = "Custom property #3")
    private String custom3;

    // Many-to-many relations
    @Schema(description = "A list of groups assigned to device")
    private List<LookupItem> groups;

    // Helper fields, not persisted
    @Schema(hidden = true)
    private List<Integer> ids;

    @Schema(hidden = true)
    private Configuration configuration;

    @Schema(hidden = true)
    private String configName;

    @Schema(hidden = true)
    @Deprecated
    private String oldConfigName;

    @Schema(hidden = true)
    private boolean applied;

    @Schema(hidden = true)
    private String launcherVersion;

    @Schema(hidden = true)
    private String launcherPkg;

    @Schema(hidden = true)
    private String statusCode;

    @Schema(description = "Old device number, used when the number is changed")
    private String oldNumber;

    @Schema(description = "Last characters of the device number used for fast search")
    private String fastSearch;

    @Schema(hidden = true)
    private Boolean mdmMode;

    @Schema(hidden = true)
    private Boolean kioskMode;

    @Schema(hidden = true)
    private String androidVersion;

    @Schema(hidden = true)
    private Long enrollTime;

    @Schema(hidden = true)
    private String serial;

    public Device() {}

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

    public String getPublicIp() {
        return publicIp;
    }

    public void setPublicIp(String publicIp) {
        this.publicIp = publicIp;
    }

    public String getCustom1() {
        return custom1;
    }

    public void setCustom1(String custom1) {
        this.custom1 = custom1;
    }

    public String getCustom2() {
        return custom2;
    }

    public void setCustom2(String custom2) {
        this.custom2 = custom2;
    }

    public String getCustom3() {
        return custom3;
    }

    public void setCustom3(String custom3) {
        this.custom3 = custom3;
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

    public String getOldNumber() {
        return oldNumber;
    }

    public void setOldNumber(String oldNumber) {
        this.oldNumber = oldNumber;
    }

    public Boolean getMdmMode() {
        return mdmMode;
    }

    public void setMdmMode(Boolean mdmMode) {
        this.mdmMode = mdmMode;
    }

    public Boolean getKioskMode() {
        return kioskMode;
    }

    public void setKioskMode(Boolean kioskMode) {
        this.kioskMode = kioskMode;
    }

    public String getAndroidVersion() {
        return androidVersion;
    }

    public void setAndroidVersion(String androidVersion) {
        this.androidVersion = androidVersion;
    }

    public Long getEnrollTime() {
        return enrollTime;
    }

    public void setEnrollTime(Long enrollTime) {
        this.enrollTime = enrollTime;
    }

    public String getSerial() {
        return serial;
    }

    public void setSerial(String serial) {
        this.serial = serial;
    }

    public String getFastSearch() {
        return fastSearch;
    }

    public void setFastSearch(String fastSearch) {
        this.fastSearch = fastSearch;
    }

    public void updateFastSearch(int chars) {
        if (number == null) {
            fastSearch = null;
            return;
        }
        if (number.length() <= chars) {
            fastSearch = number;
            return;
        }
        fastSearch = number.substring(number.length() - chars, number.length());
    }

    @Override
    public String toString() {
        return "Device{" + "id=" + id + ", number='" + number + '\'' + ", description='" + description + '\''
                + ", lastUpdate=" + lastUpdate + ", configurationId=" + configurationId + ", oldConfigurationId="
                + oldConfigurationId + ", info='" + info + '\'' + ", imei='" + imei + '\'' + ", phone='" + phone + '\''
                + ", customerId=" + customerId + ", imeiUpdateTs=" + imeiUpdateTs + ", publicIp=" + publicIp
                + ", custom1=" + custom1 + ", custom2=" + custom2 + ", custom3=" + custom3 + ", groups=" + groups
                + ", ids=" + ids + ", configuration=" + configuration + ", configName='" + configName + '\''
                + ", oldConfigName='" + oldConfigName + '\'' + ", applied=" + applied + ", launcherPkg='" + launcherPkg
                + '\'' + ", launcherVersion='" + launcherVersion + '\'' + ", mdmMode='" + mdmMode + '\''
                + ", kioskMode='" + kioskMode + '\'' + ", androidVersion='" + androidVersion + '\'' + ", enrollTime='"
                + enrollTime + '\'' + ", serial='" + serial + '\'' + ", statusCode='" + statusCode + '\''
                + ", oldNumber='" + oldNumber + '\'' + ", fastSearch='" + fastSearch + '\'' + '}';
    }
}
