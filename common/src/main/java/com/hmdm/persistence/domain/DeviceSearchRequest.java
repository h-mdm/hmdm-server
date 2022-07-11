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
     * <p>A timestamp for <code>FROM</code> boundary for filtering the data records by last online dates.</p>
     */
    @ApiModelProperty("A timestamp for FROM boundary for filtering the data records by last online dates")
    private Date dateFrom;

    /**
     * <p>A timestamp for <code>TO</code> boundary for filtering the data records by last online dates.</p>
     */
    @ApiModelProperty("A timestamp for TO boundary for filtering the data records by last online dates")
    private Date dateTo;

    /**
     * <p>Age in milliseconds for selection of devices being online earlier</p>
     */
    private Long onlineEarlierMillis;

    /**
     * <p>Age in milliseconds for selection of devices being online later</p>
     */
    private Long onlineLaterMillis;

    /**
     * <p>A timestamp for <code>FROM</code> boundary for filtering the data records by enrollment dates.</p>
     */
    @ApiModelProperty("A timestamp for FROM boundary for filtering the data records by enrollment dates")
    private Date enrollmentDateFrom;

    /**
     * <p>A timestamp for <code>TO</code> boundary for filtering the data records by enrollment dates.</p>
     */
    @ApiModelProperty("A timestamp for TO boundary for filtering the data records by enrollment dates")
    private Date enrollmentDateTo;

    /**
     * <p>A condition for filtering the data records by mdm mode.</p>
     */
    @ApiModelProperty("A condition for filtering the data records by mdm mode")
    private Boolean mdmMode;

    /**
     * <p>A condition for filtering the data records by kiosk mode.</p>
     */
    @ApiModelProperty("A condition for filtering the data records by kiosk mode")
    private Boolean kioskMode;

    /**
     * <p>A filter for Android version.</p>
     */
    @ApiModelProperty("A filter for Android version")
    private String androidVersion;

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

    /**
     * <p>A flag of fast searching by device number.</p>
     */
    @ApiModelProperty("Flag of fast searching by device number")
    private boolean fastSearch;

    public DeviceSearchRequest() {
    }

    public String getValue() {
        String v = this.value != null && !this.value.trim().isEmpty() ? this.value : null;
        if (!fastSearch && v != null) {
            return "%" + v + "%";
        } else {
            return v;
        }
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

    public Long getOnlineEarlierMillis() {
        return onlineEarlierMillis;
    }

    public void setOnlineEarlierMillis(Long onlineEarlierMillis) {
        this.onlineEarlierMillis = onlineEarlierMillis;
    }

    public Long getOnlineLaterMillis() {
        return onlineLaterMillis;
    }

    public void setOnlineLaterMillis(Long onlineLaterMillis) {
        this.onlineLaterMillis = onlineLaterMillis;
    }

    public Date getEnrollmentDateFrom() {
        return enrollmentDateFrom;
    }

    public void setEnrollmentDateFrom(Date enrollmentDateFrom) {
        this.enrollmentDateFrom = enrollmentDateFrom;
    }

    public Date getEnrollmentDateTo() {
        return enrollmentDateTo;
    }

    public void setEnrollmentDateTo(Date enrollmentDateTo) {
        this.enrollmentDateTo = enrollmentDateTo;
    }

    public Boolean getMdmMode() {
        return mdmMode;
    }

    public void setMdmMode(Boolean mdmMode) {
        this.mdmMode = mdmMode;
    }

    public Boolean getKioskMode() {
        return kioskMode;
    }

    public void setKioskMode(Boolean kioskMode) {
        this.kioskMode = kioskMode;
    }

    public String getAndroidVersion() {
        return androidVersion;
    }

    public void setAndroidVersion(String androidVersion) {
        this.androidVersion = androidVersion;
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

    public boolean isFastSearch() {
        return fastSearch;
    }

    public void setFastSearch(boolean fastSearch) {
        this.fastSearch = fastSearch;
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

    public long getEnrollmentDateFromMillis() {
        if (enrollmentDateFrom != null) {
            return enrollmentDateFrom.getTime();
        } else {
            return 0;
        }
    }

    public long getEnrollmentDateToMillis() {
        if (enrollmentDateTo != null) {
            return enrollmentDateTo.getTime();
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
                ", enrollmentDateFrom=" + enrollmentDateFrom +
                ", enrollmentDateTo=" + enrollmentDateTo +
                ", onlineEarlierMillis=" + onlineEarlierMillis +
                ", onlineLaterMillis=" + onlineLaterMillis +
                ", mdmMode=" + mdmMode +
                ", kioskMode=" + kioskMode +
                ", androidVersion=" + androidVersion +
                ", installationStatus=" + installationStatus +
                '}';
    }
}
