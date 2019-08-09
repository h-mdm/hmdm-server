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

import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;
import com.hmdm.persistence.domain.Settings;

public interface CommonMapper {
    @Select({"SELECT * " +
            "FROM settings " +
            "WHERE customerId = #{customerId} " +
            "LIMIT 1"})
    Settings getSettings(@Param("customerId") int customerId);

    @Insert({"INSERT INTO settings(backgroundColor, textColor, backgroundImageUrl, iconSize, desktopHeader, customerId) " +
            "VALUES (#{backgroundColor}, #{textColor}, #{backgroundImageUrl}, #{iconSize}, #{desktopHeader}, #{customerId})"})
    void insertDefaultDesignSettings(Settings settings);

    @Update({"UPDATE settings SET " +
            "backgroundColor=#{backgroundColor}, " +
            "textColor=#{textColor}, " +
            "backgroundImageUrl=#{backgroundImageUrl}, " +
            "iconSize=#{iconSize}, " +
            "desktopHeader=#{desktopHeader} " +
            "WHERE id = #{id}"})
    void updateDefaultDesignSettings(Settings settings);

    @Insert({"INSERT INTO settings(" +
            "columnDisplayedDeviceStatus, " +
            "columnDisplayedDeviceDate, " +
            "columnDisplayedDeviceNumber, " +
            "columnDisplayedDeviceModel, " +
            "columnDisplayedDevicePermissionsStatus, " +
            "columnDisplayedDeviceAppInstallStatus, " +
            "columnDisplayedDeviceConfiguration," +
            "columnDisplayedDeviceImei," +
            "columnDisplayedDevicePhone," +
            "columnDisplayedDeviceDesc," +
            "columnDisplayedDeviceGroup," +
            "columnDisplayedLauncherVersion," +
            "customerId" +
            ") VALUES (" +
            "#{columnDisplayedDeviceStatus}, " +
            "#{columnDisplayedDeviceDate}, " +
            "#{columnDisplayedDeviceNumber}, " +
            "#{columnDisplayedDeviceModel}, " +
            "#{columnDisplayedDevicePermissionsStatus}, " +
            "#{columnDisplayedDeviceAppInstallStatus}, " +
            "#{columnDisplayedDeviceConfiguration}, " +
            "#{columnDisplayedDeviceImei}, " +
            "#{columnDisplayedDevicePhone}," +
            "#{columnDisplayedDeviceDesc}," +
            "#{columnDisplayedDeviceGroup}," +
            "#{columnDisplayedLauncherVersion}," +
            "#{customerId}" +
            ")"})
    void insertCommonSettings(Settings settings);

    @Update({"UPDATE settings SET " +
            "columnDisplayedDeviceStatus=#{columnDisplayedDeviceStatus}, " +
            "columnDisplayedDeviceDate=#{columnDisplayedDeviceDate}, " +
            "columnDisplayedDeviceNumber=#{columnDisplayedDeviceNumber}, " +
            "columnDisplayedDeviceModel=#{columnDisplayedDeviceModel}, " +
            "columnDisplayedDevicePermissionsStatus=#{columnDisplayedDevicePermissionsStatus}, " +
            "columnDisplayedDeviceAppInstallStatus=#{columnDisplayedDeviceAppInstallStatus}, " +
            "columnDisplayedDeviceConfiguration=#{columnDisplayedDeviceConfiguration}, " +
            "columnDisplayedDeviceImei=#{columnDisplayedDeviceImei}, " +
            "columnDisplayedDevicePhone=#{columnDisplayedDevicePhone}, " +
            "columnDisplayedDeviceDesc=#{columnDisplayedDeviceDesc}, " +
            "columnDisplayedDeviceGroup=#{columnDisplayedDeviceGroup}, " +
            "columnDisplayedLauncherVersion=#{columnDisplayedLauncherVersion} " +
            "WHERE id = #{id}"})
    void updateCommonSettings(Settings settings);

    @Insert({"INSERT INTO settings(useDefaultLanguage, language, customerId) " +
            "VALUES (#{useDefaultLanguage}, #{language}, #{customerId})"})
    void insertLanguageSettings(Settings settings);

    @Update({"UPDATE settings SET " +
            "useDefaultLanguage=#{useDefaultLanguage}, " +
            "language=#{language} " +
            "WHERE id = #{id}"})
    void updateLanguageSettings(Settings settings);

}