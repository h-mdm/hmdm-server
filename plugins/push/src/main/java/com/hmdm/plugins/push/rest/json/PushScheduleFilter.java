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

package com.hmdm.plugins.push.rest.json;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

import java.io.Serializable;
import java.util.Date;

/**
 * <p>A DTO carrying the parameters for filtering the lists of scheduled Push objects.</p>
 *
 * @author seva
 */
@ApiModel(description = "The parameters for filtering the lists of scheduled messages")
@JsonIgnoreProperties(ignoreUnknown = true)
public class PushScheduleFilter implements Serializable {
    private static final long serialVersionUID = 5138659302284486486L;


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
     * <p>A filter used for filtering the data records by message.</p>
     */
    @ApiModelProperty("A filter used for filtering the data records by message")
    private String messageFilter;

    /**
     * <p>A filter used for filtering the data records by device.</p>
     */
    @ApiModelProperty("A filter used for filtering the data records by payload")
    private String payloadFilter;

    /**
     * <p>A name of sorting column.</p>
     */
    @ApiModelProperty("A name of sorting column")
    private String sortValue = "id";

    /**
     * <p>An ID of a customer.</p>
     */
    @ApiModelProperty(hidden = true)
    private int customerId;

    /**
     * <p>Constructs new <code>PushMessageFilter</code> instance. This implementation does nothing.</p>
     */
    public PushScheduleFilter() {
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

    public String getSortValue() {
        return sortValue;
    }

    public void setSortValue(String sortValue) {
        this.sortValue = sortValue;
    }

    public String getMessageFilter() {
        return messageFilter;
    }

    public void setMessageFilter(String messageFilter) {
        this.messageFilter = messageFilter;
    }

    public String getPayloadFilter() {
        return payloadFilter;
    }

    public void setPayloadFilter(String payloadFilter) {
        this.payloadFilter = payloadFilter;
    }

    public int getCustomerId() {
        return customerId;
    }

    public void setCustomerId(int customerId) {
        this.customerId = customerId;
    }

    @Override
    public String toString() {
        return "PushScheduleFilter{" +
                "pageSize=" + pageSize +
                ", pageNum=" + pageNum +
                ", messageFilter='" + messageFilter + '\'' +
                ", payloadFilter='" + payloadFilter + '\'' +
                ", sortValue='" + sortValue + '\'' +
                ", customerId=" + customerId +
                '}';
    }
}
