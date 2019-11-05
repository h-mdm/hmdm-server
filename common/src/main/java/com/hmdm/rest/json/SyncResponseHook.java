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

/**
 * <p>An interface for the hooks to be executed when a response to device configuration synchronization request is going
 * to be sent to device. The hooks may extend the response with additional properties or perform any other desired
 * actions based on provided response.</p>
 *
 * @author isv
 */
public interface SyncResponseHook {

    /**
     * <p>Performs the logic specific to this hook.</p>
     *
     * @param deviceId an ID of a device.
     * @param original an original device configuration synchronization response to be handled by this hook.
     * @return a device configuration synchronization response to be used further in process.
     */
    SyncResponseInt handle(int deviceId, SyncResponseInt original);
}
