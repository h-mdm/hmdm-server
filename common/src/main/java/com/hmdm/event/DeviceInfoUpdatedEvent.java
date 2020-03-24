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

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.ToString;

import java.io.Serializable;

/**
 * <p>An event fired when info for device was updated.</p>
 */
@Data
@AllArgsConstructor
@ToString
public class DeviceInfoUpdatedEvent implements Event, Serializable {

    private static final long serialVersionUID = 482433207404662000L;
    
    /**
     * <p>An unique identifier of the device.</p>
     */
    private final int deviceId;

    /**
     * <p>Gets the type of the event.</p>
     *
     * @return a type of the event.
     */
    @Override
    public EventType getType() {
        return EventType.DEVICE_INFO_UPDATED;
    }
}
