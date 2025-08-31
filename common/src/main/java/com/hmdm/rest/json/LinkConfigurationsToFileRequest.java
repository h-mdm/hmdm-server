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
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

import java.util.List;

@ApiModel(description = "A request to setup links between the single file and listed configurations")
@JsonIgnoreProperties(ignoreUnknown = true)
public class LinkConfigurationsToFileRequest {

    @ApiModelProperty("An ID of a file  to link configurations to")
    private int fileId;
    @ApiModelProperty("A list of configurations to link to application")
    private List<FileConfigurationLink> configurations;

    /**
     * <p>Constructs new <code>LinkConfigurationsToFileRequest</code> instance. This implementation does nothing.</p>
     */
    public LinkConfigurationsToFileRequest() {
    }

    public int getFileId() {
        return fileId;
    }

    public void setFileId(int fileId) {
        this.fileId = fileId;
    }

    public List<FileConfigurationLink> getConfigurations() {
        return configurations;
    }

    public void setConfigurations(List<FileConfigurationLink> configurations) {
        this.configurations = configurations;
    }
}
