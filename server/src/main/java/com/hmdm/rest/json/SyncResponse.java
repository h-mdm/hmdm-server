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

    @ApiModelProperty("Keep-Alive time for MQTT connection")
    private Integer keepaliveTime;

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

    @ApiModelProperty("Volume manage option")
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private Boolean manageVolume;

    @ApiModelProperty("Volume (percents)")
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private Integer volume;

    @ApiModelProperty("Password requirements for the mobile device")
    private String passwordMode;

    @ApiModelProperty("Orientation lock: 0 - none, 1 - portrait, 2 - landscape")
    private Integer orientation;

    @ApiModelProperty("Set to true if Headwind MDM need to work together with a third-party launcher")
    private Boolean runDefaultLauncher;

    @ApiModelProperty("Flag indicating if screenshots are disabled on the device")
    private Boolean disableScreenshots;

    @ApiModelProperty("Time zone settings: null for using default settings, auto for automatic time zone, or Olson time zone string")
    private String timeZone;

    @ApiModelProperty("Allowed classes, separated by comma")
    private String allowedClasses;

    @ApiModelProperty("New server URL used to migrate to another server")
    private String newServerUrl;

    @ApiModelProperty("Flag disabling safe settings")
    private Boolean lockSafeSettings;

    @ApiModelProperty("Show WiFi settings if there's a connection error, also in Kiosk mode")
    private Boolean showWifi;

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

    @ApiModelProperty("New device number, used for changing the device number")
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String newNumber;

    @ApiModelProperty("List of additional restrictions in MDM mode")
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String restrictions;

    @ApiModelProperty(value = "Custom property #1 if it is being sent to device")
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String custom1;

    @ApiModelProperty(value = "Custom property #2 if it is being sent to device")
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String custom2;

    @ApiModelProperty(value = "Custom property #3 if it is being sent to device")
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String custom3;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String appName;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String vendor;

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
            if (device.getOldNumber() != null) {
                this.newNumber = device.getNumber();
            }
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
            if (device.getOldNumber() != null) {
                this.newNumber = device.getNumber();
            }
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

    @Override
    public Integer getKeepaliveTime() {
        return keepaliveTime;
    }

    public void setKeepaliveTime(Integer keepaliveTime) {
        this.keepaliveTime = keepaliveTime;
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

    public Boolean getManageVolume() {
        return manageVolume;
    }

    public void setManageVolume(Boolean manageVolume) {
        this.manageVolume = manageVolume;
    }

    public Integer getVolume() {
        return volume;
    }

    public void setVolume(Integer volume) {
        this.volume = volume;
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
    public Boolean getDisableScreenshots() {
        return disableScreenshots;
    }

    public void setDisableScreenshots(Boolean disableScreenshots) {
        this.disableScreenshots = disableScreenshots;
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
    public String getNewServerUrl() {
        return newServerUrl;
    }

    public void setNewServerUrl(String newServerUrl) {
        this.newServerUrl = newServerUrl;
    }

    @Override
    public Boolean getLockSafeSettings() {
        return lockSafeSettings;
    }

    public void setLockSafeSettings(Boolean lockSafeSettings) {
        this.lockSafeSettings = lockSafeSettings;
    }

    @Override
    public Boolean getShowWifi() {
        return showWifi;
    }

    public void setShowWifi(Boolean showWifi) {
        this.showWifi = showWifi;
    }

    @Override
    public List<SyncConfigurationFileInt> getFiles() {
        return files;
    }

    public void setFiles(List<SyncConfigurationFileInt> files) {
        this.files = files;
    }

    @Override
    public String getNewNumber() {
        return this.newNumber;
    }

    public void setNewNumber(String newNumber) {
        this.newNumber = newNumber;
    }

    @Override
    public String getRestrictions() {
        return this.restrictions;
    }

    public void setRestrictions(String restrictions) {
        this.restrictions = restrictions;
    }

    @Override
    public String getCustom1() {
        return custom1;
    }

    public void setCustom1(String custom1) {
        this.custom1 = custom1;
    }

    @Override
    public String getCustom2() {
        return custom2;
    }

    public void setCustom2(String custom2) {
        this.custom2 = custom2;
    }

    @Override
    public String getCustom3() {
        return custom3;
    }

    public void setCustom3(String custom3) {
        this.custom3 = custom3;
    }

    @Override
    public String getAppName() {
        return appName;
    }

    public void setAppName(String appName) {
        this.appName = appName;
    }

    @Override
    public String getVendor() {
        return vendor;
    }

    public void setVendor(String vendor) {
        this.vendor = vendor;
    }
}
