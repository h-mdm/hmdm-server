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
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonSetter;
import io.swagger.annotations.ApiModelProperty;

/**
 * <p>A DTO carrying the details for a single record of dynamic data for device.</p>
 *
 * @author isv
 */
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class DeviceDynamicInfoRecord {

    @ApiModelProperty("A timestamp of most recent update of device info (in milliseconds since epoch time)")
    private Long latestUpdateTime;

    @ApiModelProperty("An interval passed from the most recent update of device info from current time")
    private Long latestUpdateInterval;

    @ApiModelProperty(value = "A type of interval passed from the most recent update of device info from current time",
            allowableValues = "min,hour,day")
    private String latestUpdateIntervalType;

    @ApiModelProperty(value = "A battery level in percents", allowableValues = "range[0, 100]")
    private Integer deviceBatteryLevel;

    @ApiModelProperty(value = "A battery charge type", allowableValues = "usb,ac", allowEmptyValue = true)
    private String deviceBatteryCharging;

    @ApiModelProperty(value = "A used IP-address")
    private String deviceIpAddress;

    @ApiModelProperty(value = "A flag indicating if keyguard is on")
    private Boolean deviceKeyguard;

    @ApiModelProperty(value = "A ring volume level")
    private Integer deviceRingVolume;

    @ApiModelProperty(value = "A flag indicating if Wi-FI is on")
    private Boolean deviceWifiEnabled;

    @ApiModelProperty(value = "A flag indicating if Mobile Data is on")
    private Boolean deviceMobileDataEnabled;

    @ApiModelProperty(value = "A flag indicating if GPS is on")
    private Boolean deviceGpsEnabled;

    @ApiModelProperty(value = "A flag indicating if Bluetooth is on")
    private Boolean deviceBluetoothEnabled;

    @ApiModelProperty(value = "A flag indicating if USB storage is on")
    private Boolean deviceUsbEnabled;

    @ApiModelProperty(value = "Total device memory in Mb")
    private Integer deviceMemoryTotal;

    @ApiModelProperty(value = "Available device memory in Mb")
    private Integer deviceMemoryAvailable;

    @ApiModelProperty("A signal level")
    private Integer wifiRssi;

    @ApiModelProperty("A SSID")
    private String wifiSsid;

    @ApiModelProperty("A security status")
    private String wifiSecurity;

    @ApiModelProperty("A connection status")
    private String wifiState;

    @ApiModelProperty("A used IP-address")
    private String wifiIpAddress;

    @ApiModelProperty("A number of transmitted bytes since previous data exhange")
    private Long wifiTx;

    @ApiModelProperty("A number of received bytes since previous data exhange")
    private Long wifiRx;

    @ApiModelProperty("A connection status")
    private String gpsState;

    @ApiModelProperty("A latitude coordinate")
    private Double gpsLat;

    @ApiModelProperty("A longitude coordinate")
    private Double gpsLon;

    @ApiModelProperty("An altitude coordinate")
    private Double gpsAlt;

    @ApiModelProperty("A speed in km/h")
    private Double gpsSpeed;

    @ApiModelProperty("A course direction in degrees")
    private Double gpsCourse;

    @ApiModelProperty("A signal level")
    private Integer mobile1Rssi;

    @ApiModelProperty("A carrier name")
    private String mobile1Carrier;

    @ApiModelProperty("A flag indicating if data transmission is on")
    private Boolean mobile1DataEnabled;

    @ApiModelProperty("A used IP-address")
    private String mobile1IpAddress;

    @ApiModelProperty("A connection status")
    private String mobile1State;

    @ApiModelProperty("A SIM-card status")
    private String mobile1SimState;

    @ApiModelProperty("A number of transmitted bytes since previous data exhange")
    private Long mobile1Tx;

    @ApiModelProperty("A number of received bytes since previous data exhange")
    private Long mobile1Rx;

    @ApiModelProperty("A signal level")
    private Integer mobile2Rssi;

    @ApiModelProperty("A carrier name")
    private String mobile2Carrier;

    @ApiModelProperty("A flag indicating if data transmission is on")
    private Boolean mobile2DataEnabled;

    @ApiModelProperty("A used IP-address")
    private String mobile2IpAddress;

    @ApiModelProperty("A connection status")
    private String mobile2State;

    @ApiModelProperty("A SIM-card status")
    private String mobile2SimState;

    @ApiModelProperty("A number of transmitted bytes since previous data exhange")
    private Long mobile2Tx;

    @ApiModelProperty("A number of received bytes since previous data exhange")
    private Long mobile2Rx;


    /**
     * <p>Constructs new <code>DeviceDynamicInfoRecord</code> instance. This implementation does nothing.</p>
     */
    public DeviceDynamicInfoRecord() {
    }

    @JsonSetter
    public void setLatestUpdateTime(Long latestUpdateTime) {
        this.latestUpdateTime = latestUpdateTime != null && latestUpdateTime == 0L ? null : latestUpdateTime;
        if (latestUpdateTime != null && latestUpdateTime != 0L) {
            long diff = (System.currentTimeMillis() - latestUpdateTime) / 1000 / 60;
            if (diff >= 24 * 60) {
                this.latestUpdateInterval = diff / 24 / 60;
                this.latestUpdateIntervalType = "day";
            } else if (diff >= 60) {
                this.latestUpdateInterval = diff / 60;
                this.latestUpdateIntervalType = "hour";
            } else {
                this.latestUpdateInterval = diff;
                this.latestUpdateIntervalType = "min";
            }
        } else {
            this.latestUpdateInterval = null;
            this.latestUpdateIntervalType = null;
        }
    }

    public Long getLatestUpdateTime() {
        return latestUpdateTime;
    }

    public Long getLatestUpdateInterval() {
        return latestUpdateInterval;
    }

    public String getLatestUpdateIntervalType() {
        return latestUpdateIntervalType;
    }

    public Integer getDeviceBatteryLevel() {
        return deviceBatteryLevel;
    }

    public void setDeviceBatteryLevel(Integer deviceBatteryLevel) {
        this.deviceBatteryLevel = deviceBatteryLevel;
    }

    public String getDeviceBatteryCharging() {
        return deviceBatteryCharging;
    }

    public void setDeviceBatteryCharging(String deviceBatteryCharging) {
        this.deviceBatteryCharging = deviceBatteryCharging;
    }

    public String getDeviceIpAddress() {
        return deviceIpAddress;
    }

    public void setDeviceIpAddress(String deviceIpAddress) {
        this.deviceIpAddress = deviceIpAddress;
    }

    public Boolean getDeviceKeyguard() {
        return deviceKeyguard;
    }

    public void setDeviceKeyguard(Boolean deviceKeyguard) {
        this.deviceKeyguard = deviceKeyguard;
    }

    public Integer getDeviceRingVolume() {
        return deviceRingVolume;
    }

    public void setDeviceRingVolume(Integer deviceRingVolume) {
        this.deviceRingVolume = deviceRingVolume;
    }

    public Boolean getDeviceWifiEnabled() {
        return deviceWifiEnabled;
    }

    public void setDeviceWifiEnabled(Boolean deviceWifiEnabled) {
        this.deviceWifiEnabled = deviceWifiEnabled;
    }

    public Boolean getDeviceMobileDataEnabled() {
        return deviceMobileDataEnabled;
    }

    public void setDeviceMobileDataEnabled(Boolean deviceMobileDataEnabled) {
        this.deviceMobileDataEnabled = deviceMobileDataEnabled;
    }

    public Boolean getDeviceGpsEnabled() {
        return deviceGpsEnabled;
    }

    public void setDeviceGpsEnabled(Boolean deviceGpsEnabled) {
        this.deviceGpsEnabled = deviceGpsEnabled;
    }

    public Boolean getDeviceBluetoothEnabled() {
        return deviceBluetoothEnabled;
    }

    public void setDeviceBluetoothEnabled(Boolean deviceBluetoothEnabled) {
        this.deviceBluetoothEnabled = deviceBluetoothEnabled;
    }

    public Boolean getDeviceUsbEnabled() {
        return deviceUsbEnabled;
    }

    public void setDeviceUsbEnabled(Boolean deviceUsbEnabled) {
        this.deviceUsbEnabled = deviceUsbEnabled;
    }

    public Integer getDeviceMemoryTotal() {
        return deviceMemoryTotal;
    }

    public void setDeviceMemoryTotal(Integer deviceMemoryTotal) {
        this.deviceMemoryTotal = deviceMemoryTotal;
    }

    public Integer getDeviceMemoryAvailable() {
        return deviceMemoryAvailable;
    }

    public void setDeviceMemoryAvailable(Integer deviceMemoryAvailable) {
        this.deviceMemoryAvailable = deviceMemoryAvailable;
    }

    public Integer getWifiRssi() {
        return wifiRssi;
    }

    public void setWifiRssi(Integer wifiRssi) {
        this.wifiRssi = wifiRssi;
    }

    public String getWifiSsid() {
        return wifiSsid;
    }

    public void setWifiSsid(String wifiSsid) {
        this.wifiSsid = wifiSsid;
    }

    public String getWifiSecurity() {
        return wifiSecurity;
    }

    public void setWifiSecurity(String wifiSecurity) {
        this.wifiSecurity = wifiSecurity;
    }

    public String getWifiState() {
        return wifiState;
    }

    public void setWifiState(String wifiState) {
        this.wifiState = wifiState;
    }

    public String getWifiIpAddress() {
        return wifiIpAddress;
    }

    public void setWifiIpAddress(String wifiIpAddress) {
        this.wifiIpAddress = wifiIpAddress;
    }

    public Long getWifiTx() {
        return wifiTx;
    }

    public void setWifiTx(Long wifiTx) {
        this.wifiTx = wifiTx;
    }

    public Long getWifiRx() {
        return wifiRx;
    }

    public void setWifiRx(Long wifiRx) {
        this.wifiRx = wifiRx;
    }

    public String getGpsState() {
        return gpsState;
    }

    public void setGpsState(String gpsState) {
        this.gpsState = gpsState;
    }

    public Double getGpsLat() {
        return gpsLat;
    }

    public void setGpsLat(Double gpsLat) {
        this.gpsLat = gpsLat;
    }

    public Double getGpsLon() {
        return gpsLon;
    }

    public void setGpsLon(Double gpsLon) {
        this.gpsLon = gpsLon;
    }

    public Double getGpsAlt() {
        return gpsAlt;
    }

    public void setGpsAlt(Double gpsAlt) {
        this.gpsAlt = gpsAlt;
    }

    public Double getGpsSpeed() {
        return gpsSpeed;
    }

    public void setGpsSpeed(Double gpsSpeed) {
        this.gpsSpeed = gpsSpeed;
    }

    public Double getGpsCourse() {
        return gpsCourse;
    }

    public void setGpsCourse(Double gpsCourse) {
        this.gpsCourse = gpsCourse;
    }

    public Integer getMobile1Rssi() {
        return mobile1Rssi;
    }

    public void setMobile1Rssi(Integer mobile1Rssi) {
        this.mobile1Rssi = mobile1Rssi;
    }

    public String getMobile1Carrier() {
        return mobile1Carrier;
    }

    public void setMobile1Carrier(String mobile1Carrier) {
        this.mobile1Carrier = mobile1Carrier;
    }

    public Boolean getMobile1DataEnabled() {
        return mobile1DataEnabled;
    }

    public void setMobile1DataEnabled(Boolean mobile1DataEnabled) {
        this.mobile1DataEnabled = mobile1DataEnabled;
    }

    public String getMobile1IpAddress() {
        return mobile1IpAddress;
    }

    public void setMobile1IpAddress(String mobile1IpAddress) {
        this.mobile1IpAddress = mobile1IpAddress;
    }

    public String getMobile1State() {
        return mobile1State;
    }

    public void setMobile1State(String mobile1State) {
        this.mobile1State = mobile1State;
    }

    public String getMobile1SimState() {
        return mobile1SimState;
    }

    public void setMobile1SimState(String mobile1SimState) {
        this.mobile1SimState = mobile1SimState;
    }

    public Long getMobile1Tx() {
        return mobile1Tx;
    }

    public void setMobile1Tx(Long mobile1Tx) {
        this.mobile1Tx = mobile1Tx;
    }

    public Long getMobile1Rx() {
        return mobile1Rx;
    }

    public void setMobile1Rx(Long mobile1Rx) {
        this.mobile1Rx = mobile1Rx;
    }

    public Integer getMobile2Rssi() {
        return mobile2Rssi;
    }

    public void setMobile2Rssi(Integer mobile2Rssi) {
        this.mobile2Rssi = mobile2Rssi;
    }

    public String getMobile2Carrier() {
        return mobile2Carrier;
    }

    public void setMobile2Carrier(String mobile2Carrier) {
        this.mobile2Carrier = mobile2Carrier;
    }

    public Boolean getMobile2DataEnabled() {
        return mobile2DataEnabled;
    }

    public void setMobile2DataEnabled(Boolean mobile2DataEnabled) {
        this.mobile2DataEnabled = mobile2DataEnabled;
    }

    public String getMobile2IpAddress() {
        return mobile2IpAddress;
    }

    public void setMobile2IpAddress(String mobile2IpAddress) {
        this.mobile2IpAddress = mobile2IpAddress;
    }

    public String getMobile2State() {
        return mobile2State;
    }

    public void setMobile2State(String mobile2State) {
        this.mobile2State = mobile2State;
    }

    public String getMobile2SimState() {
        return mobile2SimState;
    }

    public void setMobile2SimState(String mobile2SimState) {
        this.mobile2SimState = mobile2SimState;
    }

    public Long getMobile2Tx() {
        return mobile2Tx;
    }

    public void setMobile2Tx(Long mobile2Tx) {
        this.mobile2Tx = mobile2Tx;
    }

    public Long getMobile2Rx() {
        return mobile2Rx;
    }

    public void setMobile2Rx(Long mobile2Rx) {
        this.mobile2Rx = mobile2Rx;
    }

    public boolean isDeviceDataIncluded() {
        return deviceBatteryLevel != null
                || deviceBatteryCharging != null
                || deviceIpAddress != null
                || deviceKeyguard != null
                || deviceRingVolume != null
                || deviceWifiEnabled != null
                || deviceMobileDataEnabled != null
                || deviceGpsEnabled != null
                || deviceBluetoothEnabled != null
                || deviceUsbEnabled != null
                || deviceMemoryTotal != null
                || deviceMemoryAvailable != null
                ;
    }

    public boolean isWifiDataIncluded() {
        return wifiRssi != null
                || wifiSsid != null
                || wifiSecurity != null
                || wifiState != null
                || wifiIpAddress != null
                || wifiTx != null
                || wifiRx != null
                ;
    }

    public boolean isGpsDataIncluded() {
        return gpsState != null
                || gpsLat != null
                || gpsLon != null
                || gpsAlt != null
                || gpsSpeed != null
                || gpsCourse != null
                ;
    }

    public boolean isMobile1DataIncluded() {
        return mobile1Rssi != null
                || mobile1Carrier != null
                || mobile1DataEnabled != null
                || mobile1IpAddress != null
                || mobile1State != null
                || mobile1SimState != null
                || mobile1Tx != null
                || mobile1Rx != null
                ;
    }

    public boolean isMobile2DataIncluded() {
        return mobile2Rssi != null
                || mobile2Carrier != null
                || mobile2DataEnabled != null
                || mobile2IpAddress != null
                || mobile2State != null
                || mobile2SimState != null
                || mobile2Tx != null
                || mobile2Rx != null
                ;
    }
}
