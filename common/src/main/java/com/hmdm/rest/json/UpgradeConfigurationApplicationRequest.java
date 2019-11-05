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

@ApiModel(description = "A request to upgrade application for configuration up to recent version")
@JsonIgnoreProperties(ignoreUnknown = true)
public class UpgradeConfigurationApplicationRequest {

    @ApiModelProperty("An ID of a configuration to upgrade application for")
    private Integer configurationId;

    @ApiModelProperty("An ID of an application to upgrade")
    private Integer applicationId;

    /**
     * <p>Constructs new <code>UpgradeConfigurationApplicationRequest</code> instance. This implementation does nothing.</p>
     */
    public UpgradeConfigurationApplicationRequest() {
    }

    public Integer getConfigurationId() {
        return configurationId;
    }

    public void setConfigurationId(Integer configurationId) {
        this.configurationId = configurationId;
    }

    public Integer getApplicationId() {
        return applicationId;
    }

    public void setApplicationId(Integer applicationId) {
        this.applicationId = applicationId;
    }

    @Override
    public String toString() {
        return "UpgradeConfigurationApplicationRequest{" +
                "configurationId=" + configurationId +
                ", applicationId=" + applicationId +
                '}';
    }
}
