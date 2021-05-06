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
 * <p>An interface for the configuration file sent to mobile client in response to request for configuration
 * synchronization.</p>
 */
public interface SyncConfigurationFileInt {

    /**
     * <p>A description of the file.</p>
     */
    String getDescription();

    /**
     * <p>A path to a file on device (including the name of the file).</p>
     */
    String getPath();

    /**
     * <p>A checksum for the file content.</p>
     */
    String getChecksum();

    /**
     * <p>A flag indicating if file is to be removed from the device or not.</p>
     */
    Boolean getRemove();

    /**
     * <p>A timestamp of file uploading to server (in milliseconds since epoch time).</p>
     */
    Long getLastUpdate();

    /**
     * <p>An URL referencing the content of the file.</p>
     */
    String getUrl();

    /**
     * A flag indicating whether the file content must be updated by device-specific values
     */
    Boolean getVarContent();
}
