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

package com.hmdm.persistence.domain;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.io.Serializable;
import java.util.Objects;

/**
 * <p>Other instances usage stats.</p>
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class UsageStats implements Serializable {

    private static final long serialVersionUID = 5087620848766943386L;

    private Integer id;
    private long ts;
    private String instanceId;
    private String webVersion;
    private boolean community;
    private int devicesTotal;
    private int devicesOnline;
    private int cpuTotal;
    private int cpuUsed;
    private int ramTotal;
    private int ramUsed;
    private String scheme;
    private String arch;
    private String os;

    public UsageStats() {
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public long getTs() {
        return ts;
    }

    public void setTs(long ts) {
        this.ts = ts;
    }

    public String getInstanceId() {
        return instanceId;
    }

    public void setInstanceId(String instanceId) {
        this.instanceId = instanceId;
    }

    public String getWebVersion() {
        return webVersion;
    }

    public void setWebVersion(String webVersion) {
        this.webVersion = webVersion;
    }

    public boolean isCommunity() {
        return community;
    }

    public void setCommunity(boolean community) {
        this.community = community;
    }

    public int getDevicesTotal() {
        return devicesTotal;
    }

    public void setDevicesTotal(int devicesTotal) {
        this.devicesTotal = devicesTotal;
    }

    public int getDevicesOnline() {
        return devicesOnline;
    }

    public void setDevicesOnline(int devicesOnline) {
        this.devicesOnline = devicesOnline;
    }

    public int getCpuTotal() {
        return cpuTotal;
    }

    public void setCpuTotal(int cpuTotal) {
        this.cpuTotal = cpuTotal;
    }

    public int getCpuUsed() {
        return cpuUsed;
    }

    public void setCpuUsed(int cpuUsed) {
        this.cpuUsed = cpuUsed;
    }

    public int getRamTotal() {
        return ramTotal;
    }

    public void setRamTotal(int ramTotal) {
        this.ramTotal = ramTotal;
    }

    public int getRamUsed() {
        return ramUsed;
    }

    public void setRamUsed(int ramUsed) {
        this.ramUsed = ramUsed;
    }

    public String getScheme() {
        return scheme;
    }

    public void setScheme(String scheme) {
        this.scheme = scheme;
    }

    public String getArch() {
        return arch;
    }

    public void setArch(String arch) {
        this.arch = arch;
    }

    public String getOs() {
        return os;
    }

    public void setOs(String os) {
        this.os = os;
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return "UsageStats{" +
                "id=" + id +
                ", ts=" + ts +
                ", instanceId='" + instanceId + '\'' +
                ", webVersion='" + webVersion + '\'' +
                ", community=" + community +
                ", devicesTotal=" + devicesTotal +
                ", devicesOnline=" + devicesOnline +
                ", cpuTotal=" + cpuTotal +
                ", cpuUsed=" + cpuUsed +
                ", ramTotal=" + ramTotal +
                ", ramUsed=" + ramUsed +
                ", scheme=" + scheme +
                ", arch=" + arch +
                ", os=" + os +
                '}';
    }

    public String toJsonString() {
        return "{" +
                "\"ts\":" + ts + "," +
                "\"instanceId\":\"" + instanceId + "\"," +
                "\"webVersion\":\"" + webVersion + "\"," +
                "\"community\":" + community + "," +
                "\"devicesTotal\":" + devicesTotal + "," +
                "\"devicesOnline\":" + devicesOnline + "," +
                "\"cpuTotal\":" + cpuTotal + "," +
                "\"cpuUsed\":" + cpuUsed + "," +
                "\"ramTotal\":" + ramTotal + "," +
                "\"ramUsed\":" + ramUsed + "," +
                "\"scheme\":\"" + scheme + "\"," +
                "\"arch\":\"" + arch + "\"," +
                "\"os\":\"" + os + "\"" +
                "}";
    }
}
