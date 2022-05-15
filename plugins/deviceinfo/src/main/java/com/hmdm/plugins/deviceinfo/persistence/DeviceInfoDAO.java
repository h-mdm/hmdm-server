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

package com.hmdm.plugins.deviceinfo.persistence;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.hmdm.persistence.ApplicationDAO;
import com.hmdm.persistence.ConfigurationDAO;
import com.hmdm.persistence.DeviceDAO;
import com.hmdm.persistence.UnsecureDAO;
import com.hmdm.persistence.domain.*;
import com.hmdm.plugins.deviceinfo.persistence.domain.DeviceDynamicInfo;
import com.hmdm.plugins.deviceinfo.persistence.mapper.DeviceInfoMapper;
import com.hmdm.plugins.deviceinfo.rest.json.DeviceDynamicInfoRecord;
import com.hmdm.plugins.deviceinfo.rest.json.DeviceInfo;
import com.hmdm.plugins.deviceinfo.rest.json.DeviceInfoApplication;
import com.hmdm.plugins.deviceinfo.rest.json.DynamicInfoFilter;
import com.hmdm.rest.json.LookupItem;
import org.mybatis.guice.transactional.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.TreeSet;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

/**
 * <p>A DAO for {@link DeviceDynamicInfo} domain objects.</p>
 *
 * @author isv
 */
@Singleton
public class DeviceInfoDAO {

    private static final Logger logger = LoggerFactory.getLogger(DeviceInfoDAO.class);

    /**
     * <p>An interface to persistence layer.</p>
     */
    private final DeviceInfoMapper deviceInfoMapper;

    /**
     * <p>An interface to application data persistence layer.</p>
     */
    private final ApplicationDAO applicationDAO;

    /**
     * <p>An interface to configuration data persistence layer.</p>
     */
    private final ConfigurationDAO configurationDAO;

    /**
     * <p>An interface to devices data persistence layer.</p>
     */
    private final DeviceDAO deviceDAO;

    private final UnsecureDAO unsecureDAO;

    /**
     * <p>Constructs new <code>DeviceInfoDAO</code> instance. This implementation does nothing.</p>
     */
    @Inject
    public DeviceInfoDAO(DeviceInfoMapper deviceInfoMapper,
                         ApplicationDAO applicationDAO,
                         ConfigurationDAO configurationDAO,
                         DeviceDAO deviceDAO,
                         UnsecureDAO unsecureDAO) {
        this.deviceInfoMapper = deviceInfoMapper;
        this.applicationDAO = applicationDAO;
        this.configurationDAO = configurationDAO;
        this.deviceDAO = deviceDAO;
        this.unsecureDAO = unsecureDAO;
    }

    /**
     * <p>Saves the specified list of device info records.</p>
     *
     * @param data a list of records to be saved.
     */
    @Transactional
    public void saveDeviceDynamicData(List<DeviceDynamicInfo> data) {
        AtomicInteger countMain = new AtomicInteger(0);
        AtomicInteger countDevice = new AtomicInteger(0);
        AtomicInteger countWifi = new AtomicInteger(0);
        AtomicInteger countGps = new AtomicInteger(0);
        AtomicInteger countMobile1 = new AtomicInteger(0);
        AtomicInteger countMobile2 = new AtomicInteger(0);

        data.forEach(record -> {
            countMain.addAndGet(
                    this.deviceInfoMapper.insertDeviceInfoMain(record)
            );
            if (record.getDevice() != null) {
                countDevice.addAndGet(
                    this.deviceInfoMapper.insertDeviceInfoGroupDevice(record.getId(), record.getDevice())
                );
            }
            if (record.getWifi() != null) {
                countWifi.addAndGet(
                    this.deviceInfoMapper.insertDeviceInfoGroupWifi(record.getId(), record.getWifi())
                );
            }
            if (record.getGps() != null) {
                countGps.addAndGet(
                    this.deviceInfoMapper.insertDeviceInfoGroupGps(record.getId(), record.getGps())
                );
            }
            if (record.getMobile() != null) {
                countMobile1.addAndGet(
                        this.deviceInfoMapper.insertDeviceInfoGroupMobile(record.getId(), record.getMobile())
                );
            }
            if (record.getMobile2() != null) {
                countMobile2.addAndGet(
                        this.deviceInfoMapper.insertDeviceInfoGroupMobile2(record.getId(), record.getMobile2())
                );
            }
        });

        logger.debug("Number of records inserted: main {}, device group: {}, wi-fi group: {}, gps group: {}, " +
                        "mobile data group 1: {}, mobile data group 2: {} ",
                countMain.get(),
                countDevice.get(),
                countWifi.get(),
                countGps.get(),
                countMobile1.get(),
                countMobile2.get()
        );
    }

    /**
     * <p>Deletes the device info records which are older than number of days configured in customer's profile.</p>
     */
    public void purgeDeviceInfoRecords() {
        try {
            logger.info("Deleting outdated device parameter records...");

            List<Customer> customers = unsecureDAO.getAllCustomersUnsecure();
            for (Customer c : customers) {
                final int count = this.deviceInfoMapper.purgeDeviceInfoRecords(c.getId());
                if (count > 0) {
                    logger.info("Deleted {} records from the device info for customer {}", count, c.getId());
                }
            }

        } catch (Exception e) {
            logger.error("Unexpected error when purging the device info records", e);
        }
    }

    /**
     * <p>Gets the current detailed info for the specified device.</p>
     *
     * @param deviceId an ID of a device to get the detailed info for.
     * @return a most recent detailed info for device.
     */
    @Transactional
    public DeviceInfo getDeviceInfo(int deviceId) {
        DeviceInfo deviceInfo = this.deviceInfoMapper.getDetailedDeviceInfo(deviceId);
        if (deviceInfo != null) {
            final List<LookupItem> deviceGroups = this.deviceInfoMapper.getDeviceGroups(deviceId);
            deviceInfo.setGroups(deviceGroups);

            DeviceDynamicInfoRecord dynamicInfo = this.deviceInfoMapper.getLatestDeviceDynamicInfo(deviceId);
            deviceInfo.setLatestDynamicData(dynamicInfo);

            final Device dbDevice = this.deviceDAO.getDeviceById(deviceId);
            final Integer deviceConfigurationId = dbDevice.getConfigurationId();

            final Map<String, Application> configurationApps = this.configurationDAO
                    .getPlainConfigurationApplications(deviceConfigurationId)
                    .stream()
                    .filter(app -> app.getAction() == 1)
                    .collect(Collectors.toMap(Application::getPkg, app -> app, (app1, app2) -> app1));

            final Map<String, DeviceApplication> deviceApps = this.deviceDAO.getDeviceInstalledApplications(deviceId)
                    .stream()
                    .collect(Collectors.toMap(DeviceApplication::getPkg, app -> app, (app1, app2) -> app1));

            Set<String> allAppPackages = new TreeSet<>();
            allAppPackages.addAll(configurationApps.keySet());
            allAppPackages.addAll(deviceApps.keySet());

            List<DeviceInfoApplication> deviceInfoApps = new ArrayList<>();
            allAppPackages.forEach(pkg -> {
                Optional<Application> configApp = Optional.ofNullable(configurationApps.get(pkg));
                Optional<DeviceApplication> deviceAppVersion = Optional.ofNullable(deviceApps.get(pkg));

                DeviceInfoApplication app = new DeviceInfoApplication();
                app.setApplicationName(
                        configApp.map(Application::getName)
                                .orElse(deviceAppVersion.map(DeviceApplication::getName)
                                        .orElse(""))
                );
                app.setApplicationPkg(pkg);
                app.setVersionInstalled(deviceAppVersion.map(DeviceApplication::getVersion).orElse(null));
                app.setVersionRequired(configApp.map(Application::getVersion).orElse(null));

                // Not installed system apps as well as web apps are not displayed
                if (ApplicationType.app.equals(configApp.map(Application::getType).orElse(null)) &&
                        (true != configApp.map(Application::isSystem).orElse(false) ||
                         app.getVersionInstalled() != null)) {
                    deviceInfoApps.add(app);
                }
            });
            
            deviceInfoApps.sort(Comparator.comparing(DeviceInfoApplication::getApplicationName));

            deviceInfo.setApplications(deviceInfoApps);

        }
        return deviceInfo;
    }

    public List<DeviceDynamicInfoRecord> searchDynamicData(DynamicInfoFilter filter) {
        return this.deviceInfoMapper.searchDynamicData(filter);
    }

    public long countAllDynamicData(DynamicInfoFilter filter) {
        return this.deviceInfoMapper.countAllDynamicData(filter);
    }
}
