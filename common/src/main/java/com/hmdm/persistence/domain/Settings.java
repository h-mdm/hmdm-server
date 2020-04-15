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

import static com.hmdm.persistence.domain.DesktopHeader.NO_HEADER;
import static com.hmdm.persistence.domain.IconSize.SMALL;

@ApiModel(description = "The settings for MDM web application")
@JsonIgnoreProperties(ignoreUnknown = true)
public class Settings implements CustomerData, Serializable {

    private static final long serialVersionUID = -7584080480340396129L;

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

    // A language used for localization
    @ApiModelProperty("A flag indicating if browser-dependent language is to be used for content localization")
    private boolean useDefaultLanguage = true;
    @ApiModelProperty("A combination of language and country codes used for content localization (e.g. 'en_US')")
    private String language;
    @ApiModelProperty("Flag indicating if the new devices must be created on first access")
    private boolean createNewDevices = false;
    @ApiModelProperty("Default group for the new devices")
    private Integer newDeviceGroupId;
    @ApiModelProperty("Default configuration for the new devices")
    private Integer newDeviceConfigurationId;
    @ApiModelProperty("Phone number format")
    private String phoneNumberFormat;

    // This property is not stored in the database, it is a transient field used by the Settings resource
    @ApiModelProperty(hidden = true)
    private boolean singleCustomer;

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

    public boolean isCreateNewDevices() {
        return createNewDevices;
    }

    public void setCreateNewDevices(boolean createNewDevices) {
        this.createNewDevices = createNewDevices;
    }

    public Integer getNewDeviceGroupId() {
        return newDeviceGroupId;
    }

    public void setNewDeviceGroupId(Integer newDeviceGroupId) {
        this.newDeviceGroupId = newDeviceGroupId;
    }

    public Integer getNewDeviceConfigurationId() {
        return newDeviceConfigurationId;
    }

    public void setNewDeviceConfigurationId(Integer newDeviceConfigurationId) {
        this.newDeviceConfigurationId = newDeviceConfigurationId;
    }

    public String getPhoneNumberFormat() {
        return phoneNumberFormat;
    }

    public void setPhoneNumberFormat(String phoneNumberFormat) {
        this.phoneNumberFormat = phoneNumberFormat;
    }

    public boolean isSingleCustomer() {
        return singleCustomer;
    }

    public void setSingleCustomer(boolean singleCustomer) {
        this.singleCustomer = singleCustomer;
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
                ", useDefaultLanguage=" + useDefaultLanguage +
                ", language='" + language + '\'' +
                ", createNewDevices=" + createNewDevices +
                ", newDeviceGroupId=" + newDeviceGroupId +
                ", newDeviceConfigurationId=" + newDeviceConfigurationId +
                ", singleCustomer=" + singleCustomer +
                '}';
    }
}
