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

package com.hmdm.persistence;

import com.google.inject.Inject;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import com.hmdm.persistence.domain.ApplicationSetting;
import org.mybatis.guice.transactional.Transactional;
import com.hmdm.persistence.domain.Device;
import com.hmdm.persistence.domain.DeviceSearchRequest;
import com.hmdm.persistence.mapper.DeviceMapper;
import com.hmdm.rest.json.DeviceLookupItem;
import com.hmdm.rest.json.LookupItem;
import com.hmdm.rest.json.PaginatedData;
import com.hmdm.security.SecurityContext;
import com.hmdm.security.SecurityException;

public class DeviceDAO extends AbstractDAO<Device> {
    private final DeviceMapper mapper;
    private final ApplicationSettingDAO applicationSettingDAO;

    @Inject
    public DeviceDAO(DeviceMapper mapper, ApplicationSettingDAO applicationSettingDAO) {
        this.mapper = mapper;
        this.applicationSettingDAO = applicationSettingDAO;
    }

    public PaginatedData<Device> getAllDevices(DeviceSearchRequest request) {
        List<Device> devices = getListWithCurrentUser(currentUser -> {
            request.setCustomerId(currentUser.getCustomerId());
            request.setUserId(currentUser.getId());
            return this.mapper.getAllDevices(request);
        });

        Long totalItemsCount = this.mapper.countAllDevices(request);
        return new PaginatedData<>(devices, totalItemsCount);
    }

    public List<Device> getDeviceIdsByConfigurationId(int configurationId) {
        return getList(customerId -> this.mapper.getDeviceIdsByConfigurationId(customerId, configurationId));
    }

    public List<ApplicationSetting> getDeviceApplicationSettings(int deviceId) {
        final Device dbDevice
                = getSingleRecord(() -> this.mapper.getDeviceById(deviceId), SecurityException::onDeviceAccessViolation);
        if (dbDevice != null) {
            final List<ApplicationSetting> deviceAppSettings
                    = this.applicationSettingDAO.getApplicationSettingsByDeviceId(dbDevice.getId());
            return deviceAppSettings;
        } else {
            return new ArrayList<>();
        }
    }

    public void removeDeviceById(Integer id) {
        updateById(
                id,
                this::getDeviceById,
                device -> this.mapper.removeDevice(device.getId()),
                SecurityException::onDeviceAccessViolation
        );
    }

    public void updateDeviceConfiguration(Integer deviceId, Integer configurationId) {
        updateById(
                deviceId,
                this::getDeviceById,
                device -> this.mapper.updateDeviceConfiguration(device.getId(), configurationId),
                SecurityException::onDeviceAccessViolation
        );
    }

    public Device getDeviceByNumber(String number) {
        return getSingleRecord(() -> this.mapper.getDeviceByNumber(number), SecurityException::onDeviceAccessViolation);
    }

    public Device getDeviceByNumberIgnoreCase(String number) {
        return getSingleRecord(() -> this.mapper.getDeviceByNumberIgnoreCase(number), SecurityException::onDeviceAccessViolation);
    }

    @Transactional
    public void insertDevice(Device device) {
        insertRecord(device, d -> {
            this.mapper.insertDevice(d);
            if (d.getGroups() != null && !d.getGroups().isEmpty()) {
                this.mapper.insertDeviceGroups(
                        d.getId(), d.getGroups().stream().map(LookupItem::getId).collect(Collectors.toList())
                );
            }
        });
    }

    @Transactional
    public void updateDevice(Device device) {
        updateRecord(device, d -> {
            Integer currentUserId = SecurityContext.get().getCurrentUser().get().getId();
            this.mapper.updateDevice(d);
            this.mapper.removeDeviceGroupsByDeviceId(currentUserId, d.getCustomerId(), d.getId());
            if (d.getGroups() != null && !d.getGroups().isEmpty()) {
                this.mapper.insertDeviceGroups(
                        d.getId(), d.getGroups().stream().map(LookupItem::getId).collect(Collectors.toList())
                );
            }
        }, SecurityException::onDeviceAccessViolation);
    }

    public Device getDeviceById(Integer id) {
        return this.mapper.getDeviceById(id);
    }

    @Deprecated
    public void updateDeviceOldConfiguration(Integer id, Integer configurationId) {
        updateById(
                id,
                this::getDeviceById,
                device -> this.mapper.updateDeviceOldConfiguration(device.getId(), configurationId),
                SecurityException::onDeviceAccessViolation
        );
    }

    /**
     * <p>Gets the lookup list of devices matching the specified filter.</p>
     *
     * @param filter a filter to be used for filtering the records.
     * @param resultsCount a maximum number of items to be included to list.
     * @return a response with list of devices matching the specified filter.
     */
    public List<DeviceLookupItem> findDevices(String filter, int resultsCount) {
        String searchFilter = '%' + filter.trim() + '%';
        return SecurityContext.get().getCurrentUser()
                .map(u -> this.mapper.lookupDevices(u.getId(), u.getCustomerId(), searchFilter, resultsCount))
                .orElse(new ArrayList<>());
    }

    @Transactional
    public void saveDeviceApplicationSettings(Integer deviceId, List<ApplicationSetting> applicationSettings) {
        final Device dbDevice = getSingleRecord(() -> this.mapper.getDeviceById(deviceId), SecurityException::onDeviceAccessViolation);
        if (dbDevice != null) {
            this.mapper.deleteDeviceApplicationSettings(dbDevice.getId());
            this.mapper.insertDeviceApplicationSettings(dbDevice.getId(), applicationSettings);
        }
    }
}
