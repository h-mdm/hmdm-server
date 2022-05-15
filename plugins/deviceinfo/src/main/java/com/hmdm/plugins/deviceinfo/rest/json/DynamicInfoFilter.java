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

package com.hmdm.plugins.deviceinfo.rest.json;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

import java.io.Serializable;
import java.util.Date;

/**
 * <p>$</p>
 *
 * @author isv
 */
@ApiModel(description = "A request for searching the dynamic info records for device")
@JsonIgnoreProperties(value = {"deviceId"}, ignoreUnknown = true)
public class DynamicInfoFilter implements Serializable {

    private static final long serialVersionUID = 1321492463446573290L;
    @ApiModelProperty(hidden = true)
    private int deviceId;

    /**
     * <p>A device identifier.</p>
     */
    @ApiModelProperty("A device identifier")
    private String deviceNumber;

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
     * <p>A timestamp for <code>FROM</code> boundary for filtering the data records by dates.</p>
     */
    @ApiModelProperty("A timestamp for FROM boundary for filtering the data records by dates")
    private Date dateFrom;

    /**
     * <p>A timestamp for <code>TO</code> boundary for filtering the data records by dates.</p>
     */
    @ApiModelProperty("A timestamp for TO boundary for filtering the data records by dates")
    private Date dateTo;

    @ApiModelProperty("A fixed interval is to be used for searching the records (in seconds)")
    private Integer fixedInterval = 24 * 3600;

    @ApiModelProperty("A flag indicating if a fixed interval is to be used for searching the records")
    private boolean useFixedInterval = true;

    /**
     * <p>Constructs new <code>DynamicInfoFilter</code> instance. This implementation does nothing.</p>
     */
    public DynamicInfoFilter() {
    }

    public int getDeviceId() {
        return deviceId;
    }

    public void setDeviceId(int deviceId) {
        this.deviceId = deviceId;
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

    public String getDeviceNumber() {
        return deviceNumber;
    }

    public void setDeviceNumber(String deviceNumber) {
        this.deviceNumber = deviceNumber;
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

    public Integer getFixedInterval() {
        return fixedInterval;
    }

    public void setFixedInterval(Integer fixedInterval) {
        this.fixedInterval = fixedInterval;
    }

    public boolean isUseFixedInterval() {
        return useFixedInterval;
    }

    public void setUseFixedInterval(boolean useFixedInterval) {
        this.useFixedInterval = useFixedInterval;
    }
}
