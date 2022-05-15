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

package com.hmdm.persistence.mapper;

import com.hmdm.persistence.domain.ApplicationSetting;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

public interface ApplicationSettingMapper {

    @Select("SELECT appSettings.*, " +
            "       applications.pkg AS applicationPkg, " +
            "       applications.name AS applicationName " +
            "FROM configurationApplicationSettings appSettings " +
            "INNER JOIN applications ON applications.id = appSettings.applicationID " +
            "WHERE appSettings.extRefId = #{id}")
    List<ApplicationSetting> getApplicationSettingsByConfigurationId(@Param("id") int configurationId);

    @Select("SELECT appSettings.*, " +
            "       applications.pkg AS applicationPkg, " +
            "       applications.name AS applicationName " +
            "FROM configurationApplicationSettings appSettings " +
            "INNER JOIN applications ON applications.id = appSettings.applicationID " +
            "WHERE appSettings.extRefId = #{id} " +
            "AND applications.pkg = #{pkg} ")
    List<ApplicationSetting> getConfigAppSettings(@Param("id") int configurationId, @Param("pkg") String pkg);

    @Select("SELECT appSettings.*, " +
            "       applications.pkg AS applicationPkg, " +
            "       applications.name AS applicationName " +
            "FROM deviceApplicationSettings appSettings " +
            "INNER JOIN applications ON applications.id = appSettings.applicationID " +
            "WHERE appSettings.extRefId = #{id}")
    List<ApplicationSetting> getApplicationSettingsByDeviceId(@Param("id") int deviceId);

    @Select("SELECT appSettings.*, " +
            "       applications.pkg AS applicationPkg, " +
            "       applications.name AS applicationName " +
            "FROM deviceApplicationSettings appSettings " +
            "INNER JOIN applications ON applications.id = appSettings.applicationID " +
            "WHERE appSettings.extRefId = #{id} " +
            "AND applications.pkg = #{pkg} ")
    List<ApplicationSetting> getDeviceAppSettings(@Param("id") int deviceId, @Param("pkg") String pkg);

    @Delete("DELETE FROM configurationApplicationSettings WHERE extRefId=#{configurationId} AND applicationId=#{applicationId} AND name=#{name}")
    void deleteApplicationSettingByName(@Param("configurationId") int configurationId,
                                        @Param("applicationId") int applicationId,
                                        @Param("name") String name);

    @Delete("DELETE FROM configurationApplicationSettings WHERE extRefId=#{configurationId} AND applicationId=#{applicationId}")
    void deleteApplicationSettingByApp(@Param("configurationId") int configurationId,
                                        @Param("applicationId") int applicationId);

    @Insert("INSERT INTO configurationApplicationSettings (extRefId, applicationId, name, type, value, comment, readonly, lastUpdate) " +
            "        VALUES (#{configurationId}, #{item.applicationId}, #{item.name}, #{item.type}, #{item.value}, #{item.comment}, #{item.readonly}, #{item.lastUpdate})")
    void insertApplicationSetting(@Param("configurationId") int configurationId, @Param("item") ApplicationSetting setting);
}
