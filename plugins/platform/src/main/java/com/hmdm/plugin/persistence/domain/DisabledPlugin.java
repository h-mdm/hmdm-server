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

package com.hmdm.plugin.persistence.domain;

import java.io.Serializable;

/**
 * <p>$</p>
 *
 * @author isv
 */
public class DisabledPlugin implements Serializable {

    private static final long serialVersionUID = -2650166539403079224L;
    private int customerId;

    private int pluginId;

    /**
     * <p>Constructs new <code>DisabledPlugin</code> instance. This implementation does nothing.</p>
     */
    public DisabledPlugin() {
    }

    public int getCustomerId() {
        return customerId;
    }

    public void setCustomerId(int customerId) {
        this.customerId = customerId;
    }

    public int getPluginId() {
        return pluginId;
    }

    public void setPluginId(int pluginId) {
        this.pluginId = pluginId;
    }

    @Override
    public String toString() {
        return "DisabledPlugin{" +
                "customerId=" + customerId +
                ", pluginId=" + pluginId +
                '}';
    }
}
