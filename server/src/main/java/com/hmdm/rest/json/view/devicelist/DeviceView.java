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
import com.fasterxml.jackson.databind.ObjectMapper;
import com.hmdm.persistence.domain.Configuration;
import com.hmdm.persistence.domain.Device;
import com.hmdm.rest.json.DeviceInfo;
import com.hmdm.rest.json.LookupItem;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.List;

/**
 * <p>A wrapper around the {@link Device} object providing the view suitable for the <code>Device List</code> view of
 * server application.</p>
 *
 * @author isv
 */
@JsonIgnoreProperties(value = {"device", "deviceInfo"}, ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
@ApiModel(description = "A device registered to MDM server and running the MDM mobile application")
public class DeviceView {

    private static final Logger logger = LoggerFactory.getLogger(DeviceView.class);

    /**
     * <p>A device data.</p>
     */
    private final Device device;

    /**
     * <p>An info submitted by device to server.</p>
     */
    private final DeviceInfoView deviceInfo;

    /**
     * <p>Constructs new <code>DeviceView</code> instance. This implementation does nothing.</p>
     */
    public DeviceView(Device device) {
        this.device = device;

        DeviceInfo info = null;
        try {
            if (device.getInfo() != null) {
                if (!device.getInfo().trim().isEmpty()) {
                    final String deviceInfoString = device.getInfo();
                    ObjectMapper jsonMapper = new ObjectMapper();
                    info = jsonMapper.readValue(deviceInfoString, DeviceInfo.class);
                }
            }
        } catch (IOException e) {
            logger.error("Failed to parse JSON data from info property", e);
        }

        if (info != null) {
            this.deviceInfo = new DeviceInfoView(info);
        } else {
            this.deviceInfo = null;
        }
    }

    @ApiModelProperty("An ID of device")
    public Integer getId() {
        return device.getId();
    }

    @ApiModelProperty("An ID of configuration for device")
    public Integer getConfigurationId() {
        return device.getConfigurationId();
    }

    @ApiModelProperty("An unique textual identifier of device")
    public String getNumber() {
        return device.getNumber();
    }

    @ApiModelProperty("A description of device")
    public String getDescription() {
        return device.getDescription();
    }

    @ApiModelProperty("A date of last synchronization of device state (in milliseconds since epoch time)")
    public Long getLastUpdate() {
        return device.getLastUpdate();
    }

    @ApiModelProperty("An IMEI of device as set by the administrator")
    public String getImei() {
        return device.getImei();
    }

    @ApiModelProperty("A phone number of device as set by the administrator")
    public String getPhone() {
        return device.getPhone();
    }

    @ApiModelProperty("Custom property #1")
    public String getCustom1() {
        return device.getCustom1();
    }

    @ApiModelProperty("Custom property #2")
    public String getCustom2() {
        return device.getCustom2();
    }

    @ApiModelProperty("Custom property #3")
    public String getCustom3() {
        return device.getCustom3();
    }

    @ApiModelProperty("Old device number, used when the number is changed")
    public String getOldNumber() {
        return device.getOldNumber();
    }

    @ApiModelProperty("A list of groups assigned to device")
    public List<LookupItem> getGroups() {
        return device.getGroups();
    }

    @ApiModelProperty("A flag indicating if MDM mode is activated on the device")
    public Boolean getMdmMode() {
        return device.getMdmMode();
    }

    @ApiModelProperty("A flag indicating if kiosk mode is activated on the device")
    public Boolean getKioskMode() {
        return device.getKioskMode();
    }

    @ApiModelProperty("Version of Android OS on the device")
    public String getAndroidVersion() {
        return device.getAndroidVersion();
    }

    @ApiModelProperty("Date and time of the device enrollment")
    public Long getEnrollTime() {
        return device.getEnrollTime();
    }

    @ApiModelProperty("Device serial number")
    public String getSerial() {
        return device.getSerial();
    }

    @ApiModelProperty("A version number for Launcher application installed on device")
    public String getLauncherVersion() {
        return device.getLauncherVersion();
    }

    @ApiModelProperty("A package ID for Launcher application installed on device")
    public String getLauncherPkg() {
        return device.getLauncherPkg();
    }

    @ApiModelProperty(value = "A color coding the current status of the device", allowableValues = "green,red,yellow,brown,grey")
    public String getStatusCode() {
        return device.getStatusCode();
    }

    @ApiModelProperty("An info on device state submitted by device to MDM server")
    public DeviceInfoView getInfo() {
        return deviceInfo;
    }
}
