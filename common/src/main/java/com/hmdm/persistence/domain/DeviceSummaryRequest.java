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
import com.hmdm.service.DeviceApplicationsStatus;
import com.hmdm.service.DeviceConfigFilesStatus;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

@ApiModel(description = "A request for getting the device summary")
@JsonIgnoreProperties(value = {"customerId", "userId"}, ignoreUnknown = true)
public class DeviceSummaryRequest implements Serializable {

    private static final long serialVersionUID = -8435796711101693827L;

    public DeviceSummaryRequest() {}

    public DeviceSummaryRequest(int userId,
                                 int customerId,
                                 DeviceConfigFilesStatus fileStatus,
                                 DeviceApplicationsStatus appStatus,
                                 Long minEnrollTime,
                                 Long maxEnrollTime,
                                 Long minOnlineTime,
                                 Long maxOnlineTime,
                                 List<Integer> configIds) {
        this.userId = userId;
        this.customerId = customerId;
        this.fileStatus = fileStatus;
        this.appStatus = appStatus;
        this.minEnrollTime = minEnrollTime;
        this.maxEnrollTime = maxEnrollTime;
        this.minOnlineTime = minOnlineTime;
        this.maxOnlineTime = maxOnlineTime;
        this.configIds = configIds;
    }

    @ApiModelProperty("Filter by file status")
    private DeviceConfigFilesStatus fileStatus;

    @ApiModelProperty("Filter by app status")
    private DeviceApplicationsStatus appStatus;

    @ApiModelProperty("Filter by min enroll time")
    private Long minEnrollTime;

    @ApiModelProperty("Filter by max enroll time")
    private Long maxEnrollTime;

    @ApiModelProperty("Filter by min online time")
    private Long minOnlineTime;

    @ApiModelProperty("Filter by max online time")
    private Long maxOnlineTime;

    @ApiModelProperty(hidden = true)
    private int customerId;

    @ApiModelProperty(hidden = true)
    private int userId;

    @ApiModelProperty("Filter by certain configurations only")
    private List<Integer> configIds;

    public DeviceConfigFilesStatus getFileStatus() {
        return fileStatus;
    }

    public void setFileStatus(DeviceConfigFilesStatus fileStatus) {
        this.fileStatus = fileStatus;
    }

    public DeviceApplicationsStatus getAppStatus() {
        return appStatus;
    }

    public void setAppStatus(DeviceApplicationsStatus appStatus) {
        this.appStatus = appStatus;
    }

    public Long getMinEnrollTime() {
        return minEnrollTime;
    }

    public void setMinEnrollTime(Long minEnrollTime) {
        this.minEnrollTime = minEnrollTime;
    }

    public Long getMaxEnrollTime() {
        return maxEnrollTime;
    }

    public void setMaxEnrollTime(Long maxEnrollTime) {
        this.maxEnrollTime = maxEnrollTime;
    }

    public Long getMinOnlineTime() {
        return minOnlineTime;
    }

    public void setMinOnlineTime(Long minOnlineTime) {
        this.minOnlineTime = minOnlineTime;
    }

    public Long getMaxOnlineTime() {
        return maxOnlineTime;
    }

    public void setMaxOnlineTime(Long maxOnlineTime) {
        this.maxOnlineTime = maxOnlineTime;
    }

    public int getCustomerId() {
        return customerId;
    }

    public void setCustomerId(int customerId) {
        this.customerId = customerId;
    }

    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    public List<Integer> getConfigIds() {
        return configIds;
    }

    public void setConfigIds(List<Integer> configIds) {
        this.configIds = configIds;
    }
}
