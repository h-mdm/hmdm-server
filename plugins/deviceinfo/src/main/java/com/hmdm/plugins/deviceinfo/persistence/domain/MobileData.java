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
 * <p> A domain object representing the device parameters related to Mobile Data.</p>
 *
 * @author isv
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class MobileData implements Serializable {

    private static final long serialVersionUID = -5223140937411419594L;
    
    @ApiModelProperty("An ID of Mobile data record")
    private Integer id;

    @ApiModelProperty("A signal level")
    private Integer rssi;

    @ApiModelProperty("A carrier name")
    private String carrier;

    @ApiModelProperty("A flag indicating if data transmission is on")
    private Boolean data;

    @ApiModelProperty("A used IP-address")
    private String ip;

    @ApiModelProperty("A connection status")
    private String state;

    @ApiModelProperty("A SIM-card status")
    private String simState;

    @ApiModelProperty("A number of transmitted bytes since previous data exhange")
    private Long tx;

    @ApiModelProperty("A number of received bytes since previous data exhange")
    private Long rx;

    /**
     * <p>Constructs new <code>MobileData</code> instance. This implementation does nothing.</p>
     */
    public MobileData() {
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Integer getRssi() {
        return rssi;
    }

    public void setRssi(Integer rssi) {
        this.rssi = rssi;
    }

    public String getCarrier() {
        return carrier;
    }

    public void setCarrier(String carrier) {
        this.carrier = carrier;
    }

    public Boolean getData() {
        return data;
    }

    public void setData(Boolean data) {
        this.data = data;
    }

    public String getIp() {
        return ip;
    }

    public void setIp(String ip) {
        this.ip = ip;
    }

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }

    public String getSimState() {
        return simState;
    }

    public void setSimState(String simState) {
        this.simState = simState;
    }

    public Long getTx() {
        return tx;
    }

    public void setTx(Long tx) {
        this.tx = tx;
    }

    public Long getRx() {
        return rx;
    }

    public void setRx(Long rx) {
        this.rx = rx;
    }

    @Override
    public String toString() {
        return "MobileData{" +
                "id=" + id +
                ", rssi=" + rssi +
                ", carrier='" + carrier + '\'' +
                ", data=" + data +
                ", ip='" + ip + '\'' +
                ", state='" + state + '\'' +
                ", simState='" + simState + '\'' +
                ", tx=" + tx +
                ", rx=" + rx +
                '}';
    }
}
