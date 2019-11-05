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

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.io.Serializable;

@ApiModel(description = "The credentials to be used for authenticating the user to application")
@JsonIgnoreProperties(ignoreUnknown = true)
public class UserCredentials implements Serializable {

    private static final long serialVersionUID = 7107010132749776504L;
    
    @ApiModelProperty("A username to be used for authentication")
    private String login;

    @ApiModelProperty("A password to be used for authentication (MD5-hash)")
    private String password;

    @ApiModelProperty(hidden = true)
    @Deprecated
    private String email;

    public UserCredentials() {
    }

    public String getLogin() {
        return this.login;
    }

    public void setLogin(String login) {
        this.login = login;
    }

    public String getPassword() {
        return this.password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    @Deprecated
    public String getEmail() {
        return this.email;
    }

    @Deprecated
    public void setEmail(String email) {
        this.email = email;
    }
}
