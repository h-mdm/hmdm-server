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

import java.util.List;

import com.hmdm.persistence.domain.ApplicationSetting;
import com.hmdm.persistence.domain.ConfigurationApplicationParameters;
import com.hmdm.persistence.domain.ConfigurationFile;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.SelectKey;
import org.apache.ibatis.annotations.Update;
import com.hmdm.persistence.domain.Application;
import com.hmdm.persistence.domain.Configuration;

public interface ConfigurationMapper {
    @Select({"SELECT * " +
            "FROM configurations " +
            "WHERE customerId=#{customerId} " +
            "AND type=#{type} " +
            "ORDER BY name"})
    List<Configuration> getAllConfigurationsByType(@Param("customerId") int customerId, @Param("type") int type);

    @Select({"SELECT * " +
            "FROM configurations " +
            "WHERE customerId=#{customerId} " +
            "AND type=#{type} " +
            "AND (name ILIKE #{value} OR description ILIKE #{value}) ORDER BY name"})
    List<Configuration> getAllConfigurationsByTypeAndValue(@Param("customerId") int customerId,
                                                           @Param("type") int type,
                                                           @Param("value") String value);

    @SelectKey( statement = "SELECT currval('configurations_id_seq')", keyColumn = "id", keyProperty = "id", before = false, resultType = int.class )
    void insertConfiguration(Configuration configuration);

    @Update({"UPDATE configurations SET " +
            "name=#{name}, " +
            "description=#{description}, " +
            "password=#{password}, " +
            "backgroundColor=#{backgroundColor}, " +
            "textColor=#{textColor}, " +
            "backgroundImageUrl=#{backgroundImageUrl}, " +
            "iconSize=#{iconSize}, " +
            "desktopHeader=#{desktopHeader}, " +
            "desktopHeaderTemplate=#{desktopHeaderTemplate}, " +
            "requestUpdates=#{requestUpdates}, " +
            "pushOptions=#{pushOptions}, " +
            "keepaliveTime=#{keepaliveTime}, " +
            "autoBrightness=#{autoBrightness}, " +
            "brightness=#{brightness}, " +
            "manageTimeout=#{manageTimeout}, " +
            "timeout=#{timeout}, " +
            "lockVolume=#{lockVolume}, " +
            "manageVolume=#{manageVolume}, " +
            "volume=#{volume}, " +
            "passwordMode=#{passwordMode}, " +
            "orientation=#{orientation}, " +
            "runDefaultLauncher=#{runDefaultLauncher}, " +
            "disableScreenshots=#{disableScreenshots}, " +
            "useDefaultDesignSettings=#{useDefaultDesignSettings}, " +
            "timeZone=#{timeZone}, " +
            "allowedClasses=#{allowedClasses}, " +
            "newServerUrl=#{newServerUrl}, " +
            "lockSafeSettings=#{lockSafeSettings}, " +
            "showWifi=#{showWifi}, " +
            "gps=#{gps}, " +
            "bluetooth=#{bluetooth}, " +
            "wifi=#{wifi}, " +
            "mobileData=#{mobileData}, " +
            "usbStorage=#{usbStorage}, " +
            "mainAppId=#{mainAppId}, " +
            "contentAppId=#{contentAppId}, " +
            "eventReceivingComponent=#{eventReceivingComponent}, " +
            "kioskMode=#{kioskMode}, " +
            "wifiSSID=#{wifiSSID}, " +
            "wifiPassword=#{wifiPassword}, " +
            "wifiSecurityType=#{wifiSecurityType}, " +
            "mobileEnrollment=#{mobileEnrollment}, " +
            "kioskHome=#{kioskHome}, " +
            "kioskRecents=#{kioskRecents}, " +
            "kioskNotifications=#{kioskNotifications}, " +
            "kioskSystemInfo=#{kioskSystemInfo}, " +
            "kioskKeyguard=#{kioskKeyguard}, " +
            "kioskLockButtons=#{kioskLockButtons}, " +
            "restrictions=#{restrictions}, " +
            "autoUpdate=#{autoUpdate}, " +
            "blockStatusBar=#{blockStatusBar}, " +
            "systemUpdateType=#{systemUpdateType}, " +
            "systemUpdateFrom=#{systemUpdateFrom}, " +
            "systemUpdateTo=#{systemUpdateTo}, " +
            "scheduleAppUpdate=#{scheduleAppUpdate}, " +
            "appUpdateFrom=#{appUpdateFrom}, " +
            "appUpdateTo=#{appUpdateTo}, " +
            "defaultFilePath=#{defaultFilePath} " +
            "WHERE id=#{id}"})
    void updateConfiguration(Configuration configuration);

    @Delete({"DELETE FROM configurations WHERE id=#{id}"})
    void removeConfigurationById(@Param("id") Integer id);

    @Select({"SELECT * " +
            "FROM configurations " +
            "WHERE customerId=#{customerId} " +
            "AND name=#{name} LIMIT 1"})
    Configuration getConfigurationByName(@Param("customerId") int customerId, @Param("name") String name);

    void insertConfigurationApplications(@Param("id") Integer id, @Param("apps") List<Application> applications);

    void insertConfigurationApplicationSettings(@Param("id") Integer id,
                                                @Param("appSettings") List<ApplicationSetting> applicationSettings);

    void saveConfigurationApplicationUsageParameters(
            @Param("id") Integer configurationId,
            @Param("appUsageParameters") List<ConfigurationApplicationParameters> applicationUsageParameters
    );

    @Delete({"DELETE FROM configurationApplications WHERE configurationId=#{id}"})
    void removeConfigurationApplicationsById(@Param("id") Integer id);

    @Delete({"DELETE FROM configurationApplicationSettings WHERE extRefId = #{id}"})
    void removeConfigurationApplicationSettingsById(@Param("id") Integer id);

    /**
     * <p>Gets the list of all existing applications in context of applications usage by specified configuration.</p>
     *
     * @param customerId an ID of a customer account set for current user.
     * @param id an ID of a configuration.
     * @return a list of all existing applications with set parameters of usage by specified configuration.
     */
    List<Application> getConfigurationApplications(@Param("customerId") Integer customerId, @Param("id") Integer id);

    /**
     * <p>Gets the list of applications used by specified configuration.</p>
     *
     * @param customerId an ID of a customer account set for current user.
     * @param id an ID of a configuration.
     * @return a list of all existing applications with set parameters of usage by specified configuration.
     */
    List<Application> getPlainConfigurationApplications(@Param("customerId") Integer customerId, @Param("id") Integer id);

    /**
     * <p>Gets the list of applications used by specified configuration.</p>
     *
     * @param id an ID of a configuration.
     * @return a list of all existing applications with set parameters of usage by specified configuration.
     */
    List<Application> getPlainConfigurationSoleApplications(@Param("id") Integer id);

    @Select("SELECT EXISTS( " +
            "SELECT * FROM configurationApplications ca " +
            "LEFT JOIN applications a ON ca.applicationId=a.id " +
            "WHERE a.pkg=#{pkg} AND ca.configurationId=#{configurationId} " +
            ")")
    boolean isAppInstalledInConfiguration(@Param("pkg") String pkg, @Param("configurationId") Integer configurationId);

    @Select("SELECT * FROM configurationApplicationParameters WHERE configurationId = #{id}")
    List<ConfigurationApplicationParameters> getApplicationParameters(@Param("id") Integer configurationId);

    @Select({"SELECT * FROM configurations WHERE id=#{id}"})
    Configuration getConfigurationById(@Param("id") Integer id);

    @Select({"SELECT * FROM configurations WHERE qrCodeKey=#{key}"})
    Configuration getConfigurationByQRCodeKey(@Param("key") String key);

    /**
     * <p>Upgrades the version of specified application used in specified configuration to most recent one.</p>
     *
     * @param configurationId an ID of a configuration to upgrade application version for.
     * @param applicationId an ID of an application to upgrade to most recent version.
     */
    @Update("SELECT mdm_config_app_upgrade(#{configId}, #{appId})")
    void upgradeConfigurationApplication(@Param("configId") Integer configurationId,
                                         @Param("appId") Integer applicationId);

    @Select("SELECT COUNT(*) FROM devices WHERE configurationId = #{id}")
    long countDevices(@Param("id") Integer id);

    void insertConfigurationFiles(@Param("id") Integer configurationId,
                                  @Param("files") List<ConfigurationFile> files);

    @Delete({"DELETE FROM configurationFiles WHERE configurationId = #{id}"})
    void removeConfigurationFilesById(@Param("id") Integer configurationId);
}
