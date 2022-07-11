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

package com.hmdm.rest.json.view.devicelist;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.hmdm.rest.json.DeviceConfigurationFile;
import com.hmdm.rest.json.DeviceInfo;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * <p>A wrapper around the {@link DeviceInfo} object providing the view suitable for the <code>Device List</code> view
 * of server application.</p>
 *
 * @author isv
 */
@JsonIgnoreProperties(value = {"deviceInfo", "deviceApplications"}, ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
@ApiModel(description = "The details related to a single device. Such details are sent from the MDM mobile application " +
        "to MDM server")
public class DeviceInfoView implements Serializable {

    /**
     * <p>A wrppaed device info object.</p>
     */
    private final DeviceInfo deviceInfo;

    /**
     * <p>A list of wrappers around the applications installed on device.</p>
     */
    private final List<DeviceApplicationView> deviceApplications;

    private final List<DeviceConfigurationFile> files;

    /**
     * <p>Constructs new <code>DeviceInfoView</code> instance. This implementation does nothing.</p>
     */
    DeviceInfoView(DeviceInfo deviceInfo) {
        this.deviceInfo = deviceInfo;
        this.deviceApplications = Optional.ofNullable(deviceInfo.getApplications())
                .map(apps -> apps.stream().map(DeviceApplicationView::new).collect(Collectors.toList()))
                .orElse(new ArrayList<>());
        this.files = Optional.ofNullable(deviceInfo.getFiles()).orElse(new ArrayList<>());
    }

    @ApiModelProperty("A name of the device model")
    public String getModel() {
        return deviceInfo.getModel();
    }

    @ApiModelProperty(value = "A list of permissions set for device. Contains exactly three elements " +
            "(each either 0 or 1).")
    public List<Integer> getPermissions() {
        return deviceInfo.getPermissions();
    }

    @ApiModelProperty("A list of applications installed on device")
    public List<DeviceApplicationView> getApplications() {
        return this.deviceApplications;
    }

    @ApiModelProperty("A textual identifier of device within MDM server (e.g. device number)")
    public String getDeviceId() {
        return deviceInfo.getDeviceId();
    }

    @ApiModelProperty("An IMEI identifier for device")
    public String getImei() {
        return deviceInfo.getImei();
    }

    @ApiModelProperty("A phone number for device")
    public String getPhone() {
        return deviceInfo.getPhone();
    }

    @ApiModelProperty(value = "A battery level in percents", allowableValues = "range[0, 100]")
    public Integer getBatteryLevel() {
        return deviceInfo.getBatteryLevel();
    }

    @ApiModelProperty("A flag indicating if MDM mode is activated on the device")
    public Boolean getMdmMode() {
        return deviceInfo.getMdmMode();
    }

    @ApiModelProperty("A flag indicating if kiosk mode is activated on the device")
    public Boolean getKioskMode() {
        return deviceInfo.getKioskMode();
    }

    @ApiModelProperty("Version of Android OS on the device")
    public String getAndroidVersion() {
        return deviceInfo.getAndroidVersion();
    }

    @ApiModelProperty("Serial number of the device")
    public String getSerial() {
        return deviceInfo.getSerial();
    }

    @ApiModelProperty("A flag showing if Headwind MDM is set as default launcher on a device")
    public Boolean getDefaultLauncher() {
        return deviceInfo.getDefaultLauncher();
    }

    @ApiModelProperty("A list of configuration files installed on device")
    public List<DeviceConfigurationFile> getFiles() {
        return files;
    }
}
