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
    @ApiModelProperty("Desktop header template for Default Design of mobile application")
    private String desktopHeaderTemplate;
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
    @ApiModelProperty("Custom property name 1")
    private String customPropertyName1;
    @ApiModelProperty("Custom property name 2")
    private String customPropertyName2;
    @ApiModelProperty("Custom property name 3")
    private String customPropertyName3;
    @ApiModelProperty("Is custom property 1 multiline")
    private boolean customMultiline1;
    @ApiModelProperty("Is custom property 2 multiline")
    private boolean customMultiline2;
    @ApiModelProperty("Is custom property 3 multiline")
    private boolean customMultiline3;
    @ApiModelProperty("Send custom property 1 to device")
    private boolean customSend1;
    @ApiModelProperty("Send custom property 2 to device")
    private boolean customSend2;
    @ApiModelProperty("Send custom property 3 to device")
    private boolean customSend3;
    @ApiModelProperty("Send description to device")
    private boolean sendDescription;
    @ApiModelProperty("Request password reset to new users")
    private boolean passwordReset;
    @ApiModelProperty("Minimal password length for users")
    private int passwordLength;
    @ApiModelProperty("Password strength for users (0 - none, 1 - alphanumeric, 2 - alphanumeric + special characters")
    private int passwordStrength;

    // This property is not stored in the database, it is a transient field used by the Settings resource
    @ApiModelProperty(hidden = true)
    private boolean singleCustomer;

    // Customer settings stored in the customers table (default for single customer)
    @ApiModelProperty(hidden = true)
    private int accountType;
    @ApiModelProperty(hidden = true)
    private Long expiryTime;
    @ApiModelProperty(hidden = true)
    private int deviceLimit;
    @ApiModelProperty(hidden = true)
    private int deviceCount;

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

    public String getDesktopHeaderTemplate() {
        return desktopHeaderTemplate;
    }

    public void setDesktopHeaderTemplate(String desktopHeaderTemplate) {
        this.desktopHeaderTemplate = desktopHeaderTemplate;
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

    public String getCustomPropertyName1() {
        return customPropertyName1;
    }

    public void setCustomPropertyName1(String customPropertyName1) {
        this.customPropertyName1 = customPropertyName1;
    }

    public String getCustomPropertyName2() {
        return customPropertyName2;
    }

    public void setCustomPropertyName2(String customPropertyName2) {
        this.customPropertyName2 = customPropertyName2;
    }

    public String getCustomPropertyName3() {
        return customPropertyName3;
    }

    public void setCustomPropertyName3(String customPropertyName3) {
        this.customPropertyName3 = customPropertyName3;
    }

    public boolean isCustomMultiline1() {
        return customMultiline1;
    }

    public void setCustomMultiline1(boolean customMultiline1) {
        this.customMultiline1 = customMultiline1;
    }

    public boolean isCustomMultiline2() {
        return customMultiline2;
    }

    public void setCustomMultiline2(boolean customMultiline2) {
        this.customMultiline2 = customMultiline2;
    }

    public boolean isCustomMultiline3() {
        return customMultiline3;
    }

    public void setCustomMultiline3(boolean customMultiline3) {
        this.customMultiline3 = customMultiline3;
    }

    public boolean isCustomSend1() {
        return customSend1;
    }

    public void setCustomSend1(boolean customSend1) {
        this.customSend1 = customSend1;
    }

    public boolean isCustomSend2() {
        return customSend2;
    }

    public void setCustomSend2(boolean customSend2) {
        this.customSend2 = customSend2;
    }

    public boolean isCustomSend3() {
        return customSend3;
    }

    public void setCustomSend3(boolean customSend3) {
        this.customSend3 = customSend3;
    }

    public boolean isSendDescription() {
        return sendDescription;
    }

    public void setSendDescription(boolean sendDescription) {
        this.sendDescription = sendDescription;
    }

    public boolean isPasswordReset() {
        return passwordReset;
    }

    public void setPasswordReset(boolean passwordReset) {
        this.passwordReset = passwordReset;
    }

    public int getPasswordLength() {
        return passwordLength;
    }

    public void setPasswordLength(int passwordLength) {
        this.passwordLength = passwordLength;
    }

    public int getPasswordStrength() {
        return passwordStrength;
    }

    public void setPasswordStrength(int passwordStrength) {
        this.passwordStrength = passwordStrength;
    }

    public boolean isSingleCustomer() {
        return singleCustomer;
    }

    public void setSingleCustomer(boolean singleCustomer) {
        this.singleCustomer = singleCustomer;
    }

    public int getAccountType() {
        return accountType;
    }

    public void setAccountType(int accountType) {
        this.accountType = accountType;
    }

    public Long getExpiryTime() {
        return expiryTime;
    }

    public void setExpiryTime(Long expiryTime) {
        this.expiryTime = expiryTime;
    }

    public int getDeviceLimit() {
        return deviceLimit;
    }

    public void setDeviceLimit(int deviceLimit) {
        this.deviceLimit = deviceLimit;
    }

    public int getDeviceCount() {
        return deviceCount;
    }

    public void setDeviceCount(int deviceCount) {
        this.deviceCount = deviceCount;
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
                ", phoneNumberFormat='" + phoneNumberFormat + '\'' +
                ", customPropertyName1='" + customPropertyName1 + '\'' +
                ", customPropertyName2='" + customPropertyName2 + '\'' +
                ", customPropertyName3='" + customPropertyName3 + '\'' +
                ", createNewDevices=" + createNewDevices +
                ", newDeviceGroupId=" + newDeviceGroupId +
                ", newDeviceConfigurationId=" + newDeviceConfigurationId +
                ", singleCustomer=" + singleCustomer +
                ", accountType=" + accountType +
                ", expiryTime=" + expiryTime +
                ", deviceLimit=" + deviceLimit +
                ", deviceCount=" + deviceCount +
                '}';
    }
}
