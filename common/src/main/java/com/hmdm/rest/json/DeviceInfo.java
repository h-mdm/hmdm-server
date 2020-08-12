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

package com.hmdm.rest.json;

import java.io.Serializable;
import java.util.LinkedList;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.hmdm.persistence.domain.Application;

@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
@ApiModel(description = "The details related to a single device. Such details are sent from the MDM mobile application " +
        "to MDM server")
public class DeviceInfo implements Serializable {

    private static final long serialVersionUID = -3973746261808927823L;

    @ApiModelProperty("A name of the device model")
    private String model;

    @ApiModelProperty("A list of permissions set for device")
    private List<Integer> permissions = new LinkedList<>();

    @ApiModelProperty("A list of applications installed on device")
    private List<Application> applications = new LinkedList<>();

    @ApiModelProperty("A list of configuraiton files installed on device")
    private List<DeviceConfigurationFile> files = new LinkedList<>();

    @ApiModelProperty("An identifier of device within MDM server")
    private String deviceId;

    @ApiModelProperty("An IMEI identifier")
    private String imei;

    @ApiModelProperty("A phone number")
    private String phone;

    @ApiModelProperty(value = "A battery level in percents", allowableValues = "range[0, 100]")
    private Integer batteryLevel;

    @ApiModelProperty(value = "A battery charge type", allowableValues = "usb,ac", allowEmptyValue = true)
    private String batteryCharging;

    @ApiModelProperty(value = "Android OS version")
    private String androidVersion;

    @ApiModelProperty(value = "A flag indicating if MDM mode is ON or not")
    private Boolean mdmMode;

    @ApiModelProperty(value = "The details on device location")
    private DeviceLocation location;

    @ApiModelProperty(value = "Headwind MDM launcher build variant")
    private String launcherType;

    @ApiModelProperty(value = "Package of default launcher on the device")
    private String launcherPackage;

    @ApiModelProperty(value = "Is Headwind MDM a default launcher")
    private Boolean defaultLauncher;

    public DeviceInfo() {
    }

    public String getModel() {
        return this.model;
    }

    public void setModel(String model) {
        this.model = model;
    }

    public List<Integer> getPermissions() {
        return this.permissions;
    }

    public void setPermissions(List<Integer> permissions) {
        this.permissions = permissions;
    }

    public List<Application> getApplications() {
        return this.applications;
    }

    public void setApplications(List<Application> applications) {
        this.applications = applications;
    }

    public String getDeviceId() {
        return this.deviceId;
    }

    public void setDeviceId(String deviceId) {
        this.deviceId = deviceId;
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

    public Integer getBatteryLevel() {
        return batteryLevel;
    }

    public void setBatteryLevel(Integer batteryLevel) {
        this.batteryLevel = batteryLevel;
    }

    public String getBatteryCharging() {
        return batteryCharging;
    }

    public void setBatteryCharging(String batteryCharging) {
        this.batteryCharging = batteryCharging;
    }

    public String getAndroidVersion() {
        return androidVersion;
    }

    public void setAndroidVersion(String androidVersion) {
        this.androidVersion = androidVersion;
    }

    public Boolean getMdmMode() {
        return mdmMode;
    }

    public void setMdmMode(Boolean mdmMode) {
        this.mdmMode = mdmMode;
    }

    public DeviceLocation getLocation() {
        return location;
    }

    public void setLocation(DeviceLocation location) {
        this.location = location;
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

    public List<DeviceConfigurationFile> getFiles() {
        return files;
    }

    public void setFiles(List<DeviceConfigurationFile> files) {
        this.files = files;
    }

    @Override
    public String toString() {
        return "DeviceInfo{" +
                "model='" + model + '\'' +
                ", permissions=" + permissions +
                ", applications=" + applications +
                ", files=" + files +
                ", deviceId='" + deviceId + '\'' +
                ", imei='" + imei + '\'' +
                ", phone='" + phone + '\'' +
                ", batteryLevel=" + batteryLevel +
                ", batteryCharging='" + batteryCharging + '\'' +
                ", androidVersion='" + androidVersion + '\'' +
                ", mdmMode='" + mdmMode + '\'' +
                ", location='" + location + '\'' +
                ", launcherType='" + launcherType + '\'' +
                ", launcherPackage='" + launcherPackage + '\'' +
                ", defaultLauncher='" + defaultLauncher + '\'' +
                '}';
    }
}
