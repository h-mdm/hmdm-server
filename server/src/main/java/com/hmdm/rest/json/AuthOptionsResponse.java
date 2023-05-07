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

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.annotations.ApiModel;

/**
 * <p>A DTO carrying the data for login options/p>
 *
 * @author seva
 */
@ApiModel(description = "Login options (reset password, recover, etc)")
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class AuthOptionsResponse {

    private boolean recover;
    private boolean signup;
    private String publicKey;

    /**
     * <p>Constructs new <code>AuthOptionsResponse</code> instance. This implementation does nothing.</p>
     */
    public AuthOptionsResponse() {
    }

    public boolean isRecover() {
        return recover;
    }

    public void setRecover(boolean recover) {
        this.recover = recover;
    }

    public boolean isSignup() {
        return signup;
    }

    public void setSignup(boolean signup) {
        this.signup = signup;
    }

    public String getPublicKey() {
        return publicKey;
    }

    public void setPublicKey(String publicKey) {
        this.publicKey = publicKey;
    }
}
