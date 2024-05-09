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

package com.hmdm.plugins.push.persistence.domain;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.hmdm.persistence.domain.CustomerData;
import io.swagger.annotations.ApiModelProperty;

import java.io.Serializable;

/**
 * <p>A domain object representing the Push message sent to the device.</p>
 *
 * @author isv
 */
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class PluginPushSchedule implements CustomerData, Serializable {

    private static final long serialVersionUID = 4721182825649279472L;

    @ApiModelProperty("ID of scheduled Push record")
    private Integer id;

    @ApiModelProperty("Customer ID")
    private int customerId;

    @ApiModelProperty("Device ID (if scope is device)")
    private int deviceId;

    @ApiModelProperty("Group ID (if scope is group)")
    private int groupId;

    @ApiModelProperty("Configuration ID (if scope is configuration)")
    private int configurationId;

    @ApiModelProperty("Device number (if scope is device)")
    private String deviceNumber;

    @ApiModelProperty("Group name (if scope is group)")
    private String groupName;

    @ApiModelProperty("Configuration name (if scope is configuration)")
    private String configurationName;

    @ApiModelProperty("Target name (device number or group name or config name)")
    private String target;

    @ApiModelProperty("Message scope (device, group, configuration, all)")
    private String scope;

    @ApiModelProperty("Push Message type")
    private String messageType;

    @ApiModelProperty("Push Message payload")
    private String payload;

    @ApiModelProperty("Comment to the scheduled task")
    private String comment;

    @ApiModelProperty("Scheduled minutes (readable representation in Crontab format)")
    private String min;

    @ApiModelProperty("Scheduled minutes (bit string representation '0101...')")
    private String minBit;

    @ApiModelProperty("Scheduled hours (readable representation in Crontab format)")
    private String hour;

    @ApiModelProperty("Scheduled hours (bit string representation '0101...')")
    private String hourBit;

    @ApiModelProperty("Scheduled days of month (readable representation in Crontab format)")
    private String day;

    @ApiModelProperty("Scheduled days of month (bit string representation '0101...')")
    private String dayBit;

    @ApiModelProperty("Scheduled days of week (readable representation in Crontab format)")
    private String weekday;

    @ApiModelProperty("Scheduled days of week (bit string representation '0101...')")
    private String weekdayBit;

    @ApiModelProperty("Scheduled months (readable representation in Crontab format)")
    private String month;

    @ApiModelProperty("Scheduled months (bit string representation '0101...')")
    private String monthBit;

    /**
     * <p>Constructs new <code>PluginPushMessage</code> instance. This implementation does nothing.</p>
     */
    public PluginPushSchedule() {
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    @Override
    public int getCustomerId() {
        return customerId;
    }

    @Override
    public void setCustomerId(int customerId) {
        this.customerId = customerId;
    }

    public int getDeviceId() {
        return deviceId;
    }

    public void setDeviceId(int deviceId) {
        this.deviceId = deviceId;
    }

    public int getGroupId() {
        return groupId;
    }

    public void setGroupId(int groupId) {
        this.groupId = groupId;
    }

    public int getConfigurationId() {
        return configurationId;
    }

    public void setConfigurationId(int configurationId) {
        this.configurationId = configurationId;
    }

    public String getDeviceNumber() {
        return deviceNumber;
    }

    public void setDeviceNumber(String deviceNumber) {
        this.deviceNumber = deviceNumber;
    }

    public String getGroupName() {
        return groupName;
    }

    public void setGroupName(String groupName) {
        this.groupName = groupName;
    }

    public String getConfigurationName() {
        return configurationName;
    }

    public void setConfigurationName(String configurationName) {
        this.configurationName = configurationName;
    }

    public String getTarget() {
        return target;
    }

    public void setTarget(String target) {
        this.target = target;
    }

    public String getMessageType() {
        return messageType;
    }

    public void setMessageType(String messageType) {
        this.messageType = messageType;
    }

    public String getPayload() {
        return payload;
    }

    public void setPayload(String payload) {
        this.payload = payload;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    public String getScope() {
        return scope;
    }

    public void setScope(String scope) {
        this.scope = scope;
    }

    public String getMin() {
        return min;
    }

    public void setMin(String min) {
        this.min = min;
    }

    public String getMinBit() {
        return minBit;
    }

    public void setMinBit(String minBit) {
        this.minBit = minBit;
    }

    public String getHour() {
        return hour;
    }

    public void setHour(String hour) {
        this.hour = hour;
    }

    public String getHourBit() {
        return hourBit;
    }

    public void setHourBit(String hourBit) {
        this.hourBit = hourBit;
    }

    public String getDay() {
        return day;
    }

    public void setDay(String day) {
        this.day = day;
    }

    public String getDayBit() {
        return dayBit;
    }

    public void setDayBit(String dayBit) {
        this.dayBit = dayBit;
    }

    public String getWeekday() {
        return weekday;
    }

    public void setWeekday(String weekday) {
        this.weekday = weekday;
    }

    public String getWeekdayBit() {
        return weekdayBit;
    }

    public void setWeekdayBit(String weekdayBit) {
        this.weekdayBit = weekdayBit;
    }

    public String getMonth() {
        return month;
    }

    public void setMonth(String month) {
        this.month = month;
    }

    public String getMonthBit() {
        return monthBit;
    }

    public void setMonthBit(String monthBit) {
        this.monthBit = monthBit;
    }

    @Override
    public String toString() {
        return "PluginPushMessage{" +
                "id=" + id +
                ", deviceId=" + deviceId +
                ", groupId=" + groupId +
                ", configurationId=" + configurationId +
                ", deviceNumber=" + deviceNumber +
                ", groupName=" + groupName +
                ", configurationName=" + configurationName +
                ", target=" + target +
                ", scope=" + scope +
                ", messageType=" + messageType +
                ", payload=" + payload +
                ", comment=" + comment +
                ", min=" + min +
                ", hour=" + hour +
                ", day=" + day +
                ", weekday=" + weekday +
                ", month=" + month +
                '}';
    }
}
