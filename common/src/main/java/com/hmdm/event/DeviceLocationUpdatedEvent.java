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

package com.hmdm.event;

import java.io.Serializable;

/**
 * <p>$</p>
 *
 * @author isv
 */
public class DeviceLocationUpdatedEvent implements Event, Serializable {

    /**
     * <p>An unique identifier of the device.</p>
     */
    private final int deviceId;

    private final long ts;

    private final double lat;

    private final double lon;

    /**
     * <p>Constructs new <code>DeviceLocationUpdatedEvent</code> instance. This implementation does nothing.</p>
     */
    public DeviceLocationUpdatedEvent(int deviceId, long ts, double lat, double lon) {
        this.deviceId = deviceId;
        this.ts = ts;
        this.lat = lat;
        this.lon = lon;
    }

    public int getDeviceId() {
        return deviceId;
    }

    public long getTs() {
        return ts;
    }

    public double getLat() {
        return lat;
    }

    public double getLon() {
        return lon;
    }

    @Override
    public EventType getType() {
        return EventType.DEVICE_LOCATION_UPDATED;
    }
}
