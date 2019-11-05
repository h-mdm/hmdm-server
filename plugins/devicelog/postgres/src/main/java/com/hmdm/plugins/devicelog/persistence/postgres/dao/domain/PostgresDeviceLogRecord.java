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

package com.hmdm.plugins.devicelog.persistence.postgres.dao.domain;

import com.hmdm.persistence.domain.CustomerData;
import com.hmdm.plugins.devicelog.model.DeviceLogRecord;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/**
 * <p>A device log record stored in <code>Postgres</code> database.</p>
 *
 * @author isv
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class PostgresDeviceLogRecord extends DeviceLogRecord implements CustomerData {

    private static final long serialVersionUID = -6331048753310532028L;

    /**
     * <p>An ID of a log record.</p>
     */
    private Integer id;

    /**
     * <p>An ID of a customer account which the record belongs to.</p>
     */
    private int customerId;

    /**
     * <p>Constructs new <code>PostgresDeviceLogRecord</code> instance. This implementation does nothing.</p>
     */
    public PostgresDeviceLogRecord() {
    }

    @Override
    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    @Override
    public int getCustomerId() {
        return customerId;
    }

    @Override
    public void setCustomerId(int customerId) {
        this.customerId = customerId;
    }

    /**
     * <p>Gets the unique identifier for this record within underlying persistence layer.</p>
     *
     * @return an identifier for this record.
     */
    @Override
    public String getIdentifier() {
        return getId() == null ? null : getId().toString();
    }
}
