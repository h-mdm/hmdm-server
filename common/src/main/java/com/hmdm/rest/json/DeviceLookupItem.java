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
 * <p>A DTO to carry the data for a single lookup item for device.</p>
 *
 * @author isv
 */
public class DeviceLookupItem extends LookupItem {

    // A device IMEI.
    private String imei;

    // A device synchronization info
    private String info;

    /**
     * <p>Constructs new <code>DeviceLookupItem</code> instance. This implementation does nothing.</p>
     */
    public DeviceLookupItem() {
    }

    public String getImei() {
        return imei;
    }

    public void setImei(String imei) {
        this.imei = imei;
    }

    public String getInfo() {
        return info;
    }

    public void setInfo(String info) {
        this.info = info;
    }

    @Override
    public String toString() {
        return "DeviceLookupItem{" +
                "id=" + getId() +
                ", name='" + getName() + '\'' +
                ", imei='" + imei + '\'' +
                '}';
    }
}
