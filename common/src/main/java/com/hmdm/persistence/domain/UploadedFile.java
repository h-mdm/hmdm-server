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
 * <p>A specification of a single file uploaded to server by client.</p>
 *
 * @author isv
 */
@ApiModel(description = "A specification of a single file uploaded to server by client")
@JsonIgnoreProperties(ignoreUnknown = true)
public class UploadedFile implements Serializable, CustomerData {

    private static final long serialVersionUID = 963786599631403403L;

    @ApiModelProperty("An application ID")
    private Integer id;

    @ApiModelProperty(hidden = true)
    private int customerId;

    @ApiModelProperty("A path to a file relative to base directory for stored files")
    private String filePath;

    @ApiModelProperty("A timestamp of file uploading (in milliseconds since epoch time)")
    private Long uploadTime;

    @ApiModelProperty("An optional checksum of the file content")
    private String checksum;

    private String url;

    /**
     * <p>Constructs new <code>UploadedFile</code> instance. This implementation does nothing.</p>
     */
    public UploadedFile() {
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

    public String getFilePath() {
        return filePath;
    }

    public void setFilePath(String filePath) {
        this.filePath = filePath;
    }

    public Long getUploadTime() {
        return uploadTime;
    }

    public void setUploadTime(Long uploadTime) {
        this.uploadTime = uploadTime;
    }

    public String getChecksum() {
        return checksum;
    }

    public void setChecksum(String checksum) {
        this.checksum = checksum;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    @Override
    public String toString() {
        return "UploadedFile{" +
                "id=" + id +
                ", customerId=" + customerId +
                ", filePath='" + filePath + '\'' +
                ", uploadTime=" + uploadTime +
                ", checksum=" + checksum +
                ", url=" + url +
                '}';
    }
}
