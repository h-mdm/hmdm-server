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

package com.hmdm.persistence;

import com.hmdm.event.DeviceInfoUpdatedEvent;
import com.hmdm.event.EventListener;
import com.hmdm.event.EventType;
import com.hmdm.service.DeviceStatusService;

/**
 * <p>A listener for {@link EventType#DEVICE_INFO_UPDATED} events.</p>
 */
public class DeviceInfoUpdatedEventListener implements EventListener<DeviceInfoUpdatedEvent> {

    private final DeviceStatusService deviceStatusService;

    /**
     * <p>Constructs new <code>DeviceInfoUpdatedEventListener</code> instance. This implementation does nothing.</p>
     */
    public DeviceInfoUpdatedEventListener(DeviceStatusService deviceStatusService) {
        this.deviceStatusService = deviceStatusService;
    }

    /**
     * <p>Handles the event.</p>
     *
     * @param event an event fired from the external source.
     */
    @Override
    public void onEvent(DeviceInfoUpdatedEvent event) {
        final int deviceId = event.getDeviceId();
        this.deviceStatusService.recalcDeviceStatuses(deviceId);
    }

    /**
     * <p>Gets the type of supported events.</p>
     *
     * @return a type of supported events.
     */
    @Override
    public EventType getSupportedEventType() {
        return EventType.DEVICE_INFO_UPDATED;
    }
}
