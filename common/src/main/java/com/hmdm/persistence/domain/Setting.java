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

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
@Deprecated
public class Setting {
    private Integer id;
    private String name;
    private String description;
    private String kioskUpdateUrl;
    private String browserUpdateUrl;
    private String browserMainUrl;
    private String domains;
    private String offlineVideos;
    private Integer offlineMode;
    private String screenSaverVideos;
    private Integer screenSaverEnabled;
    private Integer inactiveTime;
    private Integer updateServiceTime;

    public Setting() {
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

    public String getKioskUpdateUrl() {
        return this.kioskUpdateUrl == null ? "" : this.kioskUpdateUrl;
    }

    public void setKioskUpdateUrl(String kioskUpdateUrl) {
        this.kioskUpdateUrl = kioskUpdateUrl;
    }

    public String getBrowserUpdateUrl() {
        return this.browserUpdateUrl == null ? "" : this.browserUpdateUrl;
    }

    public void setBrowserUpdateUrl(String browserUpdateUrl) {
        this.browserUpdateUrl = browserUpdateUrl;
    }

    public String getBrowserMainUrl() {
        return this.browserMainUrl;
    }

    public void setBrowserMainUrl(String browserMainUrl) {
        this.browserMainUrl = browserMainUrl;
    }

    public String getDomains() {
        return this.domains;
    }

    public void setDomains(String domains) {
        this.domains = domains;
    }

    public String getOfflineVideos() {
        return this.offlineVideos;
    }

    public void setOfflineVideos(String offlineVideos) {
        this.offlineVideos = offlineVideos;
    }

    public Integer getOfflineMode() {
        return this.offlineMode;
    }

    public void setOfflineMode(Integer offlineMode) {
        this.offlineMode = offlineMode;
    }

    public String getScreenSaverVideos() {
        return this.screenSaverVideos;
    }

    public void setScreenSaverVideos(String screenSaverVideos) {
        this.screenSaverVideos = screenSaverVideos;
    }

    public Integer getScreenSaverEnabled() {
        return this.screenSaverEnabled;
    }

    public void setScreenSaverEnabled(Integer screenSaverEnabled) {
        this.screenSaverEnabled = screenSaverEnabled;
    }

    public Integer getInactiveTime() {
        return this.inactiveTime;
    }

    public void setInactiveTime(Integer inactiveTime) {
        this.inactiveTime = inactiveTime;
    }

    public Integer getUpdateServiceTime() {
        return this.updateServiceTime;
    }

    public void setUpdateServiceTime(Integer updateServiceTime) {
        this.updateServiceTime = updateServiceTime;
    }
}
