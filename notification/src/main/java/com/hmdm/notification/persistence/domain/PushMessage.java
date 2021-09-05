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

package com.hmdm.notification.persistence.domain;

import com.hmdm.persistence.domain.CustomerData;

import java.io.Serializable;

/**
 * <p>A single message to be pushed to a single device.</p>
 *
 * @author isv
 */
public class PushMessage implements Serializable {

    private static final long serialVersionUID = 2664701750054255531L;
    
    private Integer id;

    private String messageType;

    private int deviceId;

    private String payload;

    public static final String TYPE_APP_CONFIG_UPDATED = "appConfigUpdated";
    public static final String TYPE_CONFIG_UPDATED = "configUpdated";
    public static final String TYPE_RUN_APP = "runApp";

    /**
     * <p>Constructs new <code>PushMessage</code> instance. This implementation does nothing.</p>
     */
    public PushMessage() {
    }

    public PushMessage(String messageType, String payload, int deviceId) {
        this.messageType = messageType;
        this.payload = payload;
        this.deviceId = deviceId;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getMessageType() {
        return messageType;
    }

    public void setMessageType(String messageType) {
        this.messageType = messageType;
    }

    public int getDeviceId() {
        return deviceId;
    }

    public void setDeviceId(int deviceId) {
        this.deviceId = deviceId;
    }

    public String getPayload() {
        return payload;
    }

    public void setPayload(String payload) {
        this.payload = payload;
    }

    @Override
    public String toString() {
        return "PushMessage{" +
                "id=" + id +
                ", messageType='" + messageType + '\'' +
                ", deviceId='" + deviceId + '\'' +
                ", payload='" + payload + '\'' +
                '}';
    }
}
