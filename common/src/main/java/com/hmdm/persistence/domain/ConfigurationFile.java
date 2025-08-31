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

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.hmdm.rest.json.FileConfigurationLink;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;

/**
 * <p>A configuration file to be sent to mobile device for usage</p>
 */
@ApiModel(description = "A configuration file to be sent to mobile device for usage")
@JsonIgnoreProperties(ignoreUnknown = true)
@Data
public class ConfigurationFile implements Serializable {

    /**
     * <p>An ID of configuration file.</p>
     */
    @ApiModelProperty("A configuration file ID")
//    @JsonIgnore
    private Integer id;

    /**
     * <p>An ID of a configuration record associated with this file.</p>
     */
    @ApiModelProperty(hidden = true)
    @JsonIgnore
    private int configurationId;

    /**
     * <p>A description of the file. Since v5.36.1, determined in UploadedFile linked via fileId</p>
     */
    @ApiModelProperty("A description of the file")
    private String description;

    /**
     * <p>A path to a file on device (including the name of the file). Since v5.36.1, determined in UploadedFile</p>
     */
    @ApiModelProperty("A path to a file on device (including the file name)")
    @JsonProperty("path")
    private String devicePath;

    /**
     * <p>An URL referencing the content of the file available on external resource. This property is mutually exclusive with {
     * @link #filePath} property. Since v5.36.1, determined in UploadedFile</p>
     */
    @ApiModelProperty(hidden = true)
    private String externalUrl;

    /**
     * <p>A path to a file relative to base directory for stored files. This property is mutually exclusive with {
     * @link #externalUrl} property. Since v5.36.1, determined in UploadedFile</p>
     */
    @ApiModelProperty(hidden = true)
    private String filePath;

    /**
     * <p>A checksum for the file content.
     * DEPRECATED since v5.36.1 - checksum isn't used due to possible variable content, use lastUpdate instead
     * </p>
     */
    @ApiModelProperty("A checksum for the file content")
    @Deprecated
    private String checksum;

    /**
     * <p>A flag indicating if file is to be removed from the device or not.
     * </p>
     */
    @ApiModelProperty("A flag indicating if file is to be removed from the device or not")
    private boolean remove;

    /**
     * <p>A timestamp of file uploading to server (in milliseconds since epoch time).
     * Since v5.36.1, determined in UploadedFile
     * </p>
     */
    @ApiModelProperty("A timestamp of file uploading to server (in milliseconds since epoch time)")
    private Long lastUpdate;

    /**
     * <p>An ID of an uploaded file storing the content of the file.</p>
     */
    @ApiModelProperty(hidden = true)
    private Integer fileId;

    /**
     * <p>An URL referencing the content of the file. Since v5.36.1, determined in UploadedFile</p>
     */
    @ApiModelProperty("An URL referencing the content of the file")
    private String url;

    /**
     * <p>A flag indicating whether the file content must be updated by device-specific values.
     * Since v5.36.1, determined in UploadedFile</p>
     */
    @ApiModelProperty("A flag indicating whether the file content must be updated by device-specific values")
    private boolean replaceVariables;

    /**
     * Default constructor
     */
    public ConfigurationFile() {}

    /**
     * Constructor from Link (could be from File and Link in the future)
     */
    public ConfigurationFile(FileConfigurationLink link) {
        configurationId = link.getConfigurationId();
        fileId = link.getFileId();
        remove = link.isRemove();
    }
}
