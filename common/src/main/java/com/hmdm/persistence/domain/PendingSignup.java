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
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

import java.io.Serializable;

@ApiModel(description = "Record for the customer self-signup flow")
@JsonIgnoreProperties(ignoreUnknown = true)
public class PendingSignup implements Serializable {

    private static final long serialVersionUID = 3971700195069436679L;

    @ApiModelProperty("ID")
    private Integer id;

    /**
     * <p>Signup time. (In milliseconds since epoch time).)</p>
     */
    private Long signupTime;

    @ApiModelProperty("Customer email")
    private String email;

    @ApiModelProperty("Customer language (two small letters)")
    private String language;

    @ApiModelProperty("Customer signup token")
    private String token;

    public PendingSignup() {
    }

    public Integer getId() {
        return this.id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Long getSignupTime() {
        return signupTime;
    }

    public void setSignupTime(Long signupTime) {
        this.signupTime = signupTime;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getLanguage() {
        return language;
    }

    public void setLanguage(String language) {
        this.language = language;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }
}
