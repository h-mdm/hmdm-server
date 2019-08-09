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

package com.hmdm.plugins.devicelog.task;

import com.hmdm.plugins.devicelog.persistence.DeviceLogDAO;
import com.hmdm.plugins.devicelog.rest.json.UploadedDeviceLogRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

/**
 * <p>A standalone task to be used for saving the .</p>
 *
 * @author isv
 */
public class InsertDeviceLogRecordsTask implements Runnable {

    /**
     * <p>A logger to be used for logging the events.</p>
     */
    private static final Logger log = LoggerFactory.getLogger("InsertDeviceLogRecordsTask");

    /**
     * <p>An identifier of a device which have uploaded the log records.</p>
     */
    private final String deviceNumber;

    /**
     * <p>An IPO-address of a device which have uploaded the log records.</p>
     */
    private final String ipAddress;

    /**
     * <p>A list of uploaded log records.</p>
     */
    private final List<UploadedDeviceLogRecord> logs;

    /**
     * <p>An interface to device log records persistence layer.</p>
     */
    private final DeviceLogDAO deviceLogDAO;

    /**
     * <p>Constructs new <code>InsertDeviceLogRecordsTask</code> instance. This implementation does nothing.</p>
     */
    public InsertDeviceLogRecordsTask(String deviceNumber,
                                      String ipAddress,
                                      List<UploadedDeviceLogRecord> logs,
                                      DeviceLogDAO deviceLogDAO) {
        this.deviceNumber = deviceNumber;
        this.ipAddress = ipAddress;
        this.logs = logs;
        this.deviceLogDAO = deviceLogDAO;
    }

    /**
     * <p>Inserts the log records into underlying persistent data store.</p>
     */
    @Override
    public void run() {
        try {
            int insertedCount = this.deviceLogDAO.insertDeviceLogRecords(this.deviceNumber, this.ipAddress, this.logs);
            log.debug("Inserted {} log records for device '{}' into persistent data store", insertedCount, deviceNumber);
        } catch (Exception e) {
            log.error("Unexpected error while inserting log records for device '{}'", this.deviceNumber, e);
        }
    }
}
