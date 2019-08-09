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

package com.hmdm.rest.json;

import java.io.Serializable;
import java.util.LinkedList;
import java.util.List;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import org.codehaus.jackson.annotate.JsonIgnoreProperties;
import com.hmdm.persistence.domain.Application;

@JsonIgnoreProperties(ignoreUnknown = true)
@ApiModel(description = "The details related to a single device. Such details are sent from the MDM mobile application " +
        "to MDM server")
public class DeviceInfo implements Serializable {

    private static final long serialVersionUID = -2969488681388412820L;

    @ApiModelProperty("A name of the device model")
    private String model;

    @ApiModelProperty("A list of permissions set for device")
    private List<Integer> permissions = new LinkedList<>();

    @ApiModelProperty("A list of applications installed on device")
    private List<Application> applications = new LinkedList<>();

    @ApiModelProperty("An identifier of device within MDM server")
    private String deviceId;

    @ApiModelProperty("An IMEI identifier")
    private String imei;

    @ApiModelProperty("A phone number")
    private String phone;

    public DeviceInfo() {
    }

    public String getModel() {
        return this.model;
    }

    public void setModel(String model) {
        this.model = model;
    }

    public List<Integer> getPermissions() {
        return this.permissions;
    }

    public void setPermissions(List<Integer> permissions) {
        this.permissions = permissions;
    }

    public List<Application> getApplications() {
        return this.applications;
    }

    public void setApplications(List<Application> applications) {
        this.applications = applications;
    }

    public String getDeviceId() {
        return this.deviceId;
    }

    public void setDeviceId(String deviceId) {
        this.deviceId = deviceId;
    }

    public String getImei() {
        return imei;
    }

    public void setImei(String imei) {
        this.imei = imei;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }
}
