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

package com.hmdm.rest.json.view;

import com.hmdm.persistence.domain.Customer;
import com.hmdm.persistence.domain.UploadedFile;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.io.File;
import java.io.Serializable;
import java.util.List;

@ApiModel(description = "A single file maintained by the MDM server")
@JsonIgnoreProperties(ignoreUnknown = true)
public class FileView implements Serializable {

    private static final long serialVersionUID = 7570897379289300175L;

    @ApiModelProperty("Id of the basic UploadedFile object")
    private Integer id;
    @ApiModelProperty("A path to file including the file name")
    private String filePath;
    @ApiModelProperty("An optional file description")
    private String description;
    @ApiModelProperty("An URL of file")
    private String url;
    @ApiModelProperty("File size in bytes")
    private long size;
    @ApiModelProperty("Last update time in ms")
    private long uploadTime;
    @ApiModelProperty("File path on the device")
    private String devicePath;
    @ApiModelProperty("A flag showing whether the file has an external URL")
    private boolean external;
    @ApiModelProperty("A flag showing whether the file has variable content")
    private boolean replaceVariables;

    // APKs are not displayed in the Files section since v5.36.1
    @Deprecated
    private List<String> usedByApps;
    private List<String> usedByIcons;
    private List<String> usedByConfigurations;

    public FileView() {
    }

    public FileView(String path, String name, String url, long size) {
        this.filePath = path + (path.length() > 0 ? "/" : "") + name;
        this.url = url;
        this.size = size;
    }

    public FileView(UploadedFile f, String baseUrl, String filesDirectory, Customer customer) {
        setId(f.getId());
        setDescription(f.getDescription());
        setUrl(f.getUrl(baseUrl, customer));
        if (!f.isExternal()) {
            setFilePath(f.getFilePath().replace(File.separator, "/"));
            File file = f.getFileByPath(filesDirectory, customer);
            if (!file.exists()) {
                setSize(-1);
            } else {
                setSize(file.length());
            }
            setUploadTime(f.getUploadTime());
        }
        setDevicePath(f.getDevicePath());
        setExternal(f.isExternal());
        setReplaceVariables(f.isReplaceVariables());
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getFilePath() {
        return this.filePath;
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

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public long getSize() {
        return size;
    }

    public void setSize(long size) {
        this.size = size;
    }

    public long getUploadTime() {
        return uploadTime;
    }

    public void setUploadTime(long uploadTime) {
        this.uploadTime = uploadTime;
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

    public boolean isReplaceVariables() {
        return replaceVariables;
    }

    public void setReplaceVariables(boolean replaceVariables) {
        this.replaceVariables = replaceVariables;
    }

    public List<String> getUsedByApps() {
        return usedByApps;
    }

    public void setUsedByApps(List<String> usedByApps) {
        this.usedByApps = usedByApps;
    }

    public List<String> getUsedByIcons() {
        return usedByIcons;
    }

    public void setUsedByIcons(List<String> usedByIcons) {
        this.usedByIcons = usedByIcons;
    }

    public List<String> getUsedByConfigurations() {
        return usedByConfigurations;
    }

    public void setUsedByConfigurations(List<String> usedByConfigurations) {
        this.usedByConfigurations = usedByConfigurations;
    }
}
