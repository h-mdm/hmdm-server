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

import java.io.Serializable;
import java.util.Objects;

/**
 * <p>$END$</p>
 *
 * @author isv
 */
@ApiModel(description = "A permission to perform desired action which might be granted to user role")
public class UserRolePermission implements Serializable {

    private static final long serialVersionUID = -8203664108953283604L;

    @ApiModelProperty("An ID of the permission")
    private int id;
    @ApiModelProperty("A name of the permission")
    private String name;
    @ApiModelProperty("A description of the permission")
    private String description;
    @ApiModelProperty(hidden = true)
    private boolean superAdmin;

    /**
     * <p>Constructs new <code>UserRolePermission</code> instance. This implementation does nothing.</p>
     */
    public UserRolePermission() {
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public boolean isSuperAdmin() {
        return superAdmin;
    }

    public void setSuperAdmin(boolean superAdmin) {
        this.superAdmin = superAdmin;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        UserRolePermission that = (UserRolePermission) o;
        return id == that.id;
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return "UserRolePermission{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", superAdmin=" + superAdmin +
                '}';
    }
}
