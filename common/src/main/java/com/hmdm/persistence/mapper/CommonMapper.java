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
import com.hmdm.persistence.domain.Settings;
import org.apache.ibatis.annotations.SelectProvider;

public interface CommonMapper {
    @Select({"SELECT * " +
            "FROM settings " +
            "WHERE customerId = #{customerId} " +
            "LIMIT 1"})
    Settings getSettings(@Param("customerId") int customerId);

//    @Select({"SELECT * " +
//            "FROM settings " +
//            "WHERE EXISTS (SELECT 1 " +
//            "              FROM devices " +
//            "              WHERE devices.number = #{deviceId} " +
//            "              AND devices.customerId = settings.customerId)"})
//    Settings getSettingsByDeviceId(@Param("deviceId") String deviceId);

    @Insert({
            "INSERT INTO settings (" +
                    "backgroundColor, " +
                    "textColor, " +
                    "backgroundImageUrl, " +
                    "iconSize, " +
                    "desktopHeader, " +
                    "desktopHeaderTemplate, " +
                    "customerId" +
                    ") VALUES (" +
                    "#{backgroundColor}, " +
                    "#{textColor}, " +
                    "#{backgroundImageUrl}, " +
                    "#{iconSize}, " +
                    "#{desktopHeader}, " +
                    "#{desktopHeaderTemplate}, " +
                    "#{customerId}" +
                    ") " +
                    "ON CONFLICT ON CONSTRAINT settings_customer_unique DO " +
                    "UPDATE SET " +
                    "backgroundColor = EXCLUDED.backgroundColor, " +
                    "textColor = EXCLUDED.textColor, " +
                    "backgroundImageUrl = EXCLUDED.backgroundImageUrl, " +
                    "iconSize = EXCLUDED.iconSize, " +
                    "desktopHeader = EXCLUDED.desktopHeader, " +
                    "desktopHeaderTemplate = EXCLUDED.desktopHeaderTemplate"
    })
    void saveDefaultDesignSettings(Settings settings);

    @Insert({
            "INSERT INTO settings (" +
                    "useDefaultLanguage, " +
                    "language, " +
                    "customerId" +
                    ") VALUES (" +
                    "#{useDefaultLanguage}, " +
                    "#{language}, " +
                    "#{customerId}" +
                    ") " +
                    "ON CONFLICT ON CONSTRAINT settings_customer_unique DO " +
                    "UPDATE SET " +
                    "useDefaultLanguage = EXCLUDED.useDefaultLanguage, " +
                    "language = EXCLUDED.language"
    })
    void saveLanguageSettings(Settings settings);

    @Insert({
            "INSERT INTO settings (" +
                    "createNewDevices, " +
                    "newDeviceGroupId, " +
                    "newDeviceConfigurationId, " +
                    "phoneNumberFormat, " +
                    "customPropertyName1, " +
                    "customPropertyName2, " +
                    "customPropertyName3, " +
                    "customMultiline1, " +
                    "customMultiline2, " +
                    "customMultiline3, " +
                    "customSend1, " +
                    "customSend2, " +
                    "customSend3, " +
                    "sendDescription, " +
                    "passwordReset, " +
                    "passwordLength, " +
                    "passwordStrength, " +
                    "customerId" +
                    ") VALUES (" +
                    "#{createNewDevices}, " +
                    "#{newDeviceGroupId}, " +
                    "#{newDeviceConfigurationId}, " +
                    "#{phoneNumberFormat}, " +
                    "#{customPropertyName1}, " +
                    "#{customPropertyName2}, " +
                    "#{customPropertyName3}, " +
                    "#{customMultiline1}, " +
                    "#{customMultiline2}, " +
                    "#{customMultiline3}, " +
                    "#{customSend1}, " +
                    "#{customSend2}, " +
                    "#{customSend3}, " +
                    "#{sendDescription}, " +
                    "#{passwordReset}, " +
                    "#{passwordLength}, " +
                    "#{passwordStrength}, " +
                    "#{customerId}" +
                    ") " +
                    "ON CONFLICT ON CONSTRAINT settings_customer_unique DO " +
                    "UPDATE SET " +
                    "createNewDevices = EXCLUDED.createNewDevices, " +
                    "newDeviceGroupId = EXCLUDED.newDeviceGroupId, " +
                    "newDeviceConfigurationId = EXCLUDED.newDeviceConfigurationId, " +
                    "phoneNumberFormat = EXCLUDED.phoneNumberFormat, " +
                    "customPropertyName1 = EXCLUDED.customPropertyName1, " +
                    "customPropertyName2 = EXCLUDED.customPropertyName2, " +
                    "customPropertyName3 = EXCLUDED.customPropertyName3, " +
                    "customMultiline1 = EXCLUDED.customMultiline1, " +
                    "customMultiline2 = EXCLUDED.customMultiline2, " +
                    "customMultiline3 = EXCLUDED.customMultiline3, " +
                    "customSend1 = EXCLUDED.customSend1, " +
                    "customSend2 = EXCLUDED.customSend2, " +
                    "customSend3 = EXCLUDED.customSend3, " +
                    "sendDescription = EXCLUDED.sendDescription, " +
                    "passwordReset = EXCLUDED.passwordReset, " +
                    "passwordLength = EXCLUDED.passwordLength, " +
                    "passwordStrength = EXCLUDED.passwordStrength "
    })
    void saveMiscSettings(Settings settings);

    @Select({"SELECT COUNT(*) FROM settings"})
    int getSettingsCount();

    @Insert({"${query}"})
    void executeRawQuery(String query);
}