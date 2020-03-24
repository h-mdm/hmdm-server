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

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

import java.io.Serializable;

/**
 * <p>The details on latest device location.</p>
 *
 * @author isv
 */
@JsonIgnoreProperties(ignoreUnknown = true)
@ApiModel(description = "The latest device location")
public class DeviceLocation implements Serializable {

    private static final long serialVersionUID = 5866031460781724004L;
    @ApiModelProperty(value = "A latitude coordinate", required = true)
    private Double lat;

    @ApiModelProperty(value = "A longitude coordinate", required = true)
    private Double lon;

    @ApiModelProperty(value = "A timestamp of location recording by device (in milliseconds since epoch time)", required = true)
    private Long ts;

    /**
     * <p>Constructs new <code>DeviceLocation</code> instance. This implementation does nothing.</p>
     */
    public DeviceLocation() {
    }

    public Double getLat() {
        return lat;
    }

    public void setLat(Double lat) {
        this.lat = lat;
    }

    public Double getLon() {
        return lon;
    }

    public void setLon(Double lon) {
        this.lon = lon;
    }

    public Long getTs() {
        return ts;
    }

    public void setTs(Long ts) {
        this.ts = ts;
    }

    @Override
    public String toString() {
        return "DeviceLocation{" +
                "lat=" + lat +
                ", lon=" + lon +
                ", ts=" + ts +
                '}';
    }
}
