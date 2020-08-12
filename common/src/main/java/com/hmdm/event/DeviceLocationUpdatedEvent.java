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

import com.hmdm.rest.json.DeviceLocation;

import java.io.Serializable;
import java.util.List;

/**
 * <p>$</p>
 *
 * @author isv
 */
public class DeviceLocationUpdatedEvent implements Event, Serializable {

    private final int deviceId;

    private List<DeviceLocation> locations;

    private boolean fromDetailedInfo;

    /**
     * <p>Constructs new <code>DeviceLocationUpdatedEvent</code> instance. This implementation does nothing.</p>
     */
    public DeviceLocationUpdatedEvent(int deviceId, List<DeviceLocation> locations, boolean fromDetailedInfo) {
        this.deviceId = deviceId;
        this.locations = locations;
        this.fromDetailedInfo = fromDetailedInfo;
    }

    public int getDeviceId() {
        return deviceId;
    }

    public List<DeviceLocation> getLocations() {
        return locations;
    }

    public boolean isFromDetailedInfo() {
        return fromDetailedInfo;
    }

    @Override
    public EventType getType() {
        return EventType.DEVICE_LOCATION_UPDATED;
    }
}
