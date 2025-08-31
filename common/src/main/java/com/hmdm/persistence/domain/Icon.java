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

/**
 * <p>A specification of a single icon used to represent an application on mobile device</p>
 *
 * @author isv
 */
@ApiModel(description = "A specification of a single icon used to represent an application on mobile device")
@JsonIgnoreProperties(ignoreUnknown = true)
public class Icon implements Serializable, CustomerData {

    private static final long serialVersionUID = -5082987201236988017L;
    @ApiModelProperty("An application ID")
    private Integer id;
    @ApiModelProperty(hidden = true)
    private int customerId;
    @ApiModelProperty("A name of the icon")
    private String name;
    @ApiModelProperty("An ID of an uploaded file storing the content of the icon")
    private Integer fileId;
    @ApiModelProperty("The name of an uploaded file storing the content of the icon")
    private String fileName;

    /**
     * <p>Constructs new <code>Icon</code> instance. This implementation does nothing.</p>
     */
    public Icon() {
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

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Integer getFileId() {
        return fileId;
    }

    public void setFileId(Integer fileId) {
        this.fileId = fileId;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    @Override
    public String toString() {
        return "Icon{" +
                "id=" + id +
                ", customerId=" + customerId +
                ", name='" + name + '\'' +
                ", fileId='" + fileId + '\'' +
                ", fileName='" + fileName + '\'' +
                '}';
    }
}
