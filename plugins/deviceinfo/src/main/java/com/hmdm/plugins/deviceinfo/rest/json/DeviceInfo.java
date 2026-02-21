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

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonSetter;
import com.hmdm.rest.json.LookupItem;
import io.swagger.v3.oas.annotations.media.Schema;
import java.io.Serializable;
import java.util.List;

/**
 * <p>A DTO carrying the detailed info for a single device.</p>
 *
 * @author isv
 */
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class DeviceInfo implements Serializable {

    private static final long serialVersionUID = 1063145760140357201L;

    @Schema(description = "A device ID")
    private int id;

    @Schema(description = "An unique device number")
    private String deviceNumber;

    @Schema(description = "A device description")
    private String description;

    @Schema(description = "A required IMEI for device")
    private String imeiRequired;

    @Schema(description = "An actual IMEI for device")
    private String imeiActual;

    @Schema(description = "A required phone number for device")
    private String phoneNumberRequired;

    @Schema(description = "An actual phone number for device")
    private String phoneNumberActual;

    @Schema(description = "A device model")
    private String model;

    @Schema(description = "An OS version installed on device")
    private String osVersion;

    @Schema(description = "A battery level for device (in percents)")
    private Integer batteryLevel;

    @Schema(description = "A timestamp of most recent update of device info (in milliseconds since epoch time)")
    private Long latestUpdateTime;

    @Schema(description = "An interval passed from the most recent update of device info from current time")
    private Long latestUpdateInterval;

    @Schema(
            description = "A type of interval passed from the most recent update of device info from current time",
            allowableValues = "min,hour,day")
    private String latestUpdateIntervalType;

    @Schema(description = "A list of groups assigned to device")
    private List<LookupItem> groups;

    @Schema(description = "A flag indicating if admin permission is set on device")
    private Boolean adminPermission;

    @Schema(description = "A flag indicating if overlap permission is set on device")
    private Boolean overlapPermission;

    @Schema(description = "A flag indicating if history permission is set on device")
    private Boolean historyPermission;

    @Schema(description = "A flag indicating if accessibility permission is set on device")
    private Boolean accessibilityPermission;

    @Schema(description = "The most recent view of dynamic data for device")
    private DeviceDynamicInfoRecord latestDynamicData;

    @Schema(description = "A list of applications which already are installed or required to be installed on device")
    private List<DeviceInfoApplication> applications;

    @Schema(description = "A flag indicating if MDM mode is ON or not")
    private Boolean mdmMode;

    @Schema(description = "A flag indicating if kiosk mode is ON or not")
    private Boolean kioskMode;

    @Schema(description = "Headwind MDM launcher build variant")
    private String launcherType;

    @Schema(description = "Package of default launcher on the device")
    private String launcherPackage;

    @Schema(description = "Is Headwind MDM a default launcher")
    private Boolean defaultLauncher;

    @Schema(description = "ICC ID")
    private String iccid;

    @Schema(description = "an IMSI identifier")
    private String imsi;

    @Schema(description = "An IMEI identifier for 2nd SIM slot")
    private String imei2;

    @Schema(description = "A phone number for 2nd SIM slot")
    private String phone2;

    @Schema(description = "ICC ID for 2nd SIM slot")
    private String iccid2;

    @Schema(description = "an IMSI identifier for 2nd SIM slot")
    private String imsi2;

    @Schema(description = "A device serial number")
    private String serial;

    @Schema(description = "CPU architecture")
    private String cpu;

    /**
     * <p>Constructs new <code>DeviceInfo</code> instance. This implementation does nothing.</p>
     */
    public DeviceInfo() {}

    public void setId(int id) {
        this.id = id;
    }

    public void setDeviceNumber(String deviceNumber) {
        this.deviceNumber = deviceNumber;
    }

    @JsonSetter
    public void setLatestUpdateTime(Long latestUpdateTime) {
        this.latestUpdateTime = latestUpdateTime != null && latestUpdateTime == 0L ? null : latestUpdateTime;
        if (latestUpdateTime != null && latestUpdateTime != 0L) {
            long diff = (System.currentTimeMillis() - latestUpdateTime) / 1000 / 60;
            if (diff >= 24 * 60) {
                this.latestUpdateInterval = diff / 24 / 60;
                this.latestUpdateIntervalType = "day";
            } else if (diff >= 60) {
                this.latestUpdateInterval = diff / 60;
                this.latestUpdateIntervalType = "hour";
            } else {
                this.latestUpdateInterval = diff;
                this.latestUpdateIntervalType = "min";
            }
        } else {
            this.latestUpdateInterval = null;
            this.latestUpdateIntervalType = null;
        }
    }

    public int getId() {
        return id;
    }

    public String getDeviceNumber() {
        return deviceNumber;
    }

    public Long getLatestUpdateTime() {
        return latestUpdateTime;
    }

    public Long getLatestUpdateInterval() {
        return latestUpdateInterval;
    }

    public String getLatestUpdateIntervalType() {
        return latestUpdateIntervalType;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getImeiRequired() {
        return imeiRequired;
    }

    public void setImeiRequired(String imeiRequired) {
        this.imeiRequired = imeiRequired;
    }

    public String getImeiActual() {
        return imeiActual;
    }

    public void setImeiActual(String imeiActual) {
        this.imeiActual = imeiActual;
    }

    public String getPhoneNumberRequired() {
        return phoneNumberRequired;
    }

    public void setPhoneNumberRequired(String phoneNumberRequired) {
        this.phoneNumberRequired = phoneNumberRequired;
    }

    public String getPhoneNumberActual() {
        return phoneNumberActual;
    }

    public void setPhoneNumberActual(String phoneNumberActual) {
        this.phoneNumberActual = phoneNumberActual;
    }

    public String getModel() {
        return model;
    }

    public void setModel(String model) {
        this.model = model;
    }

    public String getOsVersion() {
        return osVersion;
    }

    public void setOsVersion(String osVersion) {
        this.osVersion = osVersion;
    }

    public Integer getBatteryLevel() {
        return batteryLevel;
    }

    public void setBatteryLevel(Integer batteryLevel) {
        this.batteryLevel = batteryLevel;
    }

    public List<LookupItem> getGroups() {
        return groups;
    }

    public void setGroups(List<LookupItem> groups) {
        this.groups = groups;
    }

    public Boolean getAdminPermission() {
        return adminPermission;
    }

    public void setAdminPermission(Boolean adminPermission) {
        this.adminPermission = adminPermission;
    }

    public Boolean getOverlapPermission() {
        return overlapPermission;
    }

    public void setOverlapPermission(Boolean overlapPermission) {
        this.overlapPermission = overlapPermission;
    }

    public Boolean getHistoryPermission() {
        return historyPermission;
    }

    public void setHistoryPermission(Boolean historyPermission) {
        this.historyPermission = historyPermission;
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

    public String getLauncherType() {
        return launcherType;
    }

    public void setLauncherType(String launcherType) {
        this.launcherType = launcherType;
    }

    public String getLauncherPackage() {
        return launcherPackage;
    }

    public void setLauncherPackage(String launcherPackage) {
        this.launcherPackage = launcherPackage;
    }

    public Boolean getDefaultLauncher() {
        return defaultLauncher;
    }

    public void setDefaultLauncher(Boolean defaultLauncher) {
        this.defaultLauncher = defaultLauncher;
    }

    public String getIccid() {
        return iccid;
    }

    public void setIccid(String iccid) {
        this.iccid = iccid;
    }

    public String getImsi() {
        return imsi;
    }

    public void setImsi(String imsi) {
        this.imsi = imsi;
    }

    public String getImei2() {
        return imei2;
    }

    public void setImei2(String imei2) {
        this.imei2 = imei2;
    }

    public String getPhone2() {
        return phone2;
    }

    public void setPhone2(String phone2) {
        this.phone2 = phone2;
    }

    public String getIccid2() {
        return iccid2;
    }

    public void setIccid2(String iccid2) {
        this.iccid2 = iccid2;
    }

    public String getImsi2() {
        return imsi2;
    }

    public void setImsi2(String imsi2) {
        this.imsi2 = imsi2;
    }

    public String getSerial() {
        return serial;
    }

    public void setSerial(String serial) {
        this.serial = serial;
    }

    public String getCpu() {
        return cpu;
    }

    public void setCpu(String cpu) {
        this.cpu = cpu;
    }

    public DeviceDynamicInfoRecord getLatestDynamicData() {
        return latestDynamicData;
    }

    public void setLatestDynamicData(DeviceDynamicInfoRecord latestDynamicData) {
        this.latestDynamicData = latestDynamicData;
    }

    public List<DeviceInfoApplication> getApplications() {
        return applications;
    }

    public void setApplications(List<DeviceInfoApplication> applications) {
        this.applications = applications;
    }
}
