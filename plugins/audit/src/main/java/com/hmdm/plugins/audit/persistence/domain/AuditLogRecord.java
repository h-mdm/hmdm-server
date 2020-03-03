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

package com.hmdm.plugins.audit.persistence.domain;

import com.hmdm.persistence.domain.CustomerData;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.io.Serializable;

/**
 * <p>A domain object representing a single audit log record.</p>
 *
 * @author isv
 */
@JsonIgnoreProperties(ignoreUnknown = true)
@ApiModel(description = "A single audit log record")
public class AuditLogRecord implements CustomerData, Serializable {

    private static final long serialVersionUID = 6693474050584082712L;

    @ApiModelProperty("An ID of the record")
    private Integer id;

    // An ID of a customer account which these settings correspond to
    @ApiModelProperty(hidden = true)
    private Integer customerId;

    @ApiModelProperty("An ID of the user mapped to request.")
    private Integer userId;

    @ApiModelProperty("A timestamp of recording the audit data (in milliseconds since epoch time).")
    private long createTime;

    @ApiModelProperty("A username of the user mapped to request.")
    private String login;

    @ApiModelProperty("A key referencing the description of performed action in localization resource bundle")
    private String action;

    @ApiModelProperty(hidden = true)
    private String payload;

    @ApiModelProperty("An IP-address of the request sender")
    private String ipAddress;

    @ApiModelProperty("Error flag, 0 - no error")
    private Integer errorCode;

    /**
     * <p>Constructs new <code>AuditLogRecord</code> instance. This implementation does nothing.</p>
     */
    public AuditLogRecord() {
    }

    @Override
    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    @Override
    public int getCustomerId() {
        return customerId == null ? 0 : customerId;
    }

    @Override
    public void setCustomerId(int customerId) {
        this.customerId = customerId;
    }

    public void setCustomerId(Integer customerId) {
        this.customerId = customerId;
    }

    public Integer getUserId() {
        return userId;
    }

    public void setUserId(Integer userId) {
        this.userId = userId;
    }

    public long getCreateTime() {
        return createTime;
    }

    public void setCreateTime(long createTime) {
        this.createTime = createTime;
    }

    public String getLogin() {
        return login;
    }

    public void setLogin(String login) {
        this.login = login;
    }

    public String getAction() {
        return action;
    }

    public void setAction(String action) {
        this.action = action;
    }

    public String getPayload() {
        return payload;
    }

    public void setPayload(String payload) {
        this.payload = payload;
    }

    public String getIpAddress() {
        return ipAddress;
    }

    public void setIpAddress(String ipAddress) {
        this.ipAddress = ipAddress;
    }

    public Integer getErrorCode() {
        return errorCode;
    }

    public void setErrorCode(Integer errorCode) {
        this.errorCode = errorCode;
    }

    @Override
    public String toString() {
        return "AuditLogRecord{" +
                "id=" + id +
                ", customerId=" + customerId +
                ", userId=" + userId +
                ", createTime=" + createTime +
                ", login='" + login + '\'' +
                ", action='" + action + '\'' +
                ", payload='" + payload + '\'' +
                ", ipAddress='" + ipAddress + '\'' +
                ", errorCode='" + errorCode + '\'' +
                '}';
    }

    public String toLogString() {
        return "" +
                "createTime=" + createTime +
                ", userId=" + userId +
                ", login='" + login + '\'' +
                ", ipAddress='" + ipAddress + '\'' +
                ", action='" + action + '\'' +
                ", payload='" + payload + '\'' +
                ", errorCode='" + errorCode + '\''
        ;
    }
}
