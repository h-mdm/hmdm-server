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
import com.hmdm.persistence.domain.CustomerData;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

import java.io.Serializable;

@ApiModel(description = "A link between the file and the configuration")
@JsonIgnoreProperties(ignoreUnknown = true)
public class FileConfigurationLink implements CustomerData, Serializable {

    @ApiModelProperty(value = "An ID of a link between the file and configuration. " +
            "May be null if those are not linked", required = false)
    private Integer id;
    @ApiModelProperty("An ID of a customer account which both the file and configuration belong to")
    private int customerId;
    @ApiModelProperty("An ID of a configuration")
    private int configurationId;
    @ApiModelProperty("A name of a configuration")
    private String configurationName;
    @ApiModelProperty("An ID of a file")
    private int fileId;
    @ApiModelProperty("A name of a file")
    private String fileName;
    @ApiModelProperty(value = "A flag indicating that file is to be uploaded to device in the configuration")
    private boolean upload;
    @ApiModelProperty(value = "A flag indicating that file is to be removed from the configuration")
    private boolean remove;
    @ApiModelProperty(value = "Set by front-end when the configuration needs to be notified about changes")
    private boolean notify;

    /**
     * <p>Constructs new <code>FileConfigurationLink</code> instance. This implementation does nothing.</p>
     */
    public FileConfigurationLink() {
    }

    @Override
    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    @Override
    public int getCustomerId() {
        return customerId;
    }

    @Override
    public void setCustomerId(int customerId) {
        this.customerId = customerId;
    }

    public int getConfigurationId() {
        return configurationId;
    }

    public void setConfigurationId(int configurationId) {
        this.configurationId = configurationId;
    }

    public String getConfigurationName() {
        return configurationName;
    }

    public void setConfigurationName(String configurationName) {
        this.configurationName = configurationName;
    }

    public int getFileId() {
        return fileId;
    }

    public void setFileId(int fileId) {
        this.fileId = fileId;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public boolean isUpload() {
        return upload;
    }

    public void setUpload(boolean upload) {
        this.upload = upload;
    }

    public boolean isRemove() {
        return remove;
    }

    public void setRemove(boolean remove) {
        this.remove = remove;
    }

    public boolean isNotify() {
        return notify;
    }

    public void setNotify(boolean notify) {
        this.notify = notify;
    }

    @Override
    public String toString() {
        return "FileConfigurationLink{" +
                "id=" + id +
                ", customerId=" + customerId +
                ", configurationId=" + configurationId +
                ", configurationName='" + configurationName + '\'' +
                ", fileId=" + fileId +
                ", fileName='" + fileName + '\'' +
                ", upload=" + upload +
                ", remove=" + remove +
                ", notify=" + notify +
                '}';
    }
}
