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
import org.codehaus.jackson.annotate.JsonIgnoreProperties;

import java.io.Serializable;

import static com.hmdm.persistence.domain.DesktopHeader.NO_HEADER;
import static com.hmdm.persistence.domain.IconSize.SMALL;

@ApiModel(description = "The settings for MDM web application")
@JsonIgnoreProperties(ignoreUnknown = true)
public class Settings implements CustomerData, Serializable {

    private static final long serialVersionUID = 6021629734180050883L;

    @ApiModelProperty("An ID of a settings record")
    private Integer id;

    // This group of settings corresponds to Default Design
    @ApiModelProperty("A background color for Default Design of mobile application")
    private String backgroundColor;
    @ApiModelProperty("A text color for Default Design of mobile application")
    private String textColor;
    @ApiModelProperty("An URL for background image color for Default Design of mobile application")
    private String backgroundImageUrl;
    @ApiModelProperty("A size of the icons for Default Design of mobile application")
    private IconSize iconSize = SMALL;
    @ApiModelProperty("A type of desktop header for Default Design of mobile application")
    private DesktopHeader desktopHeader = NO_HEADER;
    @ApiModelProperty(hidden = true)
    private int customerId;

    // This group of settings corresponds to Displayed Device Columns from Common Settings
    @ApiModelProperty("A flag indicating if Device Status column to be displayed in MDM web application")
    private Boolean columnDisplayedDeviceStatus = Boolean.TRUE;
    @ApiModelProperty("A flag indicating if Device Update Date column to be displayed in MDM web application")
    private Boolean columnDisplayedDeviceDate = Boolean.TRUE;
    @ApiModelProperty("A flag indicating if Device Nummber column to be displayed in MDM web application")
    private Boolean columnDisplayedDeviceNumber;
    @ApiModelProperty("A flag indicating if Device Model column to be displayed in MDM web application")
    private Boolean columnDisplayedDeviceModel;
    @ApiModelProperty("A flag indicating if Device Permissions column to be displayed in MDM web application")
    private Boolean columnDisplayedDevicePermissionsStatus = Boolean.TRUE;
    @ApiModelProperty("A flag indicating if Device Apps column to be displayed in MDM web application")
    private Boolean columnDisplayedDeviceAppInstallStatus = Boolean.TRUE;
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

    // A language used for localization
    @ApiModelProperty("A flag indicating if browser-dependent language is to be used for content localization")
    private boolean useDefaultLanguage = true;
    @ApiModelProperty("A combination of language and country codes used for content localization (e.g. 'en_US')")
    private String language;

    public Settings() {
    }

    public String getBackgroundColor() {
        return this.backgroundColor;
    }

    public void setBackgroundColor(String backgroundColor) {
        this.backgroundColor = backgroundColor;
    }

    public String getTextColor() {
        return this.textColor;
    }

    public void setTextColor(String textColor) {
        this.textColor = textColor;
    }

    public String getBackgroundImageUrl() {
        return this.backgroundImageUrl;
    }

    public void setBackgroundImageUrl(String backgroundImageUrl) {
        this.backgroundImageUrl = backgroundImageUrl;
    }

    public Integer getId() {
        return this.id;
    }

    public void setId(Integer id) {
        this.id = id;
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

    public Boolean getColumnDisplayedDeviceConfiguration() {
        return columnDisplayedDeviceConfiguration;
    }

    public void setColumnDisplayedDeviceConfiguration(Boolean columnDisplayedDeviceConfiguration) {
        this.columnDisplayedDeviceConfiguration = columnDisplayedDeviceConfiguration;
    }

    public IconSize getIconSize() {
        return iconSize;
    }

    public void setIconSize(IconSize iconSize) {
        this.iconSize = iconSize;
    }

    public DesktopHeader getDesktopHeader() {
        return desktopHeader;
    }

    public void setDesktopHeader(DesktopHeader desktopHeader) {
        this.desktopHeader = desktopHeader;
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

    public int getCustomerId() {
        return customerId;
    }

    public void setCustomerId(int customerId) {
        this.customerId = customerId;
    }

    public String getLanguage() {
        return language;
    }

    public void setLanguage(String language) {
        this.language = language;
    }

    public boolean isUseDefaultLanguage() {
        return useDefaultLanguage;
    }

    public void setUseDefaultLanguage(boolean useDefaultLanguage) {
        this.useDefaultLanguage = useDefaultLanguage;
    }

    public Boolean getColumnDisplayedLauncherVersion() {
        return columnDisplayedLauncherVersion;
    }

    public void setColumnDisplayedLauncherVersion(Boolean columnDisplayedLauncherVersion) {
        this.columnDisplayedLauncherVersion = columnDisplayedLauncherVersion;
    }

    @Override
    public String toString() {
        return "Settings{" +
                "id=" + id +
                ", backgroundColor='" + backgroundColor + '\'' +
                ", textColor='" + textColor + '\'' +
                ", backgroundImageUrl='" + backgroundImageUrl + '\'' +
                ", iconSize=" + iconSize +
                ", desktopHeader=" + desktopHeader +
                ", customerId=" + customerId +
                ", columnDisplayedDeviceStatus=" + columnDisplayedDeviceStatus +
                ", columnDisplayedDeviceDate=" + columnDisplayedDeviceDate +
                ", columnDisplayedDeviceNumber=" + columnDisplayedDeviceNumber +
                ", columnDisplayedDeviceModel=" + columnDisplayedDeviceModel +
                ", columnDisplayedDevicePermissionsStatus=" + columnDisplayedDevicePermissionsStatus +
                ", columnDisplayedDeviceAppInstallStatus=" + columnDisplayedDeviceAppInstallStatus +
                ", columnDisplayedDeviceConfiguration=" + columnDisplayedDeviceConfiguration +
                ", columnDisplayedDeviceImei=" + columnDisplayedDeviceImei +
                ", columnDisplayedDevicePhone=" + columnDisplayedDevicePhone +
                ", columnDisplayedDeviceDesc=" + columnDisplayedDeviceDesc +
                ", columnDisplayedDeviceGroup=" + columnDisplayedDeviceGroup +
                ", columnDisplayedLauncherVersion=" + columnDisplayedLauncherVersion +
                ", useDefaultLanguage=" + useDefaultLanguage +
                ", language='" + language + '\'' +
                '}';
    }
}
