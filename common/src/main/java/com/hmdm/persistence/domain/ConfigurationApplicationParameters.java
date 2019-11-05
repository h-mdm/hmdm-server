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
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.io.Serializable;

@ApiModel(description = "A collection of parameters for linking the single application to single configuration")
@JsonIgnoreProperties(ignoreUnknown = true)
public class ConfigurationApplicationParameters implements Serializable {

    private static final long serialVersionUID = 6239693672327794068L;

    @ApiModelProperty("A record ID")
    private Integer id;

    @ApiModelProperty("A configuration ID")
    private int configurationId;

    @ApiModelProperty("An application ID")
    private int applicationId;

    @ApiModelProperty("A flag indicating if version check must be skipped on device")
    private boolean skipVersionCheck;

    /**
     * <p>Constructs new <code>ConfigurationApplicationParameters</code> instance. This implementation does nothing.</p>
     */
    public ConfigurationApplicationParameters() {
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public int getConfigurationId() {
        return configurationId;
    }

    public void setConfigurationId(int configurationId) {
        this.configurationId = configurationId;
    }

    public int getApplicationId() {
        return applicationId;
    }

    public void setApplicationId(int applicationId) {
        this.applicationId = applicationId;
    }

    public boolean isSkipVersionCheck() {
        return skipVersionCheck;
    }

    public void setSkipVersionCheck(boolean skipVersionCheck) {
        this.skipVersionCheck = skipVersionCheck;
    }

    @Override
    public String toString() {
        return "ConfigurationApplicationParameters{" +
                "id=" + id +
                ", configurationId=" + configurationId +
                ", applicationId=" + applicationId +
                ", skipVersionCheck=" + skipVersionCheck +
                '}';
    }
}
