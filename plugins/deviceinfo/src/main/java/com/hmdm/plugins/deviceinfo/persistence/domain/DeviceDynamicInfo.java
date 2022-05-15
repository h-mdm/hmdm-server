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
import com.hmdm.persistence.domain.CustomerData;

import java.io.Serializable;

/**
 * <p>A domain object representing the parameters collected by device at specific moment and submitted to server.</p>
 *
 * @author isv
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class DeviceDynamicInfo implements Serializable, CustomerData {

    private static final long serialVersionUID = -3451927801723169574L;
    /**
     * <p>An ID of this record.</p>
     */
    private Integer id;

    /**
     * <p>An ID of a device which this info belongs to.</p>
     */
    private int deviceId;

    /**
     * <p>An ID of a customer which this info belongs to.</p>
     */
    private int customerId;

    /**
     * <p>A timestamp of data gathering by device (in milliseconds since epoch time).</p>
     */
    private long ts;

    /**
     * <p>A data from <code>device</code> group of parameters.</p>
     */
    private DeviceData device;

    /**
     * <p>A data from <code>wifi</code> group of parameters.</p>
     */
    private WifiData wifi;

    /**
     * <p>A data from <code>gps</code> group of parameters.</p>
     */
    private GpsData gps;

    /**
     * <p>A data from <code>mobile</code> group of parameters.</p>
     */
    private MobileData mobile;

    /**
     * <p>A data from <code>mobile2</code> group of parameters.</p>
     */
    private MobileData mobile2;

    /**
     * <p>Constructs new <code>DeviceDynamicInfo</code> instance. This implementation does nothing.</p>
     */
    public DeviceDynamicInfo() {
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public int getDeviceId() {
        return deviceId;
    }

    public void setDeviceId(int deviceId) {
        this.deviceId = deviceId;
    }

    public long getTs() {
        return ts;
    }

    public void setTs(long ts) {
        this.ts = ts;
    }

    public DeviceData getDevice() {
        return device;
    }

    public void setDevice(DeviceData device) {
        this.device = device;
    }

    public WifiData getWifi() {
        return wifi;
    }

    public void setWifi(WifiData wifi) {
        this.wifi = wifi;
    }

    public GpsData getGps() {
        return gps;
    }

    public void setGps(GpsData gps) {
        this.gps = gps;
    }

    public MobileData getMobile() {
        return mobile;
    }

    public void setMobile(MobileData mobile) {
        this.mobile = mobile;
    }

    public MobileData getMobile2() {
        return mobile2;
    }

    public void setMobile2(MobileData mobile2) {
        this.mobile2 = mobile2;
    }

    @Override
    public int getCustomerId() {
        return customerId;
    }

    @Override
    public void setCustomerId(int customerId) {
        this.customerId = customerId;
    }

    @Override
    public String toString() {
        return "DeviceDynamicInfo{" +
                "id=" + id +
                ", deviceId=" + deviceId +
                ", customerId=" + customerId +
                ", ts=" + ts +
                ", device=" + device +
                ", wifi=" + wifi +
                ", gps=" + gps +
                ", mobile=" + mobile +
                ", mobile2=" + mobile2 +
                '}';
    }
}
