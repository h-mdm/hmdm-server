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

import com.hmdm.persistence.domain.Application;
import com.hmdm.persistence.domain.ConfigurationFile;

import java.util.List;

/**
 * <p>An interface for the response sent to device in response to request for configuration synchronization.</p>
 *
 * @author isv
 */
public interface SyncResponseInt {

    String getBackgroundColor();

    String getTextColor();

    String getBackgroundImageUrl();

    List<SyncApplicationInt> getApplications();

    String getPassword();

    String getImei();

    String getPhone();

    String getIconSize();

    String getTitle();

    Boolean getGps();

    Boolean getBluetooth();

    Boolean getWifi();

    Boolean getMobileData();

    boolean isKioskMode();

    Boolean getKioskHome();

    Boolean getKioskRecents();

    Boolean getKioskNotifications();

    Boolean getKioskSystemInfo();

    Boolean getKioskKeyguard();

    String getMainApp();

    boolean isLockStatusBar();

    int getSystemUpdateType();

    String getSystemUpdateFrom();

    String getSystemUpdateTo();

    List<SyncApplicationSettingInt> getApplicationSettings();

    Boolean getUsbStorage();

    String getRequestUpdates();

    String getPushOptions();

    Integer getKeepaliveTime();

    Boolean getAutoBrightness();

    Integer getBrightness();

    Boolean getManageTimeout();

    Integer getTimeout();

    Boolean getLockVolume();

    Boolean getManageVolume();

    Integer getVolume();

    String getPasswordMode();

    Integer getOrientation();

    Boolean getRunDefaultLauncher();

    Boolean getDisableScreenshots();

    String getTimeZone();

    String getAllowedClasses();

    String getNewServerUrl();

    Boolean getLockSafeSettings();

    Boolean getShowWifi();

    List<SyncConfigurationFileInt> getFiles();

    String getNewNumber();

    String getRestrictions();

    String getCustom1();

    String getCustom2();

    String getCustom3();

    String getAppName();

    String getVendor();
}
