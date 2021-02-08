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

package com.hmdm.plugins.devicelog.persistence.postgres.dao.mapper;

import com.hmdm.plugins.devicelog.persistence.postgres.dao.domain.PostgresDeviceLogPluginSettings;
import com.hmdm.plugins.devicelog.persistence.postgres.dao.domain.PostgresDeviceLogRecord;
import com.hmdm.plugins.devicelog.persistence.postgres.dao.domain.PostgresDeviceLogRule;
import com.hmdm.plugins.devicelog.rest.json.DeviceLogFilter;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.SelectKey;
import org.apache.ibatis.annotations.Update;

import java.util.List;

/**
 * <p>An ORM mapper for {@link PostgresDeviceLogRecord} domain object.</p>
 *
 * @author isv
 */
public interface PostgresDeviceLogMapper {

    List<PostgresDeviceLogRecord> findAllLogRecordsByCustomerId(DeviceLogFilter filter);

    long countAll(DeviceLogFilter filter);

    int insertDeviceLogRecords(@Param("logs") List<PostgresDeviceLogRecord> postgresLogs);

    // ------------ devicelog plugin settings ------------------------------------------------------------------------------
    PostgresDeviceLogPluginSettings findPluginSettingsByCustomerId(@Param("customerId") Integer customerId);


    @Insert("INSERT INTO plugin_devicelog_settings (customerId, logsPreservePeriod) " +
            "VALUES (#{customerId}, #{logsPreservePeriod})")
    @SelectKey( statement = "SELECT currval('plugin_devicelog_settings_id_seq')",
            keyColumn = "id", keyProperty = "id", before = false, resultType = int.class )
    void insertPluginSettings(PostgresDeviceLogPluginSettings postgresDeviceLogPluginSettings);

    @Update("UPDATE plugin_devicelog_settings SET logsPreservePeriod = #{logsPreservePeriod} WHERE id = #{id}")
    void updatePluginSettings(PostgresDeviceLogPluginSettings postgresDeviceLogPluginSettings);


    @Insert("INSERT INTO plugin_devicelog_settings_rules (" +
            "  settingId, " +
            "  name, " +
            "  active, " +
            "  applicationId, " +
            "  severity, " +
            "  filter, " +
            "  groupId, " +
            "  configurationId " +
            ") " +
            "VALUES (" +
            "  #{settingId}, " +
            "  #{name}, " +
            "  #{active}, " +
            "  #{applicationId}, " +
            "  #{severity}, " +
            "  #{filter}, " +
            "  #{groupId}, " +
            "  #{configurationId} " +
            ")")
    @SelectKey( statement = "SELECT currval('plugin_devicelog_settings_rules_id_seq')",
            keyColumn = "id", keyProperty = "id", before = false, resultType = int.class )
    void insertPluginSettingsRule(PostgresDeviceLogRule rule);

    @Update("UPDATE plugin_devicelog_settings_rules SET " +
            " settingId = #{settingId}, " +
            " name = #{name}, " +
            " active = #{active}, " +
            " applicationId = #{applicationId}, " +
            " severity = #{severity}, " +
            " filter = #{filter}, " +
            " groupId = #{groupId}, " +
            " configurationId = #{configurationId} " +
            "WHERE id = #{id}")
    void updatePluginSettingsRule(PostgresDeviceLogRule rule);

    @Delete("DELETE FROM plugin_devicelog_settings_rules WHERE id = #{id}")
    void deletePluginSettingRule(@Param("id") int id);

    @Select("SELECT settings.id, settings.customerId " +
            "FROM plugin_devicelog_settings_rules rules " +
            "INNER JOIN plugin_devicelog_settings settings ON settings.id = rules.settingId " +
            "WHERE rules.id = #{id}")
    PostgresDeviceLogPluginSettings getPluginSettingsByRuleIdForAuthorization(@Param("id") int id);

    @Select("SELECT * FROM plugin_devicelog_settings_rules WHERE id = #{id}")
    PostgresDeviceLogRule getPluginSettingsRule(@Param("id") int id);

    @Delete("DELETE FROM plugin_devicelog_setting_rule_devices WHERE ruleId = #{id}")
    void deletePluginSettingsRuleDevices(@Param("id") int ruleId);

    void insertPluginSettingsRuleDevices(@Param("ruleId") int ruleId, @Param("deviceIds") List<Integer> deviceIds);

    /**
     * <p>Deletes the log records which are older than number of days configured in customer's profile.</p>
     *
     * @return a number of deleted records.
     */
    @Delete("DELETE FROM plugin_devicelog_log " +
            "WHERE createTime < (SELECT EXTRACT(EPOCH FROM DATE_TRUNC('day', NOW() - (pds.logsPreservePeriod || ' day')::INTERVAL)) * 1000 " +
            "                    FROM plugin_devicelog_settings pds " +
            "                    WHERE pds.customerId = #{customerId})")
    int purgeLogRecords(@Param("customerId") int customerId);
}
