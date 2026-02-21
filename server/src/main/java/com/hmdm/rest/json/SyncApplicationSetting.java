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

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;
import java.io.Serializable;

@Schema(
        description = "A single setting for an application installed and used on mobile device and used in data "
                + "sycnhronization between mobile device and server application")
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class SyncApplicationSetting implements Serializable, SyncApplicationSettingInt {

    private static final long serialVersionUID = -3986494672661532347L;

    @Schema(description = "A package of the application", requiredMode = Schema.RequiredMode.REQUIRED)
    private String packageId;

    @Schema(description = "A name of the setting", requiredMode = Schema.RequiredMode.REQUIRED)
    private String name;

    @Schema(
            description = "A type of the application setting. 1 - String, 2 - Integer, 3 - Boolean",
            requiredMode = Schema.RequiredMode.REQUIRED,
            allowableValues = "1,2,3")
    private int type;

    @Schema(description = "A value of the setting")
    private String value;

    @Schema(
            description = "A flag indicating if setting can not be modified on device",
            requiredMode = Schema.RequiredMode.REQUIRED)
    private Boolean readonly;

    @Schema(description = "A timestamp of the last update of the setting (in milliseconds since epoch time")
    private long lastUpdate;

    /**
     * <p>Constructs new <code>SyncApplicationSetting</code> instance. This implementation does nothing.</p>
     */
    public SyncApplicationSetting() {}

    @Override
    public String getPackageId() {
        return packageId;
    }

    public void setPackageId(String packageId) {
        this.packageId = packageId;
    }

    @Override
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Override
    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    @Override
    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    @Override
    public Boolean isReadonly() {
        return readonly != null && readonly ? true : null;
    }

    public void setReadonly(boolean readonly) {
        this.readonly = readonly;
    }

    @Override
    public long getLastUpdate() {
        return lastUpdate;
    }

    public void setLastUpdate(long lastUpdate) {
        this.lastUpdate = lastUpdate;
    }

    @Override
    public String toString() {
        return "SyncApplicationSetting{" + "packageId='" + packageId + '\'' + ", name='" + name + '\'' + ", type="
                + type + ", value='" + value + '\'' + ", readonly=" + readonly + ", lastUpdate=" + lastUpdate + '}';
    }
}
