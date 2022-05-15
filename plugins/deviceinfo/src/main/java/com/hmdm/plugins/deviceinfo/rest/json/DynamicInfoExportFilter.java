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
import java.util.Arrays;
import java.util.Date;

/**
 * <p>A filter for searching the dynamic info records for device for export.</p>
 *
 * @author isv
 */
@ApiModel(description = "A filter for searching the dynamic info records for device for export")
@JsonIgnoreProperties(value = {"deviceId"}, ignoreUnknown = true)
public class DynamicInfoExportFilter implements Serializable {

    private static final long serialVersionUID = -2707690119899104358L;
    @ApiModelProperty(hidden = true)
    private int deviceId;

    /**
     * <p>A device identifier.</p>
     */
    @ApiModelProperty("A device identifier")
    private String deviceNumber;

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

    @ApiModelProperty("A list of names of record fields to be exported")
    private String[] fields;

    /**
     * <p>A locale used for localizing the generated content.</p>
     */
    private String locale;

    /**
     * <p>Constructs new <code>DynamicInfoExportFilter</code> instance. This implementation does nothing.</p>
     */
    public DynamicInfoExportFilter() {
    }

    public int getDeviceId() {
        return deviceId;
    }

    public void setDeviceId(int deviceId) {
        this.deviceId = deviceId;
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

    public String[] getFields() {
        return fields;
    }

    public void setFields(String[] fields) {
        this.fields = fields;
    }

    public String getLocale() {
        return locale;
    }

    public void setLocale(String locale) {
        this.locale = locale;
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
        return "DynamicInfoExportFilter{" +
                "deviceId=" + deviceId +
                ", deviceNumber='" + deviceNumber + '\'' +
                ", dateFrom=" + dateFrom +
                ", dateTo=" + dateTo +
                ", fixedInterval=" + fixedInterval +
                ", useFixedInterval=" + useFixedInterval +
                ", locale=" + locale +
                ", fields=" + Arrays.toString(fields) +
                '}';
    }
}
