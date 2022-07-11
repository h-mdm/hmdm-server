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

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.io.Serializable;

@ApiModel(description = "The settings for single user role at customer level")
@JsonIgnoreProperties(ignoreUnknown = true)
public class UserRoleSettings implements CustomerData, Serializable {

    private static final long serialVersionUID = -3149133764385852478L;

    @ApiModelProperty("An ID of a settings record")
    private Integer id;

    @ApiModelProperty(hidden = true)
    private int customerId;
    
    @ApiModelProperty("An ID of a user role")
    private int roleId;


    // This group of settings corresponds to Displayed Device Columns from Common Settings
    @ApiModelProperty("A flag indicating if Device Status column to be displayed in MDM web application")
    private Boolean columnDisplayedDeviceStatus = Boolean.TRUE;
    @ApiModelProperty("A flag indicating if Device Update Date column to be displayed in MDM web application")
    private Boolean columnDisplayedDeviceDate = Boolean.TRUE;
    @ApiModelProperty("A flag indicating if Device Nummber column to be displayed in MDM web application")
    private Boolean columnDisplayedDeviceNumber = Boolean.TRUE;
    @ApiModelProperty("A flag indicating if Device Model column to be displayed in MDM web application")
    private Boolean columnDisplayedDeviceModel;
    @ApiModelProperty("A flag indicating if Device Permissions column to be displayed in MDM web application")
    private Boolean columnDisplayedDevicePermissionsStatus = Boolean.TRUE;
    @ApiModelProperty("A flag indicating if Device Apps column to be displayed in MDM web application")
    private Boolean columnDisplayedDeviceAppInstallStatus = Boolean.TRUE;
    @ApiModelProperty("A flag indicating if Device Files column to be displayed in MDM web application")
    private Boolean columnDisplayedDeviceFilesStatus = Boolean.TRUE;
    @ApiModelProperty("A flag indicating if Device Configuration column to be displayed in MDM web application")
    private Boolean columnDisplayedDeviceConfiguration = Boolean.TRUE;
    @ApiModelProperty("A flag indicating if Device IMEI column to be displayed in MDM web application")
    private Boolean columnDisplayedDeviceImei;
    @ApiModelProperty("A flag indicating if Device Phone column to be displayed in MDM web application")
    private Boolean columnDisplayedDevicePhone;
    @ApiModelProperty("A flag indicating if Device Description column to be displayed in MDM web application")
    private Boolean columnDisplayedDeviceDesc;
    @ApiModelProperty("A flag indicating if Device Group column to be displayed in MDM web application")
    private Boolean columnDisplayedDeviceGroup;
    @ApiModelProperty("A flag indicating if Launcher Version column to be displayed in MDM web application")
    private Boolean columnDisplayedLauncherVersion;
    @ApiModelProperty("A flag indicating if Battery Level column to be displayed in MDM web application")
    private Boolean columnDisplayedBatteryLevel;
    @ApiModelProperty("A flag indicating if the default launcher column to be displayed in MDM web application")
    private Boolean columnDisplayedDefaultLauncher;
    @ApiModelProperty("A flag indicating if MDM mode column to be displayed in MDM web application")
    private Boolean columnDisplayedMdmMode;
    @ApiModelProperty("A flag indicating if kiosk mode column to be displayed in MDM web application")
    private Boolean columnDisplayedKioskMode;
    @ApiModelProperty("A flag indicating if Android version column to be displayed in MDM web application")
    private Boolean columnDisplayedAndroidVersion;
    @ApiModelProperty("A flag indicating if enrollment date column to be displayed in MDM web application")
    private Boolean columnDisplayedEnrollmentDate;
    @ApiModelProperty("A flag indicating if serial number column to be displayed in MDM web application")
    private Boolean columnDisplayedSerial;
    @ApiModelProperty("A flag indicating if custom property #1 column must be displayed in the Devices table")
    private Boolean columnDisplayedCustom1;
    @ApiModelProperty("A flag indicating if custom property #2 column must be displayed in the Devices table")
    private Boolean columnDisplayedCustom2;
    @ApiModelProperty("A flag indicating if custom property #3 column must be displayed in the Devices table")
    private Boolean columnDisplayedCustom3;

    /**
     * <p>Constructs new <code>UserRoleSettings</code> instance. This implementation does nothing.</p>
     */
    public UserRoleSettings() {
    }

    public int getRoleId() {
        return roleId;
    }

    public void setRoleId(int roleId) {
        this.roleId = roleId;
    }

    public int getCustomerId() {
        return customerId;
    }

    public void setCustomerId(int customerId) {
        this.customerId = customerId;
    }

    public Boolean getColumnDisplayedDeviceStatus() {
        return columnDisplayedDeviceStatus;
    }

    public void setColumnDisplayedDeviceStatus(Boolean columnDisplayedDeviceStatus) {
        this.columnDisplayedDeviceStatus = columnDisplayedDeviceStatus;
    }

    public Boolean getColumnDisplayedDeviceDate() {
        return columnDisplayedDeviceDate;
    }

    public void setColumnDisplayedDeviceDate(Boolean columnDisplayedDeviceDate) {
        this.columnDisplayedDeviceDate = columnDisplayedDeviceDate;
    }

    public Boolean getColumnDisplayedDeviceNumber() {
        return columnDisplayedDeviceNumber;
    }

    public void setColumnDisplayedDeviceNumber(Boolean columnDisplayedDeviceNumber) {
        this.columnDisplayedDeviceNumber = columnDisplayedDeviceNumber;
    }

    public Boolean getColumnDisplayedDeviceModel() {
        return columnDisplayedDeviceModel;
    }

    public void setColumnDisplayedDeviceModel(Boolean columnDisplayedDeviceModel) {
        this.columnDisplayedDeviceModel = columnDisplayedDeviceModel;
    }

    public Boolean getColumnDisplayedDevicePermissionsStatus() {
        return columnDisplayedDevicePermissionsStatus;
    }

    public void setColumnDisplayedDevicePermissionsStatus(Boolean columnDisplayedDevicePermissionsStatus) {
        this.columnDisplayedDevicePermissionsStatus = columnDisplayedDevicePermissionsStatus;
    }

    public Boolean getColumnDisplayedDeviceAppInstallStatus() {
        return columnDisplayedDeviceAppInstallStatus;
    }

    public void setColumnDisplayedDeviceAppInstallStatus(Boolean columnDisplayedDeviceAppInstallStatus) {
        this.columnDisplayedDeviceAppInstallStatus = columnDisplayedDeviceAppInstallStatus;
    }

    public Boolean getColumnDisplayedDeviceFilesStatus() {
        return columnDisplayedDeviceFilesStatus;
    }

    public void setColumnDisplayedDeviceFilesStatus(Boolean columnDisplayedDeviceFilesStatus) {
        this.columnDisplayedDeviceFilesStatus = columnDisplayedDeviceFilesStatus;
    }

    public Boolean getColumnDisplayedDeviceConfiguration() {
        return columnDisplayedDeviceConfiguration;
    }

    public void setColumnDisplayedDeviceConfiguration(Boolean columnDisplayedDeviceConfiguration) {
        this.columnDisplayedDeviceConfiguration = columnDisplayedDeviceConfiguration;
    }

    public Boolean getColumnDisplayedDeviceImei() {
        return columnDisplayedDeviceImei;
    }

    public void setColumnDisplayedDeviceImei(Boolean columnDisplayedDeviceImei) {
        this.columnDisplayedDeviceImei = columnDisplayedDeviceImei;
    }

    public Boolean getColumnDisplayedDevicePhone() {
        return columnDisplayedDevicePhone;
    }

    public void setColumnDisplayedDevicePhone(Boolean columnDisplayedDevicePhone) {
        this.columnDisplayedDevicePhone = columnDisplayedDevicePhone;
    }

    public Boolean getColumnDisplayedDeviceDesc() {
        return columnDisplayedDeviceDesc;
    }

    public void setColumnDisplayedDeviceDesc(Boolean columnDisplayedDeviceDesc) {
        this.columnDisplayedDeviceDesc = columnDisplayedDeviceDesc;
    }

    public Boolean getColumnDisplayedDeviceGroup() {
        return columnDisplayedDeviceGroup;
    }

    public void setColumnDisplayedDeviceGroup(Boolean columnDisplayedDeviceGroup) {
        this.columnDisplayedDeviceGroup = columnDisplayedDeviceGroup;
    }

    public Boolean getColumnDisplayedLauncherVersion() {
        return columnDisplayedLauncherVersion;
    }

    public void setColumnDisplayedLauncherVersion(Boolean columnDisplayedLauncherVersion) {
        this.columnDisplayedLauncherVersion = columnDisplayedLauncherVersion;
    }

    public Boolean getColumnDisplayedBatteryLevel() {
        return columnDisplayedBatteryLevel;
    }

    public void setColumnDisplayedBatteryLevel(Boolean columnDisplayedBatteryLevel) {
        this.columnDisplayedBatteryLevel = columnDisplayedBatteryLevel;
    }

    public Boolean getColumnDisplayedDefaultLauncher() {
        return columnDisplayedDefaultLauncher;
    }

    public void setColumnDisplayedDefaultLauncher(Boolean columnDisplayedDefaultLauncher) {
        this.columnDisplayedDefaultLauncher = columnDisplayedDefaultLauncher;
    }

    public Boolean getColumnDisplayedMdmMode() {
        return columnDisplayedMdmMode;
    }

    public void setColumnDisplayedMdmMode(Boolean columnDisplayedMdmMode) {
        this.columnDisplayedMdmMode = columnDisplayedMdmMode;
    }

    public Boolean getColumnDisplayedKioskMode() {
        return columnDisplayedKioskMode;
    }

    public void setColumnDisplayedKioskMode(Boolean columnDisplayedKioskMode) {
        this.columnDisplayedKioskMode = columnDisplayedKioskMode;
    }

    public Boolean getColumnDisplayedAndroidVersion() {
        return columnDisplayedAndroidVersion;
    }

    public void setColumnDisplayedAndroidVersion(Boolean columnDisplayedAndroidVersion) {
        this.columnDisplayedAndroidVersion = columnDisplayedAndroidVersion;
    }

    public Boolean getColumnDisplayedEnrollmentDate() {
        return columnDisplayedEnrollmentDate;
    }

    public void setColumnDisplayedEnrollmentDate(Boolean columnDisplayedEnrollmentDate) {
        this.columnDisplayedEnrollmentDate = columnDisplayedEnrollmentDate;
    }

    public Boolean getColumnDisplayedSerial() {
        return columnDisplayedSerial;
    }

    public void setColumnDisplayedSerial(Boolean columnDisplayedSerial) {
        this.columnDisplayedSerial = columnDisplayedSerial;
    }

    public Boolean getColumnDisplayedCustom1() {
        return columnDisplayedCustom1;
    }

    public void setColumnDisplayedCustom1(Boolean columnDisplayedCustom1) {
        this.columnDisplayedCustom1 = columnDisplayedCustom1;
    }

    public Boolean getColumnDisplayedCustom2() {
        return columnDisplayedCustom2;
    }

    public void setColumnDisplayedCustom2(Boolean columnDisplayedCustom2) {
        this.columnDisplayedCustom2 = columnDisplayedCustom2;
    }

    public Boolean getColumnDisplayedCustom3() {
        return columnDisplayedCustom3;
    }

    public void setColumnDisplayedCustom3(Boolean columnDisplayedCustom3) {
        this.columnDisplayedCustom3 = columnDisplayedCustom3;
    }

    @Override
    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    @Override
    public String toString() {
        return "UserRoleSettings{" +
                "id=" + id +
                ", customerId=" + customerId +
                ", roleId=" + roleId +
                ", columnDisplayedDeviceStatus=" + columnDisplayedDeviceStatus +
                ", columnDisplayedDeviceDate=" + columnDisplayedDeviceDate +
                ", columnDisplayedDeviceNumber=" + columnDisplayedDeviceNumber +
                ", columnDisplayedDeviceModel=" + columnDisplayedDeviceModel +
                ", columnDisplayedDevicePermissionsStatus=" + columnDisplayedDevicePermissionsStatus +
                ", columnDisplayedDeviceAppInstallStatus=" + columnDisplayedDeviceAppInstallStatus +
                ", columnDisplayedDeviceFilesStatus=" + columnDisplayedDeviceFilesStatus +
                ", columnDisplayedDeviceConfiguration=" + columnDisplayedDeviceConfiguration +
                ", columnDisplayedDeviceImei=" + columnDisplayedDeviceImei +
                ", columnDisplayedDevicePhone=" + columnDisplayedDevicePhone +
                ", columnDisplayedDeviceDesc=" + columnDisplayedDeviceDesc +
                ", columnDisplayedDeviceGroup=" + columnDisplayedDeviceGroup +
                ", columnDisplayedLauncherVersion=" + columnDisplayedLauncherVersion +
                ", columnDisplayedBatteryLevel=" + columnDisplayedBatteryLevel +
                ", columnDisplayedDefaultLauncher=" + columnDisplayedDefaultLauncher +
                ", columnDisplayedMdmMode=" + columnDisplayedMdmMode +
                ", columnDisplayedKioskMode=" + columnDisplayedKioskMode +
                ", columnDisplayedAndroidVersion=" + columnDisplayedAndroidVersion +
                ", columnDisplayedEnrollmentDate=" + columnDisplayedEnrollmentDate +
                ", columnDisplayedSerial=" + columnDisplayedSerial +
                ", columnDisplayedCustom1=" + columnDisplayedCustom1 +
                ", columnDisplayedCustom2=" + columnDisplayedCustom2 +
                ", columnDisplayedCustom3=" + columnDisplayedCustom3 +
                '}';
    }
}
