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

package com.hmdm.notification.persistence.mapper;

import com.hmdm.notification.persistence.domain.PushMessage;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.SelectKey;

import java.util.List;

/**
 * <p>An ORM mapper for <code>Notification</code> sub-system domain objects.</p>
 *
 * @author isv
 */
public interface NotificationMapper {

    @Select("SELECT pushMessages.* " +
            "FROM pendingPushes " +
            "INNER JOIN pushMessages ON pushMessages.id = pendingPushes.messageId " +
            "INNER JOIN devices ON devices.id = pushMessages.deviceId " +
            "WHERE (devices.number = #{deviceNumber} OR devices.oldNumber = #{deviceNumber}) " +
            "AND pendingPushes.status = 0 " +
            "ORDER BY pendingPushes.createTime ASC")
    List<PushMessage> getPendingMessagesForDelivery(@Param("deviceNumber") String deviceId);

    void markMessagesAsDelivered(@Param("messageIds") List<Integer> messageIds);

    @Insert("INSERT INTO pushMessages (messageType, deviceId, payload) " +
            "VALUES (#{messageType}, #{deviceId}, #{payload})")
    @SelectKey( statement = "SELECT currval('pushmessages_id_seq')", keyColumn = "id", keyProperty = "id", before = false, resultType = int.class )
    void insertPushMessage(PushMessage message);

    @Insert("INSERT INTO pendingPushes (messageId, status, createTime) " +
            "VALUES (#{messageId}, 0, EXTRACT(EPOCH FROM NOW()) * 1000)")
    void insertPendingPush(int messageId);

    @Select("SELECT status FROM pendingPushes WHERE messageId = #{messageId}")
    Integer getDeliveryStatus(@Param("messageId") int messageId);

    @Delete("DELETE FROM pushMessages " +
            "WHERE EXISTS " +
            "(" +
            " SELECT 1 " +
            " FROM pendingPushes " +
            " WHERE pendingPushes.messageId = pushMessages.id " +
            " AND (" +
            "      pendingPushes.status = 0 AND EXTRACT(EPOCH FROM NOW()) * 1000 - pendingPushes.createTime >= #{d1}" +
            "      OR " +
            "      pendingPushes.status = 1 AND NOT pendingPushes.sendTime IS NULL AND EXTRACT(EPOCH FROM NOW()) * 1000 - pendingPushes.sendTime >= #{d2}" +
            "     )" +
            ")")
    void purgeMessages(@Param("d1") long nonDeliveredMessagesLifeSpan, @Param("d2") long deliveredMessagesLifeSpan);
}
