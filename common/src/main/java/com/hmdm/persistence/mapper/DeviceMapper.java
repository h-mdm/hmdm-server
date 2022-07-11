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
import com.hmdm.persistence.domain.DeviceApplication;
import com.hmdm.service.DeviceApplicationsStatus;
import com.hmdm.service.DeviceConfigFilesStatus;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;
import com.hmdm.persistence.domain.Device;
import com.hmdm.persistence.domain.DeviceSearchRequest;
import com.hmdm.persistence.domain.Group;
import com.hmdm.rest.json.DeviceLookupItem;

public interface DeviceMapper {

    int insertDevice(Device device);

    void insertDeviceGroups(@Param("id") Integer deviceId,
                            @Param("groups") List<Integer> groups);

    @Delete({"DELETE FROM deviceGroups " +
            "WHERE deviceId=#{deviceId} " +
            "  AND groupId IN ( " +
            "      SELECT groups.id " +
            "      FROM groups " +
            "      INNER JOIN users ON users.id = #{userId} " +
            "      WHERE groups.customerId = #{customerId} " +
            "      AND (users.allDevicesAvailable AND users.customerId = #{customerId} " +
            "           OR " +
            "           EXISTS (SELECT 1 FROM userDeviceGroupsAccess access WHERE groups.id = access.groupId AND access.userId = users.id)" +
            "      )" +
            "  )"})
    void removeDeviceGroupsByDeviceId(
            @Param("userId") int userId,
            @Param("customerId") int customerId,
            @Param("deviceId") Integer deviceId);

    void updateDevice(Device device);

    Device getDeviceByNumber(@Param("number") String number);

    Device getDeviceByOldNumber(@Param("number") String number);

    Device getDeviceByNumberIgnoreCase(@Param("number") String number);

    Device getDeviceById(@Param("id") Integer id);

    List<Device> getAllDevices(DeviceSearchRequest deviceSearchRequest);

    @Select({"SELECT COUNT(*) " +
            "FROM devices " +
            "WHERE customerId = #{customerId}"})
    Long countAllDevicesForCustomer(@Param("customerId") Integer customerId);

    Long countAllDevices(DeviceSearchRequest filter);

    @Update({"UPDATE devices SET " +
            "  info = #{info}, " +
            "  lastUpdate = CAST(EXTRACT(EPOCH FROM NOW()) * 1000 AS BIGINT), " +
            "  enrollTime = COALESCE(enrollTime, CAST(EXTRACT(EPOCH FROM NOW()) * 1000 AS BIGINT)), " +
            "  imeiUpdateTs = #{imeiUpdateTs} " +
            "WHERE id = #{deviceId}"})
    void updateDeviceInfo(@Param("deviceId") Integer deviceId,
                          @Param("info") String info,
                          @Param("imeiUpdateTs") Long imeiUpdateTs);

    @Update({"UPDATE devices SET " +
            "  custom1 = #{custom1}, " +
            "  custom2 = #{custom2}, " +
            "  custom3 = #{custom3} " +
            "WHERE id = #{deviceId}"})
    void updateDeviceCustomProperties(@Param("deviceId") Integer deviceId,
                                      @Param("custom1") String custom1,
                                      @Param("custom2") String custom2,
                                      @Param("custom3") String custom3);

    @Update({"UPDATE devices SET oldNumber = null " +
            "WHERE id = #{deviceId}"})
    void clearOldNumber(@Param("deviceId") Integer deviceId);

    List<DeviceLookupItem> lookupDevices(@Param("userId") int userId,
                                         @Param("customerId") int customerId,
                                         @Param("filter") String filter,
                                         @Param("limit") int limit);

    @Delete({"DELETE FROM devices WHERE id = #{id}"})
    void removeDevice(@Param("id") Integer id);

    @Update({"UPDATE devices SET configurationId = #{configurationId} WHERE id = #{deviceId}"})
    void updateDeviceConfiguration(@Param("deviceId") Integer deviceId,
                                   @Param("configurationId") Integer configurationId);

    @Update({"UPDATE devices SET description = #{description} WHERE id = #{deviceId}"})
    void updateDeviceDescription(@Param("deviceId") Integer deviceId,
                                 @Param("description") String newDeviceDesc);

    @Update({"UPDATE devices SET fastSearch = RIGHT(number, #{fastSearchChars}) WHERE fastSearch IS NULL " +
            " OR LENGTH(fastSearch) != #{fastSearchChars}"})
    void updateFastSearch(@Param("fastSearchChars") Integer fastSearchChars);

    List<Group> getAllGroups(@Param("customerId") int customerId,
                             @Param("userId") Integer userId);

    List<Group> getAllGroupsByValue(@Param("customerId") int customerId,
                                    @Param("value") String value,
                                    @Param("userId") Integer userId);

    @Select({"SELECT * FROM groups WHERE customerId=#{customerId} AND name = #{name}"})
    Group getGroupByName(@Param("customerId") int customerId, @Param("name") String name);

    @Insert({"INSERT INTO groups (name, customerId) VALUES (#{name}, #{customerId})"})
    void insertGroup(Group group);

    @Update({"UPDATE groups SET name = #{name} WHERE id = #{id}"})
    void updateGroup(Group group);

    @Select({"SELECT COUNT(*) " +
            "FROM deviceGroups " +
            "WHERE groupId = #{groupId}"})
    Long countDevicesByGroupId(@Param("groupId") Integer groupId);

    @Delete({"DELETE FROM groups WHERE id = #{id}"})
    void removeGroupById(@Param("id") Integer id);

    @Select({"SELECT * FROM groups WHERE id = #{id}"})
    Group getGroupById(@Param("id") Integer id);

    @Select("SELECT devices.id FROM devices WHERE customerId = #{customerId} AND configurationId = #{configurationId}")
    List<Device> getDeviceIdsByConfigurationId(@Param("customerId") Integer customerId,
                                               @Param("configurationId") int configurationId);

    @Select("SELECT devices.id FROM devices WHERE configurationId = #{configurationId}")
    List<Device> getDeviceIdsBySoleConfigurationId(@Param("configurationId") int configurationId);

    void insertDeviceApplicationSettings(@Param("id") Integer deviceId,
                                         @Param("appSettings") List<ApplicationSetting> applicationSettings);

    @Delete("DELETE FROM deviceApplicationSettings WHERE extRefId = #{id}")
    void deleteDeviceApplicationSettings(@Param("id") Integer deviceId);

    @Select("SELECT " +
            "    deviceApps.app::json ->> 'pkg' AS pkg, " +
            "    deviceApps.app::json ->> 'version' AS version, " +
            "    deviceApps.app::json ->> 'name' AS name " +
            "FROM (" +
            "    SELECT json_array_elements(info::json -> 'applications') AS app " +
            "    FROM devices " +
            "    WHERE id = #{deviceId}" +
            ") deviceApps")
    List<DeviceApplication> getDeviceInstalledApplications(@Param("deviceId") int deviceId);

    @Update("INSERT INTO deviceStatuses (deviceId, configFilesStatus, applicationsStatus) " +
            "VALUES (#{deviceId}, #{filesStatus}, #{appsStatus})" +
            "ON CONFLICT ON CONSTRAINT deviceStatuses_pr_key DO " +
            "UPDATE SET configFilesStatus = EXCLUDED.configFilesStatus, applicationsStatus = EXCLUDED.applicationsStatus")
    int updateDeviceStatuses(@Param("deviceId") Integer deviceId,
                             @Param("filesStatus") DeviceConfigFilesStatus deviceConfigFilesStatus,
                             @Param("appsStatus") DeviceApplicationsStatus deviceApplicatiosStatus);

    @Select("SELECT id FROM devices")
    List<Integer> getAllDeviceIds();
}
