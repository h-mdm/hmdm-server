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
import com.hmdm.plugins.push.persistence.domain.PluginPushSchedule;
import com.hmdm.plugins.push.rest.json.PushMessageFilter;
import com.hmdm.plugins.push.rest.json.PushScheduleFilter;
import org.apache.ibatis.annotations.*;

import java.util.List;

/**
 * <p>An ORM mapper for {@link PluginPushSchedule} domain objects.</p>
 *
 * @author seva
 */
public interface PushScheduleMapper {

    @Insert("INSERT INTO plugin_push_schedule " +
            "(customerId, deviceId, groupId, configurationId, scope, messageType, payload, comment, " +
            "min, minBit, hour, hourBit, day, dayBit, weekday, weekdayBit, month, monthBit) " +
            "VALUES " +
            "(#{customerId}, #{deviceId}, #{groupId}, #{configurationId}, #{scope}, #{messageType}, #{payload}, #{comment}, " +
            "#{min}, CAST(#{minBit} AS BIT(60)), #{hour}, CAST(#{hourBit} AS BIT(24)), " +
            "#{day}, CAST(#{dayBit} AS BIT(31)), #{weekday}, CAST(#{weekdayBit} AS BIT(7)), " +
            "#{month}, CAST(#{monthBit} AS BIT(12)))"
    )
    @SelectKey( statement = "SELECT currval('plugin_push_schedule_id_seq')",
            keyColumn = "id", keyProperty = "id", before = false, resultType = int.class )
    int insert(PluginPushSchedule msg);

    @Update("UPDATE plugin_push_schedule " +
            "SET deviceId=#{deviceId}, groupId=#{groupId}, configurationId=#{configurationId}, " +
            "scope=#{scope}, messageType=#{messageType}, payload=#{payload}, comment=#{comment}, " +
            "min=#{min}, minBit=CAST(#{minBit} AS BIT(60)), hour=#{hour}, hourBit=CAST(#{hourBit} AS BIT(24)), " +
            "day=#{day}, dayBit=CAST(#{dayBit} AS BIT(31)), weekday=#{weekday}, weekdayBit=CAST(#{weekdayBit} AS BIT(7)), " +
            "month=#{month}, monthBit=CAST(#{monthBit} AS BIT(12)) " +
            "WHERE id=#{id} AND customerId=#{customerId}"
    )
    int update(PluginPushSchedule msg);

    @Delete("DELETE FROM plugin_push_schedule WHERE id = #{id} AND customerId = #{customerId}")
    void delete(@Param("id") int id, @Param("customerId") int customerId);

    @Select("SELECT * FROM plugin_push_schedule WHERE " +
            "(minBit & CAST(#{minMask} AS BIT(60))) = CAST(#{minMask} AS BIT(60)) AND " +
            "(hourBit & CAST(#{hourMask} AS BIT(24))) = CAST(#{hourMask} AS BIT(24)) AND " +
            "(dayBit & CAST(#{dayMask} AS BIT(31))) = CAST(#{dayMask} AS BIT(31)) AND " +
            "(weekdayBit & CAST(#{weekdayMask} AS BIT(7))) = CAST(#{weekdayMask} AS BIT(7)) AND " +
            "(monthBit & CAST(#{monthMask} AS BIT(12))) = CAST(#{monthMask} AS BIT(12))"
    )
    List<PluginPushSchedule> findMatchingTime(@Param("minMask") String minMask,
                                             @Param("hourMask") String hourMask,
                                             @Param("dayMask") String dayMask,
                                             @Param("weekdayMask") String weekdayMask,
                                             @Param("monthMask") String monthMask);

    List<PluginPushSchedule> findAll(PushScheduleFilter filter);

    long countAll(PushScheduleFilter filter);
}
