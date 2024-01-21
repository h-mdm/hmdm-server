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

import java.util.*;
import java.util.stream.Collectors;

import com.google.inject.Injector;
import com.google.inject.Key;
import com.google.inject.Singleton;
import com.google.inject.name.Named;
import com.hmdm.event.DeviceInfoUpdatedEvent;
import com.hmdm.event.EventService;
import com.hmdm.persistence.domain.*;
import com.hmdm.rest.json.*;
import com.hmdm.service.DeviceApplicationsStatus;
import com.hmdm.service.DeviceConfigFilesStatus;
import org.apache.commons.math3.stat.descriptive.summary.Sum;
import org.mybatis.guice.transactional.Transactional;
import com.hmdm.persistence.mapper.DeviceMapper;
import com.hmdm.security.SecurityContext;
import com.hmdm.security.SecurityException;

@Singleton
public class DeviceDAO extends AbstractDAO<Device> {
    private final DeviceMapper mapper;
    private final ApplicationSettingDAO applicationSettingDAO;

    private final Set<DeviceListHook> deviceListHooks;
    private final EventService eventService;
    private final int fastSearchChars;

    @Inject
    public DeviceDAO(DeviceMapper mapper, ApplicationSettingDAO applicationSettingDAO, Injector injector,
                     EventService eventService, @Named("device.fast.search.chars") int fastSearchChars) {
        this.mapper = mapper;
        this.applicationSettingDAO = applicationSettingDAO;
        this.eventService = eventService;
        this.fastSearchChars = fastSearchChars;

        // TODO : Such a logic needs to be extracted into some utility service
        Set<DeviceListHook> hooks = new HashSet<>();
        for (Key<?> key : injector.getAllBindings().keySet()) {
            if (DeviceListHook.class.isAssignableFrom(key.getTypeLiteral().getRawType())) {
                DeviceListHook hook = (DeviceListHook) injector.getInstance(key);
                hooks.add(hook);
            }
        }

        this.deviceListHooks = hooks;
    }

    // UNSECURE, for sending stats (by superadmin in multi-tenant setup) only
    public long getTotalDevicesCount() {
        return this.mapper.countTotalDevices();

/*        User user = SecurityContext.get()
                .getCurrentUser().get();
        if (user == null) {
            return 0;
        }
        Long count = this.mapper.countAllDevicesForCustomer(user.getCustomerId());
        if (count == null) {
            return 0;
        }
        return count.intValue(); */
    }

    // UNSECURE, for sending stats (by superadmin in multi-tenant setup) only
    public long getOnlineDevicesCount() {
        return this.mapper.countOnlineDevices();
    }

    public long getTotalDevicesCount(User user,
                                     DeviceConfigFilesStatus fileStatus,
                                     DeviceApplicationsStatus appStatus,
                                     Long minEnrollTime,
                                     Long maxEnrollTime,
                                     Long minOnlineTime,
                                     Long maxOnlineTime) {
        if (user == null) {
            return 0;
        }
        DeviceSummaryRequest filter = new DeviceSummaryRequest(
                user.getId(),
                user.getCustomerId(),
                fileStatus,
                appStatus,
                minEnrollTime,
                maxEnrollTime,
                minOnlineTime,
                maxOnlineTime,
                null
        );
        Long count = this.mapper.countAllDevicesForSummary(filter);
        if (count == null) {
            return 0;
        }
        return count.intValue();
    }

    public List<Device> getAllDevices() {
        DeviceSearchRequest request = new DeviceSearchRequest();
        List<Device> devices = getListWithCurrentUser(currentUser -> {
            request.setCustomerId(currentUser.getCustomerId());
            request.setUserId(currentUser.getId());
            request.setPageSize(1000000);
            return this.mapper.getAllDevices(request);
        });
        return devices;
    }

    public PaginatedData<Device> getAllDevices(DeviceSearchRequest request) {
        List<Device> devices = getListWithCurrentUser(currentUser -> {
            request.setCustomerId(currentUser.getCustomerId());
            request.setUserId(currentUser.getId());
            return this.mapper.getAllDevices(request);
        });

        if (!this.deviceListHooks.isEmpty()) {
            for (DeviceListHook hook : this.deviceListHooks) {
                devices = hook.handle(devices);
            }
        }

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
                this.mapper::getDeviceById,
                device -> this.mapper.removeDevice(device.getId()),
                SecurityException::onDeviceAccessViolation
        );
    }

    public void updateDeviceConfiguration(Integer deviceId, Integer configurationId) {
        updateById(
                deviceId,
                this.mapper::getDeviceById,
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
            d.updateFastSearch(fastSearchChars);
            this.mapper.insertDevice(d);
            if (d.getGroups() != null && !d.getGroups().isEmpty()) {
                this.mapper.insertDeviceGroups(
                        d.getId(), d.getGroups().stream().map(LookupItem::getId).collect(Collectors.toList())
                );
            }
            this.eventService.fireEvent(new DeviceInfoUpdatedEvent(d.getId()));
        });
    }

    /**
     * <p>Updates the device data in persistent data store. The reference to related customer account and last update
     * time of the device are not affected by this method.</p>
     *
     * @param device a device to be updated.
     * @throws SecurityException if current user is not authorized to update this device.
     */
    @Transactional
    public void updateDevice(Device device) {
        updateById(device.getId(), this.mapper::getDeviceById, dbDevice -> {
            device.setCustomerId(dbDevice.getCustomerId());
            device.updateFastSearch(fastSearchChars);

            final Integer currentUserId = SecurityContext.get().getCurrentUser().get().getId();
            this.mapper.updateDevice(device);
            this.mapper.removeDeviceGroupsByDeviceId(currentUserId, device.getCustomerId(), device.getId());
            if (device.getGroups() != null && !device.getGroups().isEmpty()) {
                this.mapper.insertDeviceGroups(
                        device.getId(), device.getGroups().stream().map(LookupItem::getId).collect(Collectors.toList())
                );
            }
            this.eventService.fireEvent(new DeviceInfoUpdatedEvent(device.getId()));
        }, SecurityException::onDeviceAccessViolation);
    }

    public Device getDeviceById(Integer deviceId) {
        return getSingleRecord(() -> this.mapper.getDeviceById(deviceId), SecurityException::onDeviceAccessViolation);
    }

    /**
     * <p>Gets the applications installed on specified device.</p>
     *
     * @param deviceId an ID of a device.
     * @return a list of applications reported as installed on specified device.
     */
    @Transactional
    public List<DeviceApplication> getDeviceInstalledApplications(int deviceId) {
        final Device dbDevice = getDeviceById(deviceId);
        if (dbDevice != null) {
            return this.mapper.getDeviceInstalledApplications(dbDevice.getId());
        } else {
            return new ArrayList<>();
        }
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
        final Device dbDevice = getDeviceById(deviceId);
        if (dbDevice != null) {
            this.mapper.deleteDeviceApplicationSettings(dbDevice.getId());
            this.mapper.insertDeviceApplicationSettings(dbDevice.getId(), applicationSettings);
        }
    }

    /**
     * <p>Updates the description for the specified device.</p>
     *
     * @param deviceId an ID of a device to update description for.
     * @param newDeviceDescription a new device description.
     * @throws SecurityException if current user is not authorized to update the specified device description.
     */
    @Transactional
    public void updateDeviceDescription(Integer deviceId, String newDeviceDescription) {
        updateById(
                deviceId,
                this.mapper::getDeviceById,
                device -> this.mapper.updateDeviceDescription(deviceId, newDeviceDescription),
                SecurityException::onDeviceAccessViolation
        );
    }

    @Transactional
    public List<ChartItem> getStatusSummary() {
        return SecurityContext.get().getCurrentUser()
                .map(u -> {
                    DeviceSummaryRequest filter = new DeviceSummaryRequest(
                        u.getId(), u.getCustomerId(),
                        null, null, null, null, null, null, null
                    );
                    long now = System.currentTimeMillis();
                    List<ChartItem> result = new LinkedList<>();

                    ChartItem item1 = new ChartItem();
                    item1.setStringAttr("green");
                    filter.setMinOnlineTime(now - 3600*1000l);
                    item1.setNumber(this.mapper.countAllDevicesForSummary(filter));
                    result.add(item1);

                    ChartItem item2 = new ChartItem();
                    item2.setStringAttr("yellow");
                    filter.setMinOnlineTime(now - 3600*4000l);
                    filter.setMaxOnlineTime(now - 3600*1000l);
                    item2.setNumber(this.mapper.countAllDevicesForSummary(filter));
                    result.add(item2);

                    ChartItem item3 = new ChartItem();
                    item3.setStringAttr("red");
                    filter.setMinOnlineTime(0l);
                    filter.setMaxOnlineTime(now - 3600*1000l);
                    item3.setNumber(this.mapper.countAllDevicesForSummary(filter));
                    result.add(item3);

                    return result;
                })
                .orElse(null);
    }

    @Transactional
    public List<ChartItem> getInstallSummary() {
        return SecurityContext.get().getCurrentUser()
                .map(u -> {
                    DeviceSummaryRequest filter = new DeviceSummaryRequest(
                            u.getId(), u.getCustomerId(),
                            null, null, 1l, null, null, null, null
                    );
                    List<ChartItem> result = new LinkedList<>();

                    ChartItem item1 = new ChartItem();
                    item1.setStringAttr("SUCCESS");
                    filter.setAppStatus(DeviceApplicationsStatus.SUCCESS);
                    item1.setNumber(this.mapper.countAllDevicesForSummary(filter));
                    result.add(item1);

                    ChartItem item2 = new ChartItem();
                    item2.setStringAttr("VERSION_MISMATCH");
                    filter.setAppStatus(DeviceApplicationsStatus.VERSION_MISMATCH);
                    item2.setNumber(this.mapper.countAllDevicesForSummary(filter));
                    result.add(item2);

                    ChartItem item3 = new ChartItem();
                    item3.setStringAttr("FAILURE");
                    filter.setAppStatus(DeviceApplicationsStatus.FAILURE);
                    item3.setNumber(this.mapper.countAllDevicesForSummary(filter));
                    result.add(item3);

                    return result;
                })
                .orElse(null);
    }

    @Transactional
    public Long countEnrolled(long lastEnrollTime) {
        return SecurityContext.get().getCurrentUser()
                .map(u -> {
                    DeviceSummaryRequest filter = new DeviceSummaryRequest(
                            u.getId(), u.getCustomerId(),
                            null, null, lastEnrollTime, null, null, null, null
                    );
                    return this.mapper.countAllDevicesForSummary(filter);
                })
                .orElse(0l);
    }

    @Transactional
    public List<SummaryConfigItem> getSummaryByConfig(DeviceSummaryRequest condition,
                                                      List<SummaryConfigItem> topConfigs) {
        return SecurityContext.get().getCurrentUser()
                .map(u -> {
                    List<Integer> configIds = null;
                    if (topConfigs != null) {
                        configIds = new LinkedList<>();
                        for (SummaryConfigItem summaryConfigItem : topConfigs) {
                            configIds.add(summaryConfigItem.getId());
                        }
                        ;
                    }

                    DeviceSummaryRequest filter = new DeviceSummaryRequest(
                            u.getId(), u.getCustomerId(),
                            condition.getFileStatus(),
                            condition.getAppStatus(),
                            condition.getMinEnrollTime(),
                            condition.getMaxEnrollTime(),
                            condition.getMinOnlineTime(),
                            condition.getMaxOnlineTime(),
                            condition.getConfigIds()
                    );
                    List<SummaryConfigItem> result = mapper.countDevicesByConfig(filter);
                    if (topConfigs != null) {
                        // Sort result to correspond configIds
                        List<SummaryConfigItem> result1 = new LinkedList<>();
                        for (SummaryConfigItem baseItem : topConfigs) {
                            boolean zero = true;
                            for (SummaryConfigItem c : result) {
                                if (c.getId() == baseItem.getId()) {
                                    result1.add(c);
                                    zero = false;
                                    break;
                                }
                            }
                            if (zero) {
                                // Result not present, which means zero value
                                baseItem.setCounter(0);
                                result1.add(baseItem);
                            }
                        }
                        return result1;
                    }
                    return result;
                })
                .orElse(null);
    }

    public List<ChartItem> getDevicesEnrolledMonthly() {
        return SecurityContext.get().getCurrentUser()
                .map(u -> {
                    List<ChartItem> result = new LinkedList<>();
                    Calendar c = Calendar.getInstance();
                    int year = c.get(Calendar.YEAR) - 1;
                    int month = c.get(Calendar.MONTH) + 1;
                    if (month >= 12) {
                        month = 0;
                        year++;
                    }
                    c.set(year, month, 0, 0, 0);
                    long startTime = c.getTimeInMillis();
                    long endTime = 0;
                    for (int i = 0; i < 12; i++) {
                        String label = String.format("%02d/%02d", month + 1, year % 100);
                        month++;
                        if (month >= 12) {
                            month = 0;
                            year++;
                        }
                        c.set(year, month, 0, 0, 0);
                        endTime = c.getTimeInMillis();

                        DeviceSummaryRequest filter = new DeviceSummaryRequest(
                                u.getId(), u.getCustomerId(),
                                null, null,
                                startTime, endTime,
                                null, null, null
                        );

                        ChartItem item = new ChartItem();
                        item.setStringAttr(label);
                        item.setNumber(this.mapper.countAllDevicesForSummary(filter));
                        result.add(item);
                        startTime = endTime;
                    }
                    return result;
                })
                .orElse(null);

    }

}
