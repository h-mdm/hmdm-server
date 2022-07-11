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

import com.hmdm.persistence.domain.UserRoleSettings;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

/**
 * <p>An ORM Mapper for {@link UserRoleSettings} domain object.</p>
 *
 * @author isv
 */
public interface UserRoleSettingsMapper {

    @Select({"SELECT * " +
            "FROM userRoleSettings " +
            "WHERE customerId = #{customerId} AND roleId = #{roleId}" +
            "LIMIT 1"})
    UserRoleSettings getUserRoleSettings(@Param("customerId") int customerId, @Param("roleId") int roleId);

    @Insert({
            "INSERT INTO userRoleSettings (" +
                    "columnDisplayedDeviceStatus, " +
                    "columnDisplayedDeviceDate, " +
                    "columnDisplayedDeviceNumber, " +
                    "columnDisplayedDeviceModel, " +
                    "columnDisplayedDevicePermissionsStatus, " +
                    "columnDisplayedDeviceAppInstallStatus, " +
                    "columnDisplayedDeviceFilesStatus, " +
                    "columnDisplayedDeviceConfiguration," +
                    "columnDisplayedDeviceImei," +
                    "columnDisplayedDevicePhone," +
                    "columnDisplayedDeviceDesc," +
                    "columnDisplayedDeviceGroup," +
                    "columnDisplayedLauncherVersion," +
                    "columnDisplayedBatteryLevel," +
                    "columnDisplayedDefaultLauncher," +
                    "columnDisplayedMdmMode," +
                    "columnDisplayedKioskMode," +
                    "columnDisplayedAndroidVersion," +
                    "columnDisplayedEnrollmentDate," +
                    "columnDisplayedSerial," +
                    "columnDisplayedCustom1," +
                    "columnDisplayedCustom2," +
                    "columnDisplayedCustom3," +
                    "roleId," +
                    "customerId" +
                    ") VALUES (" +
                    "#{columnDisplayedDeviceStatus}, " +
                    "#{columnDisplayedDeviceDate}, " +
                    "#{columnDisplayedDeviceNumber}, " +
                    "#{columnDisplayedDeviceModel}, " +
                    "#{columnDisplayedDevicePermissionsStatus}, " +
                    "#{columnDisplayedDeviceAppInstallStatus}, " +
                    "#{columnDisplayedDeviceFilesStatus}, " +
                    "#{columnDisplayedDeviceConfiguration}, " +
                    "#{columnDisplayedDeviceImei}, " +
                    "#{columnDisplayedDevicePhone}," +
                    "#{columnDisplayedDeviceDesc}," +
                    "#{columnDisplayedDeviceGroup}," +
                    "#{columnDisplayedLauncherVersion}," +
                    "#{columnDisplayedBatteryLevel}," +
                    "#{columnDisplayedDefaultLauncher}," +
                    "#{columnDisplayedMdmMode}," +
                    "#{columnDisplayedKioskMode}," +
                    "#{columnDisplayedAndroidVersion}," +
                    "#{columnDisplayedEnrollmentDate}," +
                    "#{columnDisplayedSerial}," +
                    "#{columnDisplayedCustom1}," +
                    "#{columnDisplayedCustom2}," +
                    "#{columnDisplayedCustom3}," +
                    "#{roleId}," +
                    "#{customerId}" +
                    ") " +
                    "ON CONFLICT ON CONSTRAINT userRoleSettings_role_customer_uniq DO " +
                    "UPDATE SET " +
                    "columnDisplayedDeviceStatus = EXCLUDED.columnDisplayedDeviceStatus, " +
                    "columnDisplayedDeviceDate = EXCLUDED.columnDisplayedDeviceDate, " +
                    "columnDisplayedDeviceNumber = EXCLUDED.columnDisplayedDeviceNumber, " +
                    "columnDisplayedDeviceModel = EXCLUDED.columnDisplayedDeviceModel, " +
                    "columnDisplayedDevicePermissionsStatus = EXCLUDED.columnDisplayedDevicePermissionsStatus, " +
                    "columnDisplayedDeviceAppInstallStatus = EXCLUDED.columnDisplayedDeviceAppInstallStatus, " +
                    "columnDisplayedDeviceFilesStatus = EXCLUDED.columnDisplayedDeviceFilesStatus, " +
                    "columnDisplayedDeviceConfiguration = EXCLUDED.columnDisplayedDeviceConfiguration, " +
                    "columnDisplayedDeviceImei = EXCLUDED.columnDisplayedDeviceImei, " +
                    "columnDisplayedDevicePhone = EXCLUDED.columnDisplayedDevicePhone, " +
                    "columnDisplayedDeviceDesc = EXCLUDED.columnDisplayedDeviceDesc, " +
                    "columnDisplayedDeviceGroup = EXCLUDED.columnDisplayedDeviceGroup, " +
                    "columnDisplayedLauncherVersion = EXCLUDED.columnDisplayedLauncherVersion, " +
                    "columnDisplayedBatteryLevel = EXCLUDED.columnDisplayedBatteryLevel, " +
                    "columnDisplayedDefaultLauncher = EXCLUDED.columnDisplayedDefaultLauncher, " +
                    "columnDisplayedMdmMode = EXCLUDED.columnDisplayedMdmMode, " +
                    "columnDisplayedKioskMode = EXCLUDED.columnDisplayedKioskMode, " +
                    "columnDisplayedAndroidVersion = EXCLUDED.columnDisplayedAndroidVersion, " +
                    "columnDisplayedEnrollmentDate = EXCLUDED.columnDisplayedEnrollmentDate, " +
                    "columnDisplayedSerial = EXCLUDED.columnDisplayedSerial, " +
                    "columnDisplayedCustom1 = EXCLUDED.columnDisplayedCustom1, " +
                    "columnDisplayedCustom2 = EXCLUDED.columnDisplayedCustom2, " +
                    "columnDisplayedCustom3 = EXCLUDED.columnDisplayedCustom3"
    })
    void saveUserRoleCommonSettings(UserRoleSettings settings);
}
