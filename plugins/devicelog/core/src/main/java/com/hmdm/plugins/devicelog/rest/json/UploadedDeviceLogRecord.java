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

package com.hmdm.plugins.devicelog.rest.json;

import io.swagger.annotations.ApiModelProperty;

import java.io.Serializable;

/**
 * <p>A DTO representing a single device log record uploaded by device to server.</p>
 *
 * @author isv
 */
public class UploadedDeviceLogRecord implements Serializable {

    private static final long serialVersionUID = -5572542628689577966L;
    @ApiModelProperty("A timestamp of creation of log record (in milliseconds since epoch time")
    private Long timestamp;

    @ApiModelProperty("A package ID of an application related to log record")
    private String packageId;

    @ApiModelProperty("A severity for log record")
    private int logLevel;

    @ApiModelProperty("A message for log record")
    private String message;

    /**
     * <p>Constructs new <code>UploadedDeviceLogRecord</code> instance. This implementation does nothing.</p>
     */
    public UploadedDeviceLogRecord() {
    }

    public Long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Long timestamp) {
        this.timestamp = timestamp;
    }

    public String getPackageId() {
        return packageId;
    }

    public void setPackageId(String packageId) {
        this.packageId = packageId;
    }

    public int getLogLevel() {
        return logLevel;
    }

    public void setLogLevel(int logLevel) {
        this.logLevel = logLevel;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    @Override
    public String toString() {
        return "UploadedDeviceLogRecord{" +
                "timestamp=" + timestamp +
                ", packageId='" + packageId + '\'' +
                ", logLevel=" + logLevel +
                ", message='" + message + '\'' +
                '}';
    }
}
