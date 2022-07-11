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

    @ApiModelProperty(value = "A flag indicating if kiosk mode is ON or not")
    private Boolean kioskMode;

    @ApiModelProperty(value = "The details on device location")
    private DeviceLocation location;

    @ApiModelProperty(value = "Headwind MDM launcher build variant")
    private String launcherType;

    @ApiModelProperty(value = "Package of default launcher on the device")
    private String launcherPackage;

    @ApiModelProperty(value = "Is Headwind MDM a default launcher")
    private Boolean defaultLauncher;

    @ApiModelProperty("ICC ID")
    private String iccid;

    @ApiModelProperty("an IMSI identifier")
    private String imsi;

    @ApiModelProperty("An IMEI identifier for 2nd SIM slot")
    private String imei2;

    @ApiModelProperty("A phone number for 2nd SIM slot")
    private String phone2;

    @ApiModelProperty("ICC ID for 2nd SIM slot")
    private String iccid2;

    @ApiModelProperty("an IMSI identifier for 2nd SIM slot")
    private String imsi2;

    @ApiModelProperty("A device serial number")
    private String serial;

    @ApiModelProperty("CPU architecture")
    private String cpu;

    @ApiModelProperty(value = "Custom property #1")
    private String custom1;

    @ApiModelProperty(value = "Custom property #2")
    private String custom2;

    @ApiModelProperty(value = "Custom property #3")
    private String custom3;

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

    public Boolean getKioskMode() {
        return kioskMode;
    }

    public void setKioskMode(Boolean kioskMode) {
        this.kioskMode = kioskMode;
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
                ", kioskMode='" + kioskMode + '\'' +
                ", location='" + location + '\'' +
                ", launcherType='" + launcherType + '\'' +
                ", launcherPackage='" + launcherPackage + '\'' +
                ", imei2='" + imei2 + '\'' +
                ", phone2='" + phone2 + '\'' +
                ", imsi='" + imsi + '\'' +
                ", iccid='" + iccid + '\'' +
                ", imsi2='" + imsi2 + '\'' +
                ", iccid2='" + iccid2 + '\'' +
                ", serial='" + serial + '\'' +
                ", cpu='" + cpu + '\'' +
                ", custom1='" + custom1 + '\'' +
                ", custom2='" + custom2 + '\'' +
                ", custom3='" + custom3 + '\'' +
                '}';
    }
}
