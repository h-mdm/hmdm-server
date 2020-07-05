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

import com.hmdm.service.DeviceApplicationsStatus;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.io.Serializable;
import java.util.Date;

@ApiModel(description = "A request for searching the devices")
@JsonIgnoreProperties(value = {"customerId", "userId"}, ignoreUnknown = true)
public class DeviceSearchRequest implements Serializable {

    private static final long serialVersionUID = -8435796711101758494L;
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

    /**
     * <p>A column to sort results by.</p>
     */
    @ApiModelProperty("A column to sort devices list by")
    private DeviceListSortBy sortBy;

    /**
     * <p>A direction to sort results by.</p>
     */
    @ApiModelProperty("A direction to sort devices list by")
    private String sortDir = "ASC";

    /**
     * <p>A timestamp for <code>FROM</code> boundary for filtering the data records by dates.</p>
     */
    @ApiModelProperty("A timestamp for FROM boundary for filtering the data records by dates")
    private Date dateFrom;

    /**
     * <p>A timestamp for <code>TO</code> boundary for filtering the data records by dates.</p>
     */
    @ApiModelProperty("A timestamp for TO boundary for filtering the data records by dates")
    private Date dateTo;

    /**
     * <p>A filter for launcher version.</p>
     */
    @ApiModelProperty("A filter for launcher version")
    private String launcherVersion;

    /**
     * <p>A filter for launcher version.</p>
     */
    @ApiModelProperty("A filter for application installation status")
    private DeviceApplicationsStatus installationStatus;

    /**
     * <p>A filter for recent IMEI change.</p>
     */
    @ApiModelProperty("A filter for recent IMEI change")
    private boolean imeiChanged;

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

    public DeviceListSortBy getSortBy() {
        return sortBy;
    }

    public void setSortBy(DeviceListSortBy sortBy) {
        this.sortBy = sortBy;
    }

    public String getSortDir() {
        return sortDir;
    }

    public void setSortDir(String sortDir) {
        this.sortDir = sortDir;
    }

    public Date getDateFrom() {
        return dateFrom;
    }

    public void setDateFrom(Date dateFrom) {
        this.dateFrom = dateFrom;
    }

    public Date getDateTo() {
        return dateTo;
    }

    public void setDateTo(Date dateTo) {
        this.dateTo = dateTo;
    }

    public String getLauncherVersion() {
        return launcherVersion;
    }

    public void setLauncherVersion(String launcherVersion) {
        this.launcherVersion = launcherVersion;
    }

    public DeviceApplicationsStatus getInstallationStatus() {
        return installationStatus;
    }

    public void setInstallationStatus(DeviceApplicationsStatus installationStatus) {
        this.installationStatus = installationStatus;
    }

    public boolean isImeiChanged() {
        return imeiChanged;
    }

    public void setImeiChanged(boolean imeiChanged) {
        this.imeiChanged = imeiChanged;
    }

    public long getDateFromMillis() {
        if (dateFrom != null) {
            return dateFrom.getTime();
        } else {
            return 0;
        }
    }

    public long getDateToMillis() {
        if (dateTo != null) {
            return dateTo.getTime();
        } else {
            return 0;
        }
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
                ", sortBy=" + sortBy +
                ", sortDir=" + sortDir +
                ", dateFrom=" + dateFrom +
                ", dateTo=" + dateTo +
                ", launcherVersion=" + launcherVersion +
                ", installationStatus=" + installationStatus +
                '}';
    }
}
