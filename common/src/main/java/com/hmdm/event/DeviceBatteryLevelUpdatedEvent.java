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
 * <p>An event fired when battery level for device was updated.</p>
 *
 * @author isv
 */
public class DeviceBatteryLevelUpdatedEvent implements Event, Serializable {

    private static final long serialVersionUID = -8793985695970335908L;
    
    /**
     * <p>An unique identifier of the device.</p>
     */
    private final int deviceId;

    /**
     * <p>A battery level (in percents) from 0 to 100.</p>
     */
    private final int batteryLevel;

    /**
     * <p>Constructs new <code>DeviceBatteryLevelUpdatedEvent</code> instance. This implementation does nothing.</p>
     */
    public DeviceBatteryLevelUpdatedEvent(int deviceId, int batteryLevel) {
        this.deviceId = deviceId;
        this.batteryLevel = batteryLevel;
    }

    /**
     * <p>Gets the device number.</p>
     *
     * @return an unique device identifier.
     */
    public int getDeviceId() {
        return deviceId;
    }

    /**
     * <p>Gets the value of battery level for device.</p>
     *
     * @return a battery level for device.
     */
    public int getBatteryLevel() {
        return batteryLevel;
    }

    /**
     * <p>Gets the type of the event.</p>
     *
     * @return a type of the event.
     */
    @Override
    public EventType getType() {
        return EventType.DEVICE_BATTERY_LEVEL_UPDATED;
    }

    @Override
    public String toString() {
        return "DeviceBatteryLevelUpdatedEvent{" +
                "deviceId='" + deviceId + '\'' +
                ", batteryLevel=" + batteryLevel +
                '}';
    }
}
