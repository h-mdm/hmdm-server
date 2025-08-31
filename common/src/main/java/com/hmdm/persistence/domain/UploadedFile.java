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
import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

import java.io.File;
import java.io.Serializable;
import java.nio.file.Path;
import java.nio.file.Paths;

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

    @ApiModelProperty("A path to a file relative to base directory for stored files, including the file name")
    private String filePath;

    /**
     * <p>A description of the file.</p>
     */
    @ApiModelProperty("A description of the file")
    private String description;

    @ApiModelProperty("A timestamp of file uploading (in milliseconds since epoch time), should be equal to the timestamp in the file system")
    private long uploadTime;

    /**
     * DEPRECATED since v5.36.1 - checksum isn't used due to possible variable content, use lastUpdate instead
     */
    @ApiModelProperty("An optional checksum of the file content")
    @Deprecated
    private String checksum;

    private String url;

    /**
     * <p>A path to a file on device (including the name of the file).</p>
     */
    @ApiModelProperty("A path to a file on device (including the file name)")
    private String devicePath;

    /**
     * <p>A flag indicating whether the file is using the external URL instead of being uploaded.
     * Notice: "external" is not a reserved keyword in PostgreSQL but reserved in other SQL dialects
     * </p>
     */
    @ApiModelProperty("A flag indicating whether the file is using the external URL instead of being uploaded.")
    private boolean external;

    /**
     * <p>An URL referencing the content of the file available on external resource, if a file is marked as external</p>
     */
    @ApiModelProperty("An external URL referencing the content of the file")
    private String externalUrl;

    /**
     * <p>A flag indicating whether the file content must be updated by device-specific values.
     */
    @ApiModelProperty("A flag indicating whether the file content must be updated by device-specific values")
    private boolean replaceVariables;

    private String tmpPath;

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

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
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

    public String getDevicePath() {
        return devicePath;
    }

    public void setDevicePath(String devicePath) {
        this.devicePath = devicePath;
    }

    public boolean isExternal() {
        return external;
    }

    public void setExternal(boolean external) {
        this.external = external;
    }

    public String getExternalUrl() {
        return externalUrl;
    }

    public void setExternalUrl(String externalUrl) {
        this.externalUrl = externalUrl;
    }

    public boolean isReplaceVariables() {
        return replaceVariables;
    }

    public void setReplaceVariables(boolean replaceVariables) {
        this.replaceVariables = replaceVariables;
    }

    public String getTmpPath() {
        return tmpPath;
    }

    public void setTmpPath(String tmpPath) {
        this.tmpPath = tmpPath;
    }

    public File getFileByPath(String filesDirectory, Customer customer) {
        final String customerFilesBaseDir = customer.getFilesDir();
        if (customerFilesBaseDir != null && !customerFilesBaseDir.isEmpty()) {
            Path path = Paths.get(filesDirectory, customerFilesBaseDir, this.filePath);
            return path.toFile();
        } else {
            Path path = Paths.get(filesDirectory, this.filePath);
            return path.toFile();
        }
    }

    public String getUrl(String baseUrl, Customer customer) {
        if (this.isExternal()) {
            return this.externalUrl;
        }
        final String customerFilesBaseDir = customer.getFilesDir();
        if (customerFilesBaseDir != null && !customerFilesBaseDir.isEmpty()) {
            return String.format("%s/files/%s/%s", baseUrl, customerFilesBaseDir, this.filePath.replace(File.separator, "/"));
        } else {
            return String.format("%s/files/%s", baseUrl, this.filePath.replace(File.separator, "/"));
        }
    }

    @Override
    public String toString() {
        return "UploadedFile{" +
                "id=" + id +
                ", customerId=" + customerId +
                ", filePath='" + filePath + '\'' +
                ", description='" + description + '\'' +
                ", uploadTime=" + uploadTime +
                ", devicePath='" + devicePath + '\'' +
                ", external=" + external +
                ", externalUrl='" + externalUrl + '\'' +
                ", replaceVariables=" + replaceVariables +
                ", url=" + url +
                '}';
    }
}
