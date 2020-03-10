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

package com.hmdm.rest.json.view.devicelist;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.hmdm.persistence.domain.ConfigurationFile;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

import java.io.Serializable;

/**
 * <p>A wrapper around the {@link ConfigurationFile} object providing the view suitable for the <code>Device List</code>
 * view of configuration file.</p>
 *
 * @author isv
 */
@JsonIgnoreProperties(value = {"file"}, ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
@ApiModel(description = "A specification of a single configuration file available for usage on mobile device")
public class ConfigurationFileView implements Serializable {

    /**
     * <p>A wrapped file object.</p>
     */
    private final ConfigurationFile file;

    /**
     * <p>Constructs new <code>ConfigurationFileView</code> instance. This implementation does nothing.</p>
     */
    public ConfigurationFileView(ConfigurationFile file) {
        this.file = file;
    }

    /**
     * <p>A flag indicating if file is to be removed from the device or not.</p>
     */
    @ApiModelProperty("A flag indicating if file is to be removed from the device or not")
    public boolean isRemove() {
        return file.isRemove();
    }

    /**
     * <p>A timestamp of file uploading to server (in milliseconds since epoch time).</p>
     */
    @ApiModelProperty("A timestamp of file uploading to server (in milliseconds since epoch time)")
    public Long getLastUpdate() {
        return file.getLastUpdate();
    }

    /**
     * <p>A path to a file on device (including the name of the file).</p>
     */
    @ApiModelProperty("A path to a file on device")
    @JsonProperty("path")
    public String getDevicePath() {
        return file.getDevicePath();
    }
}
