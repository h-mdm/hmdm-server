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

import java.io.Serializable;

/**
 * <p>A status of the delivery of a single push-message.</p>
 *
 * @author isv
 */
public class PendingPush implements Serializable {

    private static final long serialVersionUID = 4246565898054398224L;
    
    /**
     * <p>An unique ID of this record.</p>
     */
    private long id;

    /**
     * <p>An ID of a message this record relates to.</p>
     */
    private int messageId;

    /**
     * <p>A current status of the delivery of the message. 0 - not delivered yet, 1 - delivered.</p>
     */
    private int status;

    /**
     * <p>A timestamp of creation of the message (in milliseconds since Epoch time).</p>
     */
    private long createTime;

    /**
     * <p>A timestamp of delivering of the message (in milliseconds since Epoch time).</p>
     */
    private Long sendTime;

    /**
     * <p>Constructs new <code>PendingPush</code> instance. This implementation does nothing.</p>
     */
    public PendingPush() {
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public int getMessageId() {
        return messageId;
    }

    public void setMessageId(int messageId) {
        this.messageId = messageId;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public long getCreateTime() {
        return createTime;
    }

    public void setCreateTime(long createTime) {
        this.createTime = createTime;
    }

    public Long getSendTime() {
        return sendTime;
    }

    public void setSendTime(Long sendTime) {
        this.sendTime = sendTime;
    }

    @Override
    public String toString() {
        return "PendingPush{" +
                "id=" + id +
                ", messageId=" + messageId +
                ", status=" + status +
                ", createTime=" + createTime +
                ", sendTime=" + sendTime +
                '}';
    }
}
