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

package com.hmdm.notification.persistence;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.hmdm.notification.persistence.domain.PushMessage;
import com.hmdm.notification.persistence.mapper.NotificationMapper;
import com.hmdm.persistence.ConfigurationDAO;
import com.hmdm.persistence.DeviceDAO;
import com.hmdm.persistence.domain.Configuration;
import com.hmdm.persistence.domain.Device;
import org.mybatis.guice.transactional.Transactional;

import java.util.List;
import java.util.stream.Collectors;

/**
 * <p>An interface to notification messages persistence.</p>
 *
 * @author isv
 */
@Singleton
public class NotificationDAO {

    private final NotificationMapper notificationMapper;
    private final DeviceDAO deviceDAO;
    private final ConfigurationDAO configurationDAO;

    /**
     * <p>Constructs new <code>NotificationDAO</code> instance. This implementation does nothing.</p>
     */
    @Inject
    public NotificationDAO(NotificationMapper notificationMapper, DeviceDAO deviceDAO, ConfigurationDAO configurationDAO) {
        this.notificationMapper = notificationMapper;
        this.deviceDAO = deviceDAO;
        this.configurationDAO = configurationDAO;
    }

    /**
     * <p>Gets the list of messages to be delivered to specified device. The returned messages are immediately marked as
     * delivered.</p>
     *
     * @param deviceId a device number identifying the device.
     * @return a list of messages to be delivered to device.
     */
    @Transactional
    public List<PushMessage> getPendingMessagesForDelivery(String deviceId) {
        final List<PushMessage> messages = this.notificationMapper.getPendingMessagesForDelivery(deviceId);
        if (!messages.isEmpty()) {
            final List<Integer> messageIds = messages.stream().map(PushMessage::getId).collect(Collectors.toList());
            this.notificationMapper.markMessagesAsDelivered(messageIds);
        }
        return messages;
    }

    /**
     * <p>Sends the specified notification message. This implementation puts it to queue to be retrieved by device later.</p>
     *
     * @param message a message to send.
     * @return an ID of a message.
     */
    @Transactional
    public int send(PushMessage message) {
        this.notificationMapper.insertPushMessage(message);
        this.notificationMapper.insertPendingPush(message.getId());
        return message.getId();
    }

    /**
     * <p>Gets the current status of delivery for the specified message.</p>
     *
     * @param messageId an ID of a message to get status for.
     * @return a status of message delivery. 0 - not sent, 1 - sent to device; or <code>null</code> if specified message
     *         is not found.
     */
    public Integer getStatus(int messageId) {
        return this.notificationMapper.getDeliveryStatus(messageId);
    }

    /**
     * <p>Deletes the messages with lifespans exceeding the specified limits.</p>
     *
     * @param nonDeliveredMessagesLifeSpan a limit for lifespan for non-delivered messages (in seconds).
     * @param deliveredMessagesLifeSpan a limit for lifespan for delivered messages (in seconds).
     */
    @Transactional
    public void purgeMessages(int nonDeliveredMessagesLifeSpan, int deliveredMessagesLifeSpan) {
        this.notificationMapper.purgeMessages(nonDeliveredMessagesLifeSpan * 1000, deliveredMessagesLifeSpan * 1000);
    }
}
