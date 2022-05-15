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
 * <p> A domain object representing the device parameters related to GPS.</p>
 *
 * @author isv
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class GpsData implements Serializable {

    private static final long serialVersionUID = 3701846866813916864L;
    
    @ApiModelProperty("An ID of GPS data record")
    private Integer id;

    @ApiModelProperty("A connection status")
    private String state;

    @ApiModelProperty("A latitude coordinate")
    private Double lat;

    @ApiModelProperty("A longitude coordinate")
    private Double lon;

    @ApiModelProperty("An altitude coordinate")
    private Double alt;

    @ApiModelProperty("A speed in km/h")
    private Double speed;

    @ApiModelProperty("A course direction in degrees")
    private Double course;

    /**
     * <p>Constructs new <code>GpsData</code> instance. This implementation does nothing.</p>
     */
    public GpsData() {
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
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

    public Double getAlt() {
        return alt;
    }

    public void setAlt(Double alt) {
        this.alt = alt;
    }

    public Double getSpeed() {
        return speed;
    }

    public void setSpeed(Double speed) {
        this.speed = speed;
    }

    public Double getCourse() {
        return course;
    }

    public void setCourse(Double course) {
        this.course = course;
    }

    @Override
    public String toString() {
        return "GpsData{" +
                "id=" + id +
                ", state='" + state + '\'' +
                ", lat=" + lat +
                ", lon=" + lon +
                ", alt=" + alt +
                ", speed=" + speed +
                ", course=" + course +
                '}';
    }
}
