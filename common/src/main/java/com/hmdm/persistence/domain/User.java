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

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.hmdm.rest.json.LookupItem;

import java.io.Serializable;
import java.util.List;

@ApiModel(description = "A user account within MDM web application")
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class User implements CustomerData, Serializable {

    private static final long serialVersionUID = -5231237331183323703L;

    @ApiModelProperty("An ID of a user")
    private Integer id;
    @ApiModelProperty("A username of a user")
    private String login;
    @ApiModelProperty("An email address of a user")
    private String email;
    @ApiModelProperty("A name of a user")
    private String name;
    @ApiModelProperty("A password of a user (Salted SHA1 hash)")
    private transient String password;
    @ApiModelProperty(hidden = true)
    private int customerId;
    @ApiModelProperty("A role assigned to user")
    private UserRole userRole;
    @ApiModelProperty("Are all devices available to user")
    private boolean allDevicesAvailable = true;
    @ApiModelProperty("Are all configs available to user")
    private boolean allConfigAvailable = true;
    @ApiModelProperty("Is password reset required")
    private boolean passwordReset = false;
    @ApiModelProperty("Authentication token")
    private String authToken;
    @ApiModelProperty("Password reset token")
    private String passwordResetToken;

    // Many-to-many relations
    private List<LookupItem> groups;
    private List<LookupItem> configurations;

    // Helper fields which are not mapped directly to DB and are not persisted with user object
    @ApiModelProperty("An old password for user to be used for verification when changing the password")
    private String oldPassword;
    @ApiModelProperty("A new password to be set for user")
    private String newPassword;
    @ApiModelProperty(hidden = true)
    private boolean masterCustomer;
    @ApiModelProperty(hidden = true)
    private boolean editable = false;

    public User() {
    }

    public Integer getId() {
        return this.id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getLogin() {
        return this.login;
    }

    public void setLogin(String login) {
        this.login = login;
    }

    public String getEmail() {
        return this.email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getName() {
        return this.name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPassword() {
        return this.password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getOldPassword() {
        return oldPassword;
    }

    public int getCustomerId() {
        return customerId;
    }

    public void setCustomerId(int customerId) {
        this.customerId = customerId;
    }

    public boolean isMasterCustomer() {
        return masterCustomer;
    }

    public void setMasterCustomer(boolean masterCustomer) {
        this.masterCustomer = masterCustomer;
    }

    public void setOldPassword(String oldPassword) {
        this.oldPassword = oldPassword;
    }

    public String getNewPassword() {
        return newPassword;
    }

    public void setNewPassword(String newPassword) {
        this.newPassword = newPassword;
    }

    public boolean isEditable() {
        return editable;
    }

    public void setEditable(boolean editable) {
        this.editable = editable;
    }

    public UserRole getUserRole() {
        return userRole;
    }

    public void setUserRole(UserRole userRole) {
        this.userRole = userRole;
    }

    public boolean isSuperAdmin() {
        return this.userRole.isSuperAdmin();
    }

    public boolean isAllDevicesAvailable() {
        return allDevicesAvailable;
    }

    public void setAllDevicesAvailable(boolean allDevicesAvailable) {
        this.allDevicesAvailable = allDevicesAvailable;
    }

    public boolean isAllConfigAvailable() {
        return allConfigAvailable;
    }

    public void setAllConfigAvailable(boolean allConfigAvailable) {
        this.allConfigAvailable = allConfigAvailable;
    }

    public boolean isPasswordReset() {
        return passwordReset;
    }

    public void setPasswordReset(boolean passwordReset) {
        this.passwordReset = passwordReset;
    }

    public String getAuthToken() {
        return authToken;
    }

    public void setAuthToken(String authToken) {
        this.authToken = authToken;
    }

    public String getPasswordResetToken() {
        return passwordResetToken;
    }

    public void setPasswordResetToken(String passwordResetToken) {
        this.passwordResetToken = passwordResetToken;
    }

    public List<LookupItem> getGroups() {
        return groups;
    }

    public void setGroups(List<LookupItem> groups) {
        this.groups = groups;
    }

    public List<LookupItem> getConfigurations() {
        return configurations;
    }

    public void setConfigurations(List<LookupItem> configurations) {
        this.configurations = configurations;
    }

    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append(this.getName());
        builder.append(' ');
        builder.append(this.getLogin());
        builder.append(' ');
        builder.append(this.getEmail());
        builder.append(' ');
        builder.append(this.getUserRole().getName());
        return builder.toString();
    }


}
