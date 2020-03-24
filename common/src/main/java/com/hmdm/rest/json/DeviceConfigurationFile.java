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
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * <p>$</p>
 */
@JsonIgnoreProperties(ignoreUnknown = true)
@Data
public class DeviceConfigurationFile {

    /**
     * <p>A logical name of the file.</p>
     */
    @ApiModelProperty("A path to file on device")
    private String path;

    /**
     * <p>A flag indicating if file is to be removed from the device or not.</p>
     */
    @ApiModelProperty("A flag indicating if file is to be removed from the device or not")
    private boolean remove;

    /**
     * <p>A timestamp of file uploading to server (in milliseconds since epoch time).</p>
     */
    @ApiModelProperty("A timestamp of file uploading to server (in milliseconds since epoch time)")
    private Long lastUpdate;

}
