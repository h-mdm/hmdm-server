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
import com.hmdm.persistence.domain.Device;
import com.hmdm.rest.json.DeviceInfo;
import com.hmdm.rest.json.LookupItem;
import io.swagger.v3.oas.annotations.media.Schema;
import java.io.IOException;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * <p>A wrapper around the {@link Device} object providing the view suitable for the <code>Device List</code> view of
 * server application.</p>
 *
 * @author isv
 */
@JsonIgnoreProperties(
        value = {"device", "deviceInfo"},
        ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
@Schema(description = "A device registered to MDM server and running the MDM mobile application")
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

    @Schema(description = "An ID of device")
    public Integer getId() {
        return device.getId();
    }

    @Schema(description = "An ID of configuration for device")
    public Integer getConfigurationId() {
        return device.getConfigurationId();
    }

    @Schema(description = "An unique textual identifier of device")
    public String getNumber() {
        return device.getNumber();
    }

    @Schema(description = "A description of device")
    public String getDescription() {
        return device.getDescription();
    }

    @Schema(description = "A date of last synchronization of device state (in milliseconds since epoch time)")
    public Long getLastUpdate() {
        return device.getLastUpdate();
    }

    @Schema(description = "An IMEI of device as set by the administrator")
    public String getImei() {
        return device.getImei();
    }

    @Schema(description = "A phone number of device as set by the administrator")
    public String getPhone() {
        return device.getPhone();
    }

    @Schema(description = "A public IP of device")
    public String getPublicIp() {
        return device.getPublicIp();
    }

    @Schema(description = "Custom property #1")
    public String getCustom1() {
        return device.getCustom1();
    }

    @Schema(description = "Custom property #2")
    public String getCustom2() {
        return device.getCustom2();
    }

    @Schema(description = "Custom property #3")
    public String getCustom3() {
        return device.getCustom3();
    }

    @Schema(description = "Old device number, used when the number is changed")
    public String getOldNumber() {
        return device.getOldNumber();
    }

    @Schema(description = "A list of groups assigned to device")
    public List<LookupItem> getGroups() {
        return device.getGroups();
    }

    @Schema(description = "A flag indicating if MDM mode is activated on the device")
    public Boolean getMdmMode() {
        return device.getMdmMode();
    }

    @Schema(description = "A flag indicating if kiosk mode is activated on the device")
    public Boolean getKioskMode() {
        return device.getKioskMode();
    }

    @Schema(description = "Version of Android OS on the device")
    public String getAndroidVersion() {
        return device.getAndroidVersion();
    }

    @Schema(description = "Date and time of the device enrollment")
    public Long getEnrollTime() {
        return device.getEnrollTime();
    }

    @Schema(description = "Device serial number")
    public String getSerial() {
        return device.getSerial();
    }

    @Schema(description = "A version number for Launcher application installed on device")
    public String getLauncherVersion() {
        return device.getLauncherVersion();
    }

    @Schema(description = "A package ID for Launcher application installed on device")
    public String getLauncherPkg() {
        return device.getLauncherPkg();
    }

    @Schema(
            description = "A color coding the current status of the device",
            allowableValues = "green,red,yellow,brown,grey")
    public String getStatusCode() {
        return device.getStatusCode();
    }

    @Schema(description = "An info on device state submitted by device to MDM server")
    public DeviceInfoView getInfo() {
        return deviceInfo;
    }
}
