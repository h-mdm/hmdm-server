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

package com.hmdm.plugins.devicelog.model;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.io.Serializable;
import java.util.Date;

@ApiModel(description = "A single log record received from device")
@JsonIgnoreProperties(ignoreUnknown = true)
public abstract class DeviceLogRecord implements Serializable {

    private static final long serialVersionUID = 7956573806273714751L;
    
    @ApiModelProperty("A timestamp of creation of log record (in milliseconds since epoch time")
    private Long createTime;

    @ApiModelProperty("An ID of an application related to log record")
    private int applicationId;

    @ApiModelProperty("An ID of a device related to log record")
    private int deviceId;

    @ApiModelProperty("An IP-address for the originating request")
    private String ipAddress;

    @ApiModelProperty("A severity for log record")
    private LogLevel severity;

    @ApiModelProperty("A message for log record")
    private String message;

    @ApiModelProperty("A device identifier")
    private String deviceNumber;

    @ApiModelProperty("A package ID for application")
    private String applicationPkg;

    /**
     * <p>Constructs new <code>DeviceLogRecord</code> instance. This implementation does nothing.</p>
     */
    public DeviceLogRecord() {
    }

    public Long getCreateTime() {
        return createTime;
    }

    public void setCreateTime(Long createTime) {
        this.createTime = createTime;
    }

    public int getApplicationId() {
        return applicationId;
    }

    public void setApplicationId(int applicationId) {
        this.applicationId = applicationId;
    }

    public int getDeviceId() {
        return deviceId;
    }

    public void setDeviceId(int deviceId) {
        this.deviceId = deviceId;
    }

    public String getIpAddress() {
        return ipAddress;
    }

    public void setIpAddress(String ipAddress) {
        this.ipAddress = ipAddress;
    }

    public LogLevel getSeverity() {
        return severity;
    }

    public void setSeverity(LogLevel severity) {
        this.severity = severity;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getDeviceNumber() {
        return deviceNumber;
    }

    public void setDeviceNumber(String deviceNumber) {
        this.deviceNumber = deviceNumber;
    }

    public String getApplicationPkg() {
        return applicationPkg;
    }

    public void setApplicationPkg(String applicationPkg) {
        this.applicationPkg = applicationPkg;
    }

    /**
     * <p>Gets the unique identifier for this record within underlying persistence layer.</p>
     *
     * @return an identifier for this record.
     */
    public abstract String getIdentifier();

    @Override
    public String toString() {
        return "DeviceLogRecord{" +
                "createTime=" + createTime +
                ", applicationId=" + applicationId +
                ", deviceId=" + deviceId +
                ", ipAddress='" + ipAddress + '\'' +
                ", severity=" + severity +
                ", message='" + message + '\'' +
                ", deviceNumber='" + deviceNumber + '\'' +
                ", applicationPkg='" + applicationPkg + '\'' +
                '}';
    }
}
