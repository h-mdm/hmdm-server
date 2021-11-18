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

/**
 * <p>An enumeration over the supported desktop headers.</p>
 *
 * @author isv
 */
public enum DesktopHeader {
    NO_HEADER("none"),
    DEVICE_ID("deviceId"),
    DESCRIPTION("description"),
    CUSTOM1("custom1"),
    CUSTOM2("custom2"),
    CUSTOM3("custom3"),
    TEMPLATE("template");

    /**
     * <p>A string value transmitted between the server and the device to represent this item.</p>
     */
    private String transmittedValue;

    private DesktopHeader(String transmittedValue) {
        this.transmittedValue = transmittedValue;
    }

    public String getTransmittedValue() {
        return transmittedValue;
    }
}
