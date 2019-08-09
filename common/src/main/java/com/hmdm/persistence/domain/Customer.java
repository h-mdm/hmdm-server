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

import java.util.Objects;

/**
 * <p>$END$</p>
 *
 * @author isv
 */
public class Customer {

    private Integer id;
    private String name;
    private String description;
    private String filesDir;
    private boolean master = false;

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
                '}';
    }
}
