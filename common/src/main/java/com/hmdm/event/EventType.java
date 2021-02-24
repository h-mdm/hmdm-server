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

/**
 * <p>An enumeration over the event types supported by the application.</p>
 *
 * @author isv
 */
public enum EventType {

    DEVICE_BATTERY_LEVEL_UPDATED(DeviceBatteryLevelUpdatedEvent.class),
    DEVICE_LOCATION_UPDATED(DeviceLocationUpdatedEvent.class),
    DEVICE_INFO_UPDATED(DeviceInfoUpdatedEvent.class),
    CONFIGURATION_UPDATED(ConfigurationUpdatedEvent.class),
    CUSTOMER_CREATED(CustomerCreatedEvent.class);

    /**
     * <p>A type of the event.</p>
     */
    private final Class<? extends Event> eventClass;

    /**
     * <p>Constructs new <code>EventType</code> instance. This implementation does nothing.</p>
     */
    EventType(Class<? extends Event> eventClass) {
        this.eventClass = eventClass;
    }

    /**
     * <p>Gets the class of the events.</p>
     *
     * @return a class of the events.
     */
    public Class<? extends Event> getEventClass() {
        return eventClass;
    }
}
