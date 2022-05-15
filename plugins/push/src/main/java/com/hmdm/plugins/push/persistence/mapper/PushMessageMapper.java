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

package com.hmdm.plugins.push.persistence.mapper;

import com.hmdm.plugins.push.persistence.domain.PluginPushMessage;
import com.hmdm.plugins.push.rest.json.PushMessageFilter;
import org.apache.ibatis.annotations.*;

import java.util.List;

/**
 * <p>An ORM mapper for {@link PluginPushMessage} domain objects.</p>
 *
 * @author seva
 */
public interface PushMessageMapper {

    @Insert("INSERT INTO plugin_push_messages " +
            "(customerId, deviceId, ts, messageType, payload) " +
            "VALUES " +
            "(#{customerId}, #{deviceId}, #{ts}, #{messageType}, #{payload})"
    )
    @SelectKey( statement = "SELECT currval('plugin_push_messages_id_seq')",
            keyColumn = "id", keyProperty = "id", before = false, resultType = int.class )
    int insertMessage(PluginPushMessage msg);

    @Delete("DELETE FROM plugin_push_messages WHERE id = #{id}")
    void deleteMessage(@Param("id") int id);

    void purgeOldMessages(@Param("ts") long ts, @Param("customerId") int customerId);

    List<PluginPushMessage> findAllMessages(PushMessageFilter filter);

    long countAll(PushMessageFilter filter);
}
