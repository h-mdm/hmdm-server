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

package com.hmdm.notification.rest.json;

import com.hmdm.notification.persistence.domain.PushMessage;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

import java.io.Serializable;

/**
 * <p>A DTO carrying the details for a single push-message to be delivered to device.</p>
 *
 * @author isv
 */
@ApiModel(description = "A notification message")
public class PlainPushMessage implements Serializable {

    private static final long serialVersionUID = 5860815657249909466L;
    @ApiModelProperty("A type of the message")
    private String messageType;
    @ApiModelProperty("A payload for the message")
    private String payload;

    /**
     * <p>Constructs new <code>PlainPushMessage</code> instance. This implementation does nothing.</p>
     */
    public PlainPushMessage(PushMessage original) {
        this.messageType = original.getMessageType();
        this.payload = original.getPayload();
    }

    public String getMessageType() {
        return messageType;
    }

    public void setMessageType(String messageType) {
        this.messageType = messageType;
    }


    public String getPayload() {
        return payload;
    }

    public void setPayload(String payload) {
        this.payload = payload;
    }

    @Override
    public String toString() {
        return "PlainPushMessage{" +
                "messageType='" + messageType + '\'' +
                ", payload='" + payload + '\'' +
                '}';
    }
}
