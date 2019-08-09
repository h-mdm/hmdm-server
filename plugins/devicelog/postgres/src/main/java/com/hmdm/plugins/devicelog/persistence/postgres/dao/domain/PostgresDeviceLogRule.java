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

import com.hmdm.plugins.devicelog.model.DeviceLogRule;

/**
 * <p>A single device log rule stored in <code>Postgres</code> database.</p>
 *
 * @author isv
 */
public class PostgresDeviceLogRule extends DeviceLogRule {

    private static final long serialVersionUID = 1792395795601813159L;

    /**
     * <p>An ID of a rule record.</p>
     */
    private Integer id;

    /**
     * <p>An ID of a setting record.</p>
     */
    private Integer settingId;

    /**
     * <p>Constructs new <code>PostgresDeviceLogRule</code> instance. This implementation does nothing.</p>
     */
    public PostgresDeviceLogRule() {
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
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

    public Integer getSettingId() {
        return settingId;
    }

    public void setSettingId(Integer settingId) {
        this.settingId = settingId;
    }

    @Override
    public String toString() {
        return "PostgresDeviceLogRule{" +
                "id=" + id +
                ", settingId=" + settingId +
                ", data=" + super.toString() +
                '}';
    }
}
