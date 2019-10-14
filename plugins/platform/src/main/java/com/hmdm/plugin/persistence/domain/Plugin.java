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

package com.hmdm.plugin.persistence.domain;

import java.io.Serializable;
import java.util.Objects;

/**
 * <p>A domain object to represent a single plugin existing in the application.</p>
 *
 * @author isv
 */
public class Plugin implements Serializable {

    private static final long serialVersionUID = -1608865046021511170L;

    // An unique ID of a plugin in DB
    private int id;

    // An unique logical identifier of plugin
    private String identifier;

    // A name of the plugin
    private String name;

    // A description of the plugin
    private String description;

    // A time of the creation of plugin
    private String createTime;

    // A flag indicating if plugin is currently disabled or not
    private boolean disabled;

    // A path to the JavaScript-file containing the Angular module containing the controllers, servies, routes,
    // directives for plugin front-end
    private String javascriptModuleFile;

    // A path to the template to be used for servicing the Functions tab view. May be null if plugin
    // does not provide a view for Functions tab
    private String functionsViewTemplate;

    // A path to the template to be used for servicing the Settings tab view. May be null if plugin
    // does not provide a view for Settings tab
    private String settingsViewTemplate;

    // A key in I18N resource bundle to present the name of plugin in selected language
    private String nameLocalizationKey;

    // An optional permission require for granting access to plugin's Settings functionalities
    private String settingsPermission;

    // An optional permission require for granting access to plugin's Functions functionalities
    private String functionsPermission;

    // An optional permission require for granting access to plugin's Device Functions functionalities
    private String deviceFunctionsPermission;

    // A flag indicating if plugin is enabled for individual device
    private boolean enabledForDevice;

    /**
     * <p>Constructs new <code>Plugin</code> instance. This implementation does nothing.</p>
     */
    public Plugin() {
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getIdentifier() {
        return identifier;
    }

    public void setIdentifier(String identifier) {
        this.identifier = identifier;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getCreateTime() {
        return createTime;
    }

    public void setCreateTime(String createTime) {
        this.createTime = createTime;
    }

    public boolean isDisabled() {
        return disabled;
    }

    public void setDisabled(boolean disabled) {
        this.disabled = disabled;
    }

    public String getFunctionsViewTemplate() {
        return functionsViewTemplate;
    }

    public void setFunctionsViewTemplate(String functionsViewTemplate) {
        this.functionsViewTemplate = functionsViewTemplate;
    }

    public String getSettingsViewTemplate() {
        return settingsViewTemplate;
    }

    public void setSettingsViewTemplate(String settingsViewTemplate) {
        this.settingsViewTemplate = settingsViewTemplate;
    }

    public String getJavascriptModuleFile() {
        return javascriptModuleFile;
    }

    public void setJavascriptModuleFile(String javascriptModuleFile) {
        this.javascriptModuleFile = javascriptModuleFile;
    }

    public String getNameLocalizationKey() {
        return nameLocalizationKey;
    }

    public void setNameLocalizationKey(String nameLocalizationKey) {
        this.nameLocalizationKey = nameLocalizationKey;
    }

    public String getSettingsPermission() {
        return settingsPermission;
    }

    public void setSettingsPermission(String settingsPermission) {
        this.settingsPermission = settingsPermission;
    }

    public String getFunctionsPermission() {
        return functionsPermission;
    }

    public void setFunctionsPermission(String functionsPermission) {
        this.functionsPermission = functionsPermission;
    }

    public String getDeviceFunctionsPermission() {
        return deviceFunctionsPermission;
    }

    public void setDeviceFunctionsPermission(String deviceFunctionsPermission) {
        this.deviceFunctionsPermission = deviceFunctionsPermission;
    }

    public boolean isEnabledForDevice() {
        return enabledForDevice;
    }

    public void setEnabledForDevice(boolean enabledForDevice) {
        this.enabledForDevice = enabledForDevice;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Plugin plugin = (Plugin) o;
        return id == plugin.id;
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return "Plugin{" +
                "id=" + id +
                ", identifier='" + identifier + '\'' +
                ", name='" + name + '\'' +
                ", description='" + description + '\'' +
                ", createTime='" + createTime + '\'' +
                ", disabled=" + disabled +
                ", enabledForDevice=" + enabledForDevice +
                ", javascriptModuleFile='" + javascriptModuleFile + '\'' +
                ", functionsViewTemplate='" + functionsViewTemplate + '\'' +
                ", settingsViewTemplate='" + settingsViewTemplate + '\'' +
                ", nameLocalizationKey='" + nameLocalizationKey + '\'' +
                ", settingsPermission='" + settingsPermission + '\'' +
                ", functionsPermission='" + functionsPermission + '\'' +
                ", deviceFunctionsPermission='" + deviceFunctionsPermission + '\'' +
                '}';
    }
}
