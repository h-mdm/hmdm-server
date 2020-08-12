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
import java.util.stream.Collectors;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.hmdm.persistence.domain.ConfigurationFile;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.hmdm.persistence.domain.Application;
import com.hmdm.persistence.domain.Configuration;
import com.hmdm.persistence.domain.Device;
import com.hmdm.persistence.domain.Settings;
import com.hmdm.util.CryptoUtil;

@ApiModel(description = "The details and settings for a single device used for configuring MDM mobile application")
@JsonIgnoreProperties(ignoreUnknown = true)
public class SyncResponse implements Serializable, SyncResponseInt {

    private static final long serialVersionUID = 7961923794459303328L;

    @ApiModelProperty("A background color to use when running MDM application")
    private String backgroundColor;

    @ApiModelProperty("A text color to use when running MDM application")
    private String textColor;

    @ApiModelProperty("An URL for background image to use when running MDM application")
    private String backgroundImageUrl;

    @ApiModelProperty("A size of the icons to use when running MDM application")
    private String iconSize;

    @ApiModelProperty("A type of location tracking")
    private String requestUpdates;

    @ApiModelProperty("Push notification options")
    private String pushOptions;

    @ApiModelProperty("Brightness management option")
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private Boolean autoBrightness;

    @ApiModelProperty("Brightness value (0-255)")
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private Integer brightness;

    @ApiModelProperty("Timeout management option")
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private Boolean manageTimeout;

    @ApiModelProperty("Timeout value (sec)")
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private Integer timeout;

    @ApiModelProperty("Volume lock option")
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private Boolean lockVolume;

    @ApiModelProperty("Password requirements for the mobile device")
    private String passwordMode;

    @ApiModelProperty("Orientation lock: 0 - none, 1 - portrait, 2 - landscape")
    private Integer orientation;

    @ApiModelProperty("Set to true if Headwind MDM need to work together with a third-party launcher")
    private Boolean runDefaultLauncher;

    @ApiModelProperty("Time zone settings: null for using default settings, auto for automatic time zone, or Olson time zone string")
    private String timeZone;

    @ApiModelProperty("Allowed classes, separated by comma")
    private String allowedClasses;

    @ApiModelProperty("A password for administrator of MDM application used on device")
    private String password;

    @ApiModelProperty("An IMEI of device")
    private String imei;

    @ApiModelProperty("A phone number of device")
    private String phone;

    @ApiModelProperty("A displayed title of the MDM application used on device")
    private String title;

    @ApiModelProperty("A list of applications to be used on device")
    private List<SyncApplicationInt> applications;

    @ApiModelProperty("A flag indicating if GPS is enabled on device")
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private Boolean gps;

    @ApiModelProperty("A flag indicating if Bluetooth is enabled on device")
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private Boolean bluetooth;

    @ApiModelProperty("A flag indicating if Wi-Fi is enabled on device")
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private Boolean wifi;

    @ApiModelProperty("A flag indicating if Mobile Data is enabled on device")
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private Boolean mobileData;

    @ApiModelProperty("A flag indicating if USB storage is enabled on device")
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private Boolean usbStorage;

    @ApiModelProperty("A flag indicating if MDM is operating in kiosk mode")
    private boolean kioskMode;

    @ApiModelProperty("Flag enabling Home button in kiosk mode")
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private Boolean kioskHome;

    @ApiModelProperty("Flag enabling Recents button in kiosk mode")
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private Boolean kioskRecents;

    @ApiModelProperty("Flag enabling notifications in kiosk mode")
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private Boolean kioskNotifications;

    @ApiModelProperty("Flag enabling system info in kiosk mode")
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private Boolean kioskSystemInfo;

    @ApiModelProperty("Flag enabling lock screen in kiosk mode")
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private Boolean kioskKeyguard;

    @ApiModelProperty("A flag indicating if status bar is locked")
    private boolean lockStatusBar;

    @ApiModelProperty("A package ID for the main application")
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String mainApp;

    @ApiModelProperty(value = "A system update type. 0-Default, 1-Immediately, 2-Scheduled, 3-Postponed", allowableValues = "0,1,2,3")
    private int systemUpdateType;

    @ApiModelProperty(value = "A start time for system update period formatted as HH:MM. (If system update time is 2)")
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String systemUpdateFrom;

    @ApiModelProperty(value = "A finish time for system update period formatted as HH:MM. (If system update time is 2)")
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String systemUpdateTo;

    @ApiModelProperty(value = "A list of application settings to apply on device")
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private List<SyncApplicationSettingInt> applicationSettings;

    @ApiModelProperty(value = "A list of files to apply on device")
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private List<SyncConfigurationFileInt> files;

    public SyncResponse() {
    }

    public SyncResponse(Settings settings, String password, List<Application> applications, Device device) {
        if (settings != null) {
            this.backgroundColor = settings.getBackgroundColor();
            this.textColor = settings.getTextColor();
            this.backgroundImageUrl = settings.getBackgroundImageUrl();
            this.iconSize = settings.getIconSize().getTransmittedValue();
            this.title = settings.getDesktopHeader().getTransmittedValue();
        }

        if (device != null) {
            this.imei = device.getImei();
            this.phone = device.getPhone();
        }

        this.password = CryptoUtil.getMD5String(password);
        this.applications = (
                applications != null ?
                        applications.stream().map(SyncApplication::new).collect(Collectors.toList())
                        : new LinkedList<>()
        );
    }

    public SyncResponse(Configuration settings, List<Application> applications, Device device) {
        if (settings != null) {
            this.backgroundColor = settings.getBackgroundColor();
            this.textColor = settings.getTextColor();
            this.backgroundImageUrl = settings.getBackgroundImageUrl();
            this.iconSize = settings.getIconSize().getTransmittedValue();
            this.title = settings.getDesktopHeader().getTransmittedValue();
        }

        if (device != null) {
            this.imei = device.getImei();
            this.phone = device.getPhone();
        }

        this.password = CryptoUtil.getMD5String(settings.getPassword());
        this.applications = (
                applications != null ?
                        applications.stream().map(SyncApplication::new).collect(Collectors.toList())
                        : new LinkedList<>()
        );
    }

    @Override
    public String getBackgroundColor() {
        return this.backgroundColor;
    }

    public void setBackgroundColor(String backgroundColor) {
        this.backgroundColor = backgroundColor;
    }

    @Override
    public String getTextColor() {
        return this.textColor;
    }

    public void setTextColor(String textColor) {
        this.textColor = textColor;
    }

    @Override
    public String getBackgroundImageUrl() {
        return this.backgroundImageUrl;
    }

    public void setBackgroundImageUrl(String backgroundImageUrl) {
        this.backgroundImageUrl = backgroundImageUrl;
    }

    @Override
    public List<SyncApplicationInt> getApplications() {
        return this.applications;
    }

    @Override
    public String getPassword() {
        return this.password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    @Override
    public String getImei() {
        return imei;
    }

    public void setImei(String imei) {
        this.imei = imei;
    }

    @Override
    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    @Override
    public String getIconSize() {
        return iconSize;
    }

    public void setIconSize(String iconSize) {
        this.iconSize = iconSize;
    }

    @Override
    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    @Override
    public Boolean getGps() {
        return gps;
    }

    public void setGps(Boolean gps) {
        this.gps = gps;
    }

    @Override
    public Boolean getBluetooth() {
        return bluetooth;
    }

    public void setBluetooth(Boolean bluetooth) {
        this.bluetooth = bluetooth;
    }

    @Override
    public Boolean getWifi() {
        return wifi;
    }

    public void setWifi(Boolean wifi) {
        this.wifi = wifi;
    }

    @Override
    public Boolean getMobileData() {
        return mobileData;
    }

    public void setMobileData(Boolean mobileData) {
        this.mobileData = mobileData;
    }

    @Override
    public boolean isKioskMode() {
        return kioskMode;
    }

    public void setKioskMode(boolean kioskMode) {
        this.kioskMode = kioskMode;
    }

    @Override
    public Boolean getKioskHome() {
        return kioskHome;
    }

    public void setKioskHome(Boolean kioskHome) {
        this.kioskHome = kioskHome;
    }

    @Override
    public Boolean getKioskRecents() {
        return kioskRecents;
    }

    public void setKioskRecents(Boolean kioskRecents) {
        this.kioskRecents = kioskRecents;
    }

    @Override
    public Boolean getKioskNotifications() {
        return kioskNotifications;
    }

    public void setKioskNotifications(Boolean kioskNotifications) {
        this.kioskNotifications = kioskNotifications;
    }

    @Override
    public Boolean getKioskSystemInfo() {
        return kioskSystemInfo;
    }

    public void setKioskSystemInfo(Boolean kioskSystemInfo) {
        this.kioskSystemInfo = kioskSystemInfo;
    }

    @Override
    public Boolean getKioskKeyguard() {
        return kioskKeyguard;
    }

    public void setKioskKeyguard(Boolean kioskKeyguard) {
        this.kioskKeyguard = kioskKeyguard;
    }

    @Override
    public String getMainApp() {
        return mainApp;
    }

    public void setMainApp(String mainApp) {
        this.mainApp = mainApp;
    }

    @Override
    public boolean isLockStatusBar() {
        return lockStatusBar;
    }

    public void setLockStatusBar(boolean lockStatusBar) {
        this.lockStatusBar = lockStatusBar;
    }

    @Override
    public int getSystemUpdateType() {
        return systemUpdateType;
    }

    public void setSystemUpdateType(int systemUpdateType) {
        this.systemUpdateType = systemUpdateType;
    }

    @Override
    public String getSystemUpdateFrom() {
        return systemUpdateFrom;
    }

    public void setSystemUpdateFrom(String systemUpdateFrom) {
        this.systemUpdateFrom = systemUpdateFrom;
    }

    @Override
    public String getSystemUpdateTo() {
        return systemUpdateTo;
    }

    public void setSystemUpdateTo(String systemUpdateTo) {
        this.systemUpdateTo = systemUpdateTo;
    }

    @Override
    public List<SyncApplicationSettingInt> getApplicationSettings() {
        return applicationSettings;
    }

    public void setApplicationSettings(List<SyncApplicationSettingInt> applicationSettings) {
        this.applicationSettings = applicationSettings;
    }

    public Boolean getUsbStorage() {
        return usbStorage;
    }

    public void setUsbStorage(Boolean usbStorage) {
        this.usbStorage = usbStorage;
    }

    public String getRequestUpdates() {
        return requestUpdates;
    }

    public void setRequestUpdates(String requestUpdates) {
        this.requestUpdates = requestUpdates;
    }

    public String getPushOptions() {
        return pushOptions;
    }

    public void setPushOptions(String pushOptions) {
        this.pushOptions = pushOptions;
    }

    public Boolean getAutoBrightness() {
        return autoBrightness;
    }

    public void setAutoBrightness(Boolean autoBrightness) {
        this.autoBrightness = autoBrightness;
    }

    public Integer getBrightness() {
        return brightness;
    }

    public void setBrightness(Integer brightness) {
        this.brightness = brightness;
    }

    public Boolean getManageTimeout() {
        return manageTimeout;
    }

    public void setManageTimeout(Boolean manageTimeout) {
        this.manageTimeout = manageTimeout;
    }

    public Integer getTimeout() {
        return timeout;
    }

    public void setTimeout(Integer timeout) {
        this.timeout = timeout;
    }

    public Boolean getLockVolume() {
        return lockVolume;
    }

    public void setLockVolume(Boolean lockVolume) {
        this.lockVolume = lockVolume;
    }

    public String getPasswordMode() {
        return passwordMode;
    }

    public void setPasswordMode(String passwordMode) {
        this.passwordMode = passwordMode;
    }

    @Override
    public Integer getOrientation() {
        return orientation;
    }

    public void setOrientation(Integer orientation) {
        this.orientation = orientation;
    }

    @Override
    public Boolean getRunDefaultLauncher() {
        return runDefaultLauncher;
    }

    public void setRunDefaultLauncher(Boolean runDefaultLauncher) {
        this.runDefaultLauncher = runDefaultLauncher;
    }

    @Override
    public String getTimeZone() {
        return timeZone;
    }

    public void setTimeZone(String timeZone) {
        this.timeZone = timeZone;
    }

    @Override
    public String getAllowedClasses() {
        return allowedClasses;
    }

    public void setAllowedClasses(String allowedClasses) {
        this.allowedClasses = allowedClasses;
    }

    @Override
    public List<SyncConfigurationFileInt> getFiles() {
        return files;
    }

    public void setFiles(List<SyncConfigurationFileInt> files) {
        this.files = files;
    }
}
