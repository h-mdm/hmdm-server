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

package com.hmdm.plugins.devicelog.persistence;

import com.hmdm.plugins.devicelog.model.DeviceLogRecord;
import com.hmdm.plugins.devicelog.rest.json.AppliedDeviceLogRule;
import com.hmdm.plugins.devicelog.rest.json.DeviceLogFilter;
import com.hmdm.plugins.devicelog.rest.json.UploadedDeviceLogRecord;

import java.util.List;

/**
 * <p>An interface for DAO to be used for managing the device log records in persistence layer.</p>
 *
 * @author isv
 */
public interface DeviceLogDAO {

    /**
     * <p>Finds the log records matching the specified filter.</p>
     *
     * @param filter a filter used to narrowing down the search results.
     * @return a list of log records matching the specified filter.
     */
    List<DeviceLogRecord> findAll(DeviceLogFilter filter);

    /**
     * <p>Counts the log records matching the specified filter.</p>
     *
     * @param filter a filter used to narrowing down the search results.
     * @return a number of log records matching the specified filter.
     */
    long countAll(DeviceLogFilter filter);

    /**
     * <p>Inserts the specified log records uploaded by the specified device into underlying persistent data store.</p>
     *
     * @param deviceNumber an identifier of a device.
     * @param ipAddress    an IP-address of a device.
     * @param logs a list of log records to be inserted.
     * @return a number of log records inserted into underlying persistent store.
     */
    int insertDeviceLogRecords(String deviceNumber, String ipAddress, List<UploadedDeviceLogRecord> logs);

    /**
     * <p>Gets the list of log rules applicable to specified device.</p>
     *
     * @param deviceNumber an identifier of device.
     * @return a list of applicable log rules for device.
     */
    List<AppliedDeviceLogRule> getDeviceLogRules(String deviceNumber);
}
