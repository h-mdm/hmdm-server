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

package com.hmdm.plugins.deviceinfo.persistence.domain;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import io.swagger.annotations.ApiModelProperty;

import java.io.Serializable;

/**
 * <p> A domain object representing the general device parameters.</p>
 *
 * @author isv
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class DeviceData implements Serializable {

    private static final long serialVersionUID = 8593556673514494478L;
    
    @ApiModelProperty("An ID of device data record")
    private Integer id;

    @ApiModelProperty(value = "A battery level in percents", allowableValues = "range[0, 100]")
    private Integer batteryLevel;

    @ApiModelProperty(value = "A battery charge type", allowableValues = "usb,ac", allowEmptyValue = true)
    private String batteryCharging;

    @ApiModelProperty(value = "A used IP-address")
    private String ip;

    @ApiModelProperty(value = "A flag indicating if keyguard is on")
    private Boolean keyguard;

    @ApiModelProperty(value = "A ring volume level")
    private Integer ringVolume;

    @ApiModelProperty(value = "A flag indicating if Wi-FI is on")
    private Boolean wifi;

    @ApiModelProperty(value = "A flag indicating if Mobile Data is on")
    private Boolean mobileData;

    @ApiModelProperty(value = "A flag indicating if GPS is on")
    private Boolean gps;

    @ApiModelProperty(value = "A flag indicating if Bluetooth is on")
    private Boolean bluetooth;

    @ApiModelProperty(value = "A flag indicating if USB storage is on")
    private Boolean usbStorage;

    @ApiModelProperty(value = "Total memory in Mb")
    private Integer memoryTotal;

    @ApiModelProperty(value = "Available memory in Mb")
    private Integer memoryAvailable;

    /**
     * <p>Constructs new <code>DeviceData</code> instance. This implementation does nothing.</p>
     */
    public DeviceData() {
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Integer getBatteryLevel() {
        return batteryLevel;
    }

    public void setBatteryLevel(Integer batteryLevel) {
        this.batteryLevel = batteryLevel;
    }

    public String getBatteryCharging() {
        return batteryCharging;
    }

    public void setBatteryCharging(String batteryCharging) {
        this.batteryCharging = batteryCharging;
    }

    public String getIp() {
        return ip;
    }

    public void setIp(String ip) {
        this.ip = ip;
    }

    public Boolean getKeyguard() {
        return keyguard;
    }

    public void setKeyguard(Boolean keyguard) {
        this.keyguard = keyguard;
    }

    public Integer getRingVolume() {
        return ringVolume;
    }

    public void setRingVolume(Integer ringVolume) {
        this.ringVolume = ringVolume;
    }

    public Boolean getWifi() {
        return wifi;
    }

    public void setWifi(Boolean wifi) {
        this.wifi = wifi;
    }

    public Boolean getMobileData() {
        return mobileData;
    }

    public void setMobileData(Boolean mobileData) {
        this.mobileData = mobileData;
    }

    public Boolean getGps() {
        return gps;
    }

    public void setGps(Boolean gps) {
        this.gps = gps;
    }

    public Boolean getBluetooth() {
        return bluetooth;
    }

    public void setBluetooth(Boolean bluetooth) {
        this.bluetooth = bluetooth;
    }

    public Boolean getUsbStorage() {
        return usbStorage;
    }

    public void setUsbStorage(Boolean usbStorage) {
        this.usbStorage = usbStorage;
    }

    public Integer getMemoryTotal() {
        return memoryTotal;
    }

    public void setMemoryTotal(Integer memoryTotal) {
        this.memoryTotal = memoryTotal;
    }

    public Integer getMemoryAvailable() {
        return memoryAvailable;
    }

    public void setMemoryAvailable(Integer memoryAvailable) {
        this.memoryAvailable = memoryAvailable;
    }

    @Override
    public String toString() {
        return "DeviceData{" +
                "id=" + id +
                ", batteryLevel=" + batteryLevel +
                ", batteryCharging='" + batteryCharging + '\'' +
                ", ip='" + ip + '\'' +
                ", keyguard=" + keyguard +
                ", ringVolume=" + ringVolume +
                ", wifi=" + wifi +
                ", mobileData=" + mobileData +
                ", gps=" + gps +
                ", bluetooth=" + bluetooth +
                ", usbStorage=" + usbStorage +
                ", memoryTotal=" + memoryTotal +
                ", memoryAvailable=" + memoryAvailable +
                '}';
    }
}
