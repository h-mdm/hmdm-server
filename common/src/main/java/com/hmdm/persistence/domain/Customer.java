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
import java.util.Objects;

/**
 * <p>A customer account managed by the application.</p>
 */
public class Customer implements Serializable {

    private static final long serialVersionUID = 5087620848737792920L;
    
    private Integer id;
    private String name;
    private String description;
    private String filesDir;
    private String apiKey;
    private boolean master = false;

    /**
     * <p>A prefix for numbers for default generated devices for customer.</p>
     */
    private String prefix;

    /**
     * <p>An ID of a configuration to be set for default generated devices when creating a new customer account.</p>
     */
    private Integer deviceConfigurationId;

    /**
     * <p>A time of most recent login by any customer's user. (In milliseconds since epoch time).</p>
     */
    private Long lastLoginTime;

    /**
     * <p>A time of registration of customer account. (In milliseconds since epoch time).</p>
     */
    private Long registrationTime;

    // A helper field for customer creation (not stored)
    private boolean copyDesign;
    private Integer[] configurationIds;

    public Customer() {
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
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

    public String getFilesDir() {
        return filesDir;
    }

    public void setFilesDir(String filesDir) {
        this.filesDir = filesDir;
    }

    public boolean isMaster() {
        return master;
    }

    public void setMaster(boolean master) {
        this.master = master;
    }

    public boolean isCopyDesign() {
        return copyDesign;
    }

    public void setCopyDesign(boolean copyDesign) {
        this.copyDesign = copyDesign;
    }

    public Integer[] getConfigurationIds() {
        return configurationIds;
    }

    public void setConfigurationIds(Integer[] configurationIds) {
        this.configurationIds = configurationIds;
    }

    public String getApiKey() {
        return apiKey;
    }

    public void setApiKey(String apiKey) {
        this.apiKey = apiKey;
    }

    public String getPrefix() {
        return prefix;
    }

    public void setPrefix(String prefix) {
        this.prefix = prefix;
    }

    public Integer getDeviceConfigurationId() {
        return deviceConfigurationId;
    }

    public void setDeviceConfigurationId(Integer deviceConfigurationId) {
        this.deviceConfigurationId = deviceConfigurationId;
    }

    public Long getLastLoginTime() {
        return lastLoginTime;
    }

    public void setLastLoginTime(Long lastLoginTime) {
        this.lastLoginTime = lastLoginTime;
    }

    public Long getRegistrationTime() {
        return registrationTime;
    }

    public void setRegistrationTime(Long registrationTime) {
        this.registrationTime = registrationTime;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Customer customer = (Customer) o;
        return id.equals(customer.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return "Customer{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", description='" + description + '\'' +
                ", filesDir='" + filesDir + '\'' +
                ", master=" + master +
                ", prefix=" + prefix +
                ", deviceConfigurationId=" + deviceConfigurationId +
                '}';
    }
}
