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

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.io.Serializable;

@ApiModel(description = "A request for searching the devices")
@JsonIgnoreProperties(value = {"customerId", "userId"}, ignoreUnknown = true)
public class DeviceSearchRequest implements Serializable {

    private static final long serialVersionUID = -131621879714920126L;
    @ApiModelProperty("A filter to search devices")
    private String value;

    @ApiModelProperty("An ID of a group to search devices for")
    private Integer groupId;

    @ApiModelProperty("An ID of a configuration to search devices for")
    private Integer configurationId;

    @ApiModelProperty(hidden = true)
    private int customerId;

    @ApiModelProperty(hidden = true)
    private int userId;

    /**
     * <p>A number of records per single page of data to be retrieved.</p>
     */
    @ApiModelProperty("A number of records per single page of data to be retrieved")
    private int pageSize = 50;

    /**
     * <p>A number of page of data to be retrieved.</p>
     */
    @ApiModelProperty("A number of page of data to be retrieved (1-based)")
    private int pageNum = 1;

    public DeviceSearchRequest() {
    }

    public String getValue() {
        return this.value != null && !this.value.trim().isEmpty() ? "%" + this.value + "%" : null;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public Integer getGroupId() {
        return this.groupId;
    }

    public void setGroupId(Integer groupId) {
        this.groupId = groupId;
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

    public int getPageSize() {
        return pageSize;
    }

    public void setPageSize(int pageSize) {
        this.pageSize = pageSize;
    }

    public int getPageNum() {
        return pageNum;
    }

    public void setPageNum(int pageNum) {
        this.pageNum = pageNum;
    }

    public Integer getConfigurationId() {
        return configurationId;
    }

    public void setConfigurationId(Integer configurationId) {
        this.configurationId = configurationId;
    }

    @Override
    public String toString() {
        return "DeviceSearchRequest{" +
                "value='" + value + '\'' +
                ", groupId=" + groupId +
                ", configurationId=" + configurationId +
                ", customerId=" + customerId +
                ", userId=" + userId +
                ", pageSize=" + pageSize +
                ", pageNum=" + pageNum +
                '}';
    }
}
