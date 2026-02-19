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

import java.io.Serializable;
import java.util.LinkedList;
import java.util.List;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.swagger.v3.oas.annotations.media.Schema;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import static com.hmdm.persistence.domain.DesktopHeader.NO_HEADER;
import static com.hmdm.persistence.domain.IconSize.SMALL;

@Schema(description = "An MDM configuration used on mobile device")
@JsonIgnoreProperties(ignoreUnknown = true)
public class Configuration implements CustomerData, Serializable {

    private static final long serialVersionUID = -7028146375054564719L;

    // This group of settings corresponds to common settings
    @Schema(description = "A configuration ID")
    private Integer id;
    @Schema(description = "A unique name of configuration")
    private String name;
    @Schema(description = "A description of configuration")
    private String description;
    @Schema(description = "A password for administrator of configuration (MD5 hash)")
    private String password;
    @Schema(hidden = true)
    private int type;
    @Schema(description = "A list of applications set for configuration")
    private List<Application> applications = new LinkedList<>();
    @Schema(hidden = true)
    private int customerId;
    @Schema(description = "A flag indicating if application versions update is enabled", hidden = true)
    private final boolean autoUpdate = false;
    @Schema(description = "A flag indicating if status bar is locked")
    private boolean blockStatusBar;
    @Schema(description = "A system update type. 0-Default, 1-Immediately, 2-Scheduled, 3-Postponed", allowableValues = "0,1,2,3")
    private int systemUpdateType;
    @Schema(description = "A start time for system update period formatted as HH:MM. (If system update time is 2)")
    private String systemUpdateFrom;
    @Schema(description = "An end time for system update period formatted as HH:MM. (If system update time is 2)")
    private String systemUpdateTo;
    @Schema(description = "A flag indicating if the application update must be scheduled")
    private boolean scheduleAppUpdate;
    @Schema(description = "A start time for app update period formatted as HH:MM.")
    private String appUpdateFrom;
    @Schema(description = "An end time for system update period formatted as HH:MM.")
    private String appUpdateTo;
    @Schema(description = "Limits downloading updates")
    private DownloadUpdatesType downloadUpdates = DownloadUpdatesType.UNLIMITED;

    @Schema(description = "A flag indicating if GPS is enabled on device")
    private Boolean gps;
    @Schema(description = "A flag indicating if Bluetooth is enabled on device")
    private Boolean bluetooth;
    @Schema(description = "A flag indicating if Wi-Fi is enabled on device")
    private Boolean wifi;
    @Schema(description = "A flag indicating if Mobile Data is enabled on device")
    private Boolean mobileData;
    @Schema(description = "A flag indicating if USB storage is enabled on device")
    private Boolean usbStorage;
    @Schema(description = "A type of location tracking")
    private RequestUpdatesType requestUpdates = RequestUpdatesType.DONOTTRACK;
    @Schema(description = "A flag indicating if location permission shouldn't be granted")
    private Boolean disableLocation;
    @Schema(description = "Strategy of app permission auto-granting")
    private AppPermissionsType appPermissions = AppPermissionsType.GRANTALL;
    @Schema(description = "Push notification options")
    private String pushOptions;
    @Schema(description = "Keep-Alive time for MQTT connection")
    private Integer keepaliveTime;
    @Schema(description = "Brightness management flag. null: not managed, false: manual, true: auto")
    private Boolean autoBrightness;
    @Schema(description = "Brightness value (if manual), 0-255")
    private Integer brightness;
    @Schema(description = "A flag indicating if screen timeout is managed on device")
    private Boolean manageTimeout;
    @Schema(description = "Timeout value (in seconds)")
    private Integer timeout;
    @Schema(description = "A flag indicating if volume is locked on device")
    private Boolean lockVolume;
    @Schema(description = "A flag indicating if volume must be adjusted on device")
    private Boolean manageVolume;
    @Schema(description = "Volume value (in percents)")
    private Integer volume;
    @Schema(description = "Password requirements for the mobile device")
    private String passwordMode;
    @Schema(description = "Orientation lock: 0 - none, 1 - portrait, 2 - landscape")
    private Integer orientation;
    @Schema(description = "Flag enabling usage with default launcher")
    private Boolean runDefaultLauncher;
    @Schema(description = "Flag indicating if screenshots are disabled on the device")
    private Boolean disableScreenshots;
    @Schema(description = "Flag indicating if auto-started apps should be kept in the foreground")
    private Boolean autostartForeground;
    @Schema(description = "Time zone settings: null for using default settings, auto for automatic time zone, or Olson time zone string")
    private String timeZone;
    @Schema(description = "Allowed classes, separated by comma")
    private String allowedClasses;
    @Schema(description = "New server URL used to migrate to another server")
    private String newServerUrl;
    @Schema(description = "Flag disabling safe settings")
    private Boolean lockSafeSettings;
    @Schema(description = "Flag enabling permissive mode")
    private Boolean permissive;
    @Schema(description = "Flag enabling the kiosk exit button")
    private Boolean kioskExit;
    @Schema(description = "Show WiFi settings if there's a connection error, also in Kiosk mode")
    private Boolean showWifi;

    // This group of settings corresponds to MDM settings
    @Schema(description = "A package ID for main application")
    private Integer mainAppId;
    @Schema(description = "A package ID for event receiving component")
    private String eventReceivingComponent;
    @Schema(description = "A flag indicating if MDM is operating in kiosk mode")
    private boolean kioskMode;
    @Schema(description = "A package ID for content application")
    private Integer contentAppId;
    @Schema(description = "WiFi SSID for provisioning")
    private String wifiSSID;
    @Schema(description = "WiFi password for provisioning")
    private String wifiPassword;
    @Schema(description = "WiFi security type for provisioning: NONE/WPA/WEP/EAP")
    private String wifiSecurityType;
    @Schema(description = "Device encryption")
    private boolean encryptDevice;
    @Schema(description = "Additional QR code parameters")
    private String qrParameters;
    @Schema(description = "Admin extras in QR code")
    private String adminExtras;
    @Schema(description = "Prefer mobile data for provisioning")
    private boolean mobileEnrollment;
    @Schema(description = "Flag enabling Home button in kiosk mode")
    private Boolean kioskHome;
    @Schema(description = "Flag enabling Recents button in kiosk mode")
    private Boolean kioskRecents;
    @Schema(description = "Flag enabling notifications in kiosk mode")
    private Boolean kioskNotifications;
    @Schema(description = "Flag enabling system info in kiosk mode")
    private Boolean kioskSystemInfo;
    @Schema(description = "Flag enabling lock screen in kiosk mode")
    private Boolean kioskKeyguard;
    @Schema(description = "Flag locking power button in kiosk mode")
    private Boolean kioskLockButtons;
    @Schema(description = "Flag forcing screen to be on in kiosk mode")
    private Boolean kioskScreenOn;
    @Schema(description = "Overridden launcher URL")
    private String launcherUrl;
    @Schema(description = "Additional comma separated restrictions in MDM mode")
    private String restrictions;

    // This group of settings corresponds to Design settings
    private boolean useDefaultDesignSettings;
    @Schema(description = "A background color to use when running MDM application")
    private String backgroundColor;
    @Schema(description = "A text color to use when running MDM application")
    private String textColor;
    @Schema(description = "An URL for background image to use when running MDM application")
    private String backgroundImageUrl;
    @Schema(description = "A size of the icons to use when running MDM application")
    private IconSize iconSize = SMALL;
    @Schema(description = "A type of desktop header to use when running MDM application")
    private DesktopHeader desktopHeader = NO_HEADER;
    @Schema(description = "Desktop header template")
    private String desktopHeaderTemplate;
    @Schema(description = "If checked, the data of the device status bar (time, battery, etc) are displayed by Headwind MDM")
    private boolean displayStatus;

    // An unique key used for retrieving the QR code for configuration
    @Schema(hidden = true)
    private String qrCodeKey;

    @Schema(description = "A list of settings for applications set for configuration")
    private List<ApplicationSetting> applicationSettings;

    @Schema(description = "A list of files to be used on devices")
    private List<ConfigurationFile> files;

    @Schema(description = "The parameters for using applications set for configuration")
    private List<ConfigurationApplicationParameters> applicationUsageParameters;

    @Schema(hidden = true)
    private boolean selected;
    @Schema(hidden = true)
    private String baseUrl;
    @Schema(hidden = true)
    private List<Integer> filesToRemove;

    @Schema(hidden = true)
    private String defaultFilePath;

    public Configuration() {
    }

    public Integer getId() {
        return this.id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getName() {
        return this.name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return this.description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public List<Application> getApplications() {
        return this.applications;
    }

    public void setApplications(List<Application> applications) {
        this.applications = applications;
    }

    public int getType() {
        return this.type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public String getPassword() {
        return this.password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public boolean isSelected() {
        return selected;
    }

    public void setSelected(boolean selected) {
        this.selected = selected;
    }

    public String getBackgroundColor() {
        return backgroundColor;
    }

    public void setBackgroundColor(String backgroundColor) {
        this.backgroundColor = backgroundColor;
    }

    public String getTextColor() {
        return textColor;
    }

    public void setTextColor(String textColor) {
        this.textColor = textColor;
    }

    public String getBackgroundImageUrl() {
        return backgroundImageUrl;
    }

    public void setBackgroundImageUrl(String backgroundImageUrl) {
        this.backgroundImageUrl = backgroundImageUrl;
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

    public boolean isDisplayStatus() {
        return displayStatus;
    }

    public void setDisplayStatus(boolean displayStatus) {
        this.displayStatus = displayStatus;
    }

    public boolean isUseDefaultDesignSettings() {
        return useDefaultDesignSettings;
    }

    @Override
    public int getCustomerId() {
        return customerId;
    }

    @Override
    public void setCustomerId(int customerId) {
        this.customerId = customerId;
    }

    public Boolean getGps() {
        return gps;
    }

    public void setGps(Boolean gps) {
        this.gps = gps;
    }

    public Boolean getBluetooth() {
        return bluetooth;
    }

    public void setBluetooth(Boolean bluetooth) {
        this.bluetooth = bluetooth;
    }

    public Boolean getWifi() {
        return wifi;
    }

    public void setWifi(Boolean wifi) {
        this.wifi = wifi;
    }

    public Boolean getMobileData() {
        return mobileData;
    }

    public void setMobileData(Boolean mobileData) {
        this.mobileData = mobileData;
    }

    public void setUseDefaultDesignSettings(boolean useDefaultDesignSettings) {
        this.useDefaultDesignSettings = useDefaultDesignSettings;
    }

    public Integer getMainAppId() {
        return mainAppId;
    }

    public void setMainAppId(Integer mainAppId) {
        this.mainAppId = mainAppId;
    }

    public Integer getContentAppId() {
        return contentAppId;
    }

    public void setContentAppId(Integer contentAppId) {
        this.contentAppId = contentAppId;
    }

    public String getEventReceivingComponent() {
        return eventReceivingComponent;
    }

    public void setEventReceivingComponent(String eventReceivingComponent) {
        this.eventReceivingComponent = eventReceivingComponent;
    }

    public boolean isKioskMode() {
        return kioskMode;
    }

    public void setKioskMode(boolean kioskMode) {
        this.kioskMode = kioskMode;
    }

    public String getQrCodeKey() {
        return qrCodeKey;
    }

    public void setQrCodeKey(String qrCodeKey) {
        this.qrCodeKey = qrCodeKey;
    }

    public String getWifiSSID() {
        return wifiSSID;
    }

    public void setWifiSSID(String wifiSSID) {
        this.wifiSSID = wifiSSID;
    }

    public String getWifiPassword() {
        return wifiPassword;
    }

    public void setWifiPassword(String wifiPassword) {
        this.wifiPassword = wifiPassword;
    }

    public String getWifiSecurityType() {
        return wifiSecurityType;
    }

    public void setWifiSecurityType(String wifiSecurityType) {
        this.wifiSecurityType = wifiSecurityType;
    }

    public boolean isEncryptDevice() {
        return encryptDevice;
    }

    public void setEncryptDevice(boolean encryptDevice) {
        this.encryptDevice = encryptDevice;
    }

    public String getQrParameters() {
        return qrParameters;
    }

    public void setQrParameters(String qrParameters) {
        this.qrParameters = qrParameters;
    }

    public String getAdminExtras() {
        return adminExtras;
    }

    public void setAdminExtras(String adminExtras) {
        this.adminExtras = adminExtras;
    }

    public boolean isMobileEnrollment() {
        return mobileEnrollment;
    }

    public void setMobileEnrollment(boolean mobileEnrollment) {
        this.mobileEnrollment = mobileEnrollment;
    }

    public Boolean getKioskHome() {
        return kioskHome;
    }

    public void setKioskHome(Boolean kioskHome) {
        this.kioskHome = kioskHome;
    }

    public Boolean getKioskRecents() {
        return kioskRecents;
    }

    public void setKioskRecents(Boolean kioskRecents) {
        this.kioskRecents = kioskRecents;
    }

    public Boolean getKioskNotifications() {
        return kioskNotifications;
    }

    public void setKioskNotifications(Boolean kioskNotifications) {
        this.kioskNotifications = kioskNotifications;
    }

    public Boolean getKioskSystemInfo() {
        return kioskSystemInfo;
    }

    public void setKioskSystemInfo(Boolean kioskSystemInfo) {
        this.kioskSystemInfo = kioskSystemInfo;
    }

    public Boolean getKioskKeyguard() {
        return kioskKeyguard;
    }

    public void setKioskKeyguard(Boolean kioskKeyguard) {
        this.kioskKeyguard = kioskKeyguard;
    }

    public Boolean getKioskLockButtons() {
        return kioskLockButtons;
    }

    public void setKioskLockButtons(Boolean kioskLockButtons) {
        this.kioskLockButtons = kioskLockButtons;
    }

    public Boolean getKioskScreenOn() {
        return kioskScreenOn;
    }

    public void setKioskScreenOn(Boolean kioskScreenOn) {
        this.kioskScreenOn = kioskScreenOn;
    }

    public String getLauncherUrl() {
        return launcherUrl;
    }

    public void setLauncherUrl(String launcherUrl) {
        this.launcherUrl = launcherUrl;
    }

    public String getRestrictions() {
        return restrictions;
    }

    public void setRestrictions(String restrictions) {
        this.restrictions = restrictions;
    }

    public String getBaseUrl() {
        return baseUrl;
    }

    public void setBaseUrl(String baseUrl) {
        this.baseUrl = baseUrl;
    }

    public boolean isAutoUpdate() {
        return autoUpdate;
    }

    // public void setAutoUpdate(boolean autoUpdate) {
    // this.autoUpdate = autoUpdate;
    // }

    public boolean isBlockStatusBar() {
        return blockStatusBar;
    }

    public void setBlockStatusBar(boolean blockStatusBar) {
        this.blockStatusBar = blockStatusBar;
    }

    public int getSystemUpdateType() {
        return systemUpdateType;
    }

    public void setSystemUpdateType(int systemUpdateType) {
        this.systemUpdateType = systemUpdateType;
    }

    public String getSystemUpdateFrom() {
        return systemUpdateFrom;
    }

    public void setSystemUpdateFrom(String systemUpdateFrom) {
        this.systemUpdateFrom = systemUpdateFrom;
    }

    public String getSystemUpdateTo() {
        return systemUpdateTo;
    }

    public void setSystemUpdateTo(String systemUpdateTo) {
        this.systemUpdateTo = systemUpdateTo;
    }

    public boolean isScheduleAppUpdate() {
        return scheduleAppUpdate;
    }

    public void setScheduleAppUpdate(boolean scheduleAppUpdate) {
        this.scheduleAppUpdate = scheduleAppUpdate;
    }

    public String getAppUpdateFrom() {
        return appUpdateFrom;
    }

    public void setAppUpdateFrom(String appUpdateFrom) {
        this.appUpdateFrom = appUpdateFrom;
    }

    public String getAppUpdateTo() {
        return appUpdateTo;
    }

    public void setAppUpdateTo(String appUpdateTo) {
        this.appUpdateTo = appUpdateTo;
    }

    public DownloadUpdatesType getDownloadUpdates() {
        return downloadUpdates;
    }

    public void setDownloadUpdates(DownloadUpdatesType downloadUpdates) {
        this.downloadUpdates = downloadUpdates;
    }

    public List<ApplicationSetting> getApplicationSettings() {
        return applicationSettings;
    }

    public void setApplicationSettings(List<ApplicationSetting> applicationSettings) {
        this.applicationSettings = applicationSettings;
    }

    public List<ConfigurationApplicationParameters> getApplicationUsageParameters() {
        return applicationUsageParameters;
    }

    public void setApplicationUsageParameters(List<ConfigurationApplicationParameters> applicationUsageParameters) {
        this.applicationUsageParameters = applicationUsageParameters;
    }

    public Boolean getUsbStorage() {
        return usbStorage;
    }

    public void setUsbStorage(Boolean usbStorage) {
        this.usbStorage = usbStorage;
    }

    public RequestUpdatesType getRequestUpdates() {
        return requestUpdates;
    }

    public void setRequestUpdates(RequestUpdatesType requestUpdates) {
        this.requestUpdates = requestUpdates;
    }

    public Boolean getDisableLocation() {
        return disableLocation;
    }

    public void setDisableLocation(Boolean disableLocation) {
        this.disableLocation = disableLocation;
    }

    public AppPermissionsType getAppPermissions() {
        return appPermissions;
    }

    public void setAppPermissions(AppPermissionsType appPermissions) {
        this.appPermissions = appPermissions;
    }

    public String getPushOptions() {
        return pushOptions;
    }

    public void setPushOptions(String pushOptions) {
        this.pushOptions = pushOptions;
    }

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

    public Integer getOrientation() {
        return orientation;
    }

    public void setOrientation(Integer orientation) {
        this.orientation = orientation;
    }

    public Boolean getRunDefaultLauncher() {
        return runDefaultLauncher;
    }

    public void setRunDefaultLauncher(Boolean runDefaultLauncher) {
        this.runDefaultLauncher = runDefaultLauncher;
    }

    public Boolean getDisableScreenshots() {
        return disableScreenshots;
    }

    public void setDisableScreenshots(Boolean disableScreenshots) {
        this.disableScreenshots = disableScreenshots;
    }

    public Boolean getAutostartForeground() {
        return autostartForeground;
    }

    public void setAutostartForeground(Boolean autostartForeground) {
        this.autostartForeground = autostartForeground;
    }

    public String getTimeZone() {
        return timeZone;
    }

    public void setTimeZone(String timeZone) {
        this.timeZone = timeZone;
    }

    public String getAllowedClasses() {
        return allowedClasses;
    }

    public void setAllowedClasses(String allowedClasses) {
        this.allowedClasses = allowedClasses;
    }

    public String getNewServerUrl() {
        return newServerUrl;
    }

    public void setNewServerUrl(String newServerUrl) {
        this.newServerUrl = newServerUrl;
    }

    public Boolean getLockSafeSettings() {
        return lockSafeSettings;
    }

    public void setLockSafeSettings(Boolean lockSafeSettings) {
        this.lockSafeSettings = lockSafeSettings;
    }

    public Boolean getPermissive() {
        return permissive;
    }

    public void setPermissive(Boolean permissive) {
        this.permissive = permissive;
    }

    public Boolean getKioskExit() {
        return kioskExit;
    }

    public void setKioskExit(Boolean kioskExit) {
        this.kioskExit = kioskExit;
    }

    public Boolean getShowWifi() {
        return showWifi;
    }

    public void setShowWifi(Boolean showWifi) {
        this.showWifi = showWifi;
    }

    public List<ConfigurationFile> getFiles() {
        return files;
    }

    public void setFiles(List<ConfigurationFile> files) {
        this.files = files;
    }

    public List<Integer> getFilesToRemove() {
        return filesToRemove;
    }

    public void setFilesToRemove(List<Integer> filesToRemove) {
        this.filesToRemove = filesToRemove;
    }

    public String getDefaultFilePath() {
        return defaultFilePath;
    }

    public void setDefaultFilePath(String defaultFilePath) {
        this.defaultFilePath = defaultFilePath;
    }

    public Configuration newCopy() {
        Configuration copy = new Configuration();

        copy.setDescription(getDescription());
        copy.setName(getName());
        copy.setApplications(getApplications());
        copy.setApplicationSettings(getApplicationSettings());
        copy.setApplicationUsageParameters(getApplicationUsageParameters());
        copy.setFiles(getFiles());
        copy.setPassword(getPassword());
        copy.setType(getType());
        copy.setCustomerId(getCustomerId());
        // copy.setAutoUpdate(isAutoUpdate());
        copy.setBlockStatusBar(isBlockStatusBar());
        copy.setSystemUpdateType(getSystemUpdateType());
        copy.setSystemUpdateFrom(getSystemUpdateFrom());
        copy.setSystemUpdateTo(getSystemUpdateTo());
        copy.setScheduleAppUpdate(isScheduleAppUpdate());
        copy.setAppUpdateFrom(getAppUpdateFrom());
        copy.setAppUpdateTo(getAppUpdateTo());
        copy.setDownloadUpdates(getDownloadUpdates());

        copy.setMainAppId(getMainAppId());
        copy.setContentAppId(getContentAppId());
        copy.setEventReceivingComponent(getEventReceivingComponent());
        copy.setKioskMode(isKioskMode());
        copy.setWifiSSID(getWifiSSID());
        copy.setWifiPassword(getWifiPassword());
        copy.setWifiSecurityType(getWifiSecurityType());
        copy.setEncryptDevice(isEncryptDevice());
        copy.setQrParameters(getQrParameters());
        copy.setAdminExtras(getAdminExtras());
        copy.setKioskHome(getKioskHome());
        copy.setKioskRecents(getKioskRecents());
        copy.setKioskScreenOn(getKioskScreenOn());
        copy.setLauncherUrl(getLauncherUrl());
        copy.setKioskNotifications(getKioskNotifications());
        copy.setKioskSystemInfo(getKioskSystemInfo());
        copy.setKioskKeyguard(getKioskKeyguard());
        copy.setKioskLockButtons(getKioskLockButtons());
        copy.setRestrictions(getRestrictions());

        copy.setGps(getGps());
        copy.setBluetooth(getBluetooth());
        copy.setWifi(getWifi());
        copy.setMobileData(getMobileData());
        copy.setUsbStorage(getUsbStorage());
        copy.setRequestUpdates(getRequestUpdates());
        copy.setDisableLocation(getDisableLocation());
        copy.setAppPermissions(getAppPermissions());
        copy.setPushOptions(getPushOptions());
        copy.setKeepaliveTime(getKeepaliveTime());
        copy.setAutoBrightness(getAutoBrightness());
        copy.setBrightness(getBrightness());
        copy.setManageTimeout(getManageTimeout());
        copy.setTimeout(getTimeout());
        copy.setLockVolume(getLockVolume());
        copy.setManageVolume(getManageVolume());
        copy.setVolume(getVolume());
        copy.setPasswordMode(getPasswordMode());
        copy.setOrientation(getOrientation());
        copy.setRunDefaultLauncher(getRunDefaultLauncher());
        copy.setDisableScreenshots(getDisableScreenshots());
        copy.setAutostartForeground(getAutostartForeground());
        copy.setTimeZone(getTimeZone());
        copy.setAllowedClasses(getAllowedClasses());
        copy.setNewServerUrl(getNewServerUrl());
        copy.setLockSafeSettings(getLockSafeSettings());
        copy.setPermissive(getPermissive());
        copy.setKioskExit(getKioskExit());
        copy.setShowWifi(getShowWifi());

        copy.setUseDefaultDesignSettings(isUseDefaultDesignSettings());
        copy.setBackgroundColor(getBackgroundColor());
        copy.setTextColor(getTextColor());
        copy.setBackgroundImageUrl(getBackgroundImageUrl());
        copy.setIconSize(getIconSize());
        copy.setDesktopHeader(getDesktopHeader());
        copy.setDesktopHeaderTemplate(getDesktopHeaderTemplate());
        copy.setDisplayStatus(isDisplayStatus());

        copy.setDefaultFilePath(getDefaultFilePath());

        return copy;
    }
}
