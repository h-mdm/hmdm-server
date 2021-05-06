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

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.hmdm.persistence.domain.ConfigurationFile;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

import java.io.Serializable;

@ApiModel(description = "A single configuration file to be used on mobile device and used in data " +
        "synchronization between mobile device and server application")
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class SyncConfigurationFile implements Serializable, SyncConfigurationFileInt {

    @JsonIgnore
    private final ConfigurationFile wrapped;

    /**
     * <p>Constructs new <code>SyncConfigurationFile</code> instance. This implementation does nothing.</p>
     */
    public SyncConfigurationFile(ConfigurationFile file) {
        this.wrapped = file;
    }

    /**
     * <p>A description of the file.</p>
     */
    @Override
    @ApiModelProperty("A description of the file")
    public String getDescription() {
        //return wrapped.getDescription();
        // Not required in the mobile app
        return null;
    }

    /**
     * <p>A checksum for the file content.</p>
     */
    @Override
    @ApiModelProperty("A checksum for the file content")
    public String getChecksum() {
        return wrapped.getChecksum();
    }

    /**
     * <p>A flag indicating if file is to be removed from the device or not.</p>
     */
    @Override
    @ApiModelProperty("A flag indicating if file is to be removed from the device or not")
    public Boolean getRemove() {
        return wrapped.isRemove() ? true : null;
    }

    /**
     * <p>A timestamp of file uploading to server (in milliseconds since epoch time).</p>
     */
    @Override
    @ApiModelProperty("A timestamp of file uploading to server (in milliseconds since epoch time)")
    public Long getLastUpdate() {
        return wrapped.getLastUpdate();
    }

    /**
     * <p>A path to a file on device (including the name of the file).</p>
     */
    @ApiModelProperty("A path to a file on device")
    public String getPath() {
        return wrapped.getDevicePath();
    }

    /**
     * <p>An URL referencing the content of the file.</p>
     */
    @ApiModelProperty("An URL referencing the content of the file")
    public String getUrl() {
        return wrapped.getUrl();
    }

    /**
     * <p>A flag indicating if file is to be removed from the device or not.</p>
     */
    @Override
    @ApiModelProperty("A flag indicating whether the file content must be updated by device-specific values")
    public Boolean getVarContent() {
        return wrapped.isReplaceVariables() ? true : null;
    }
}
