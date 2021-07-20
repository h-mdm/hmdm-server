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

package com.hmdm.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.hmdm.persistence.domain.*;
import com.hmdm.persistence.mapper.ConfigurationFileMapper;
import com.hmdm.persistence.mapper.ConfigurationMapper;
import com.hmdm.persistence.mapper.DeviceMapper;
import com.hmdm.rest.json.DeviceConfigurationFile;
import com.hmdm.rest.json.DeviceInfo;
import com.hmdm.util.ApplicationUtil;
import org.mybatis.guice.transactional.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.BiPredicate;


/**
 * <p>$</p>
 */
@Singleton
public class DeviceStatusService {

    private static final Logger logger = LoggerFactory.getLogger(DeviceStatusService.class);

    private final DeviceMapper deviceMapper;
    private final ConfigurationMapper configurationMapper;
    private final ConfigurationFileMapper configurationFileMapper;

    /**
     * <p>Constructs new <code>DeviceStatusService</code> instance. This implementation does nothing.</p>
     */
    @Inject
    public DeviceStatusService(DeviceMapper deviceMapper,
                               ConfigurationMapper configurationMapper,
                               ConfigurationFileMapper configurationFileMapper) {
        this.deviceMapper = deviceMapper;
        this.configurationMapper = configurationMapper;
        this.configurationFileMapper = configurationFileMapper;
    }

    @Transactional
    public void recalcDeviceStatuses(int deviceId) {
        final Device dbDevice = this.deviceMapper.getDeviceById(deviceId);
        if (dbDevice != null) {
            try {
                DeviceConfigFilesStatus deviceConfigFilesStatus = DeviceConfigFilesStatus.OTHER;
                DeviceApplicationsStatus deviceApplicatiosStatus = DeviceApplicationsStatus.FAILURE;

                if (dbDevice.getInfo() != null) {
                    if (!dbDevice.getInfo().trim().isEmpty()) {
                        final String deviceInfoString = dbDevice.getInfo();
                        ObjectMapper jsonMapper = new ObjectMapper();
                        DeviceInfo info = jsonMapper.readValue(deviceInfoString, DeviceInfo.class);

                        deviceConfigFilesStatus = evaluateDeviceConfigurationFilesStatus(dbDevice, info);
                        deviceApplicatiosStatus = evaluateDeviceApplicationsStatus(dbDevice, info);
                    }
                }

                this.deviceMapper.updateDeviceStatuses(dbDevice.getId(), deviceConfigFilesStatus, deviceApplicatiosStatus);
                
            } catch (IOException e) {
                logger.error("Failed to parse JSON data from info property", e);
            }

        }
    }

    private DeviceApplicationsStatus evaluateDeviceApplicationsStatus(Device dbDevice, DeviceInfo info) {
        AtomicInteger versionMismatchCount = new AtomicInteger();
        AtomicInteger notRemovedCount = new AtomicInteger();
        AtomicInteger notInstalledCount = new AtomicInteger();

        final List<Application> configApplications = this.configurationMapper.getPlainConfigurationSoleApplications(dbDevice.getConfigurationId());

        configApplications.forEach(configApp -> {
            // Do not test apps without URL (they are mostly system apps) as well as web pages
            if ((configApp.getUrl() == null && configApp.getUrlArm64() == null && configApp.getUrlArmeabi() == null)
                    || configApp.getType() != ApplicationType.app) {
                return;
            }

            final List<Application> deviceApps = info.getApplications();
            boolean foundOnDevice = false;
            for (int i = 0; i < deviceApps.size(); i++) {
                final Application deviceApp = deviceApps.get(i);
                if (deviceApp.getPkg().equals(configApp.getPkg())) {
                    foundOnDevice = true;

                    if (configApp.getAction() == 2) {
                        if (configApp.getVersion().equals(deviceApp.getVersion())) {
                            // Needs to be removed but not removed
                            notRemovedCount.incrementAndGet();
                        }
                    } else if (!configApp.getVersion().equals("0")
                            && !configApp.isSkipVersion()
                            && !isVersionUpToDate.test(deviceApp.getVersion(), configApp.getVersion())) {
                        // Version mismatch
                        versionMismatchCount.incrementAndGet();
                    }
                    break;
                }
            }

            if (!foundOnDevice && configApp.getAction() == 1) {
                notInstalledCount.incrementAndGet();
            }
        });

        if (notInstalledCount.get() > 0) {
            return DeviceApplicationsStatus.FAILURE;
        } else if (versionMismatchCount.get() > 0 || notRemovedCount.get() > 0) {
            return DeviceApplicationsStatus.VERSION_MISMATCH;
        } else {
            return DeviceApplicationsStatus.SUCCESS;
        }
    }

    private DeviceConfigFilesStatus evaluateDeviceConfigurationFilesStatus(Device dbDevice, DeviceInfo info) {
        final List<ConfigurationFile> configurationFiles
                = this.configurationFileMapper.getConfigurationFiles(dbDevice.getConfigurationId());

        AtomicInteger correctCount = new AtomicInteger();
        AtomicInteger notInstalledCount = new AtomicInteger();
        AtomicInteger lastUpdateMismatchCount = new AtomicInteger();

        configurationFiles.forEach(configFile -> {
            final List<DeviceConfigurationFile> deviceFiles = info.getFiles();
            boolean foundOnDevice = false;
            boolean skip = false;
            for (int i = 0; i < deviceFiles.size(); i++) {
                final DeviceConfigurationFile deviceFile = deviceFiles.get(i);
                if (deviceFile.getPath().equals(configFile.getDevicePath())) {
                    foundOnDevice = true;
                    if (!configFile.getLastUpdate().equals(deviceFile.getLastUpdate())
                            && Math.abs(configFile.getLastUpdate() - deviceFile.getLastUpdate()) > 1 * 60 * 60 * 1000) {
                        lastUpdateMismatchCount.incrementAndGet();
                        skip = true;
                    }
                    break;
                }
            }

            if (!foundOnDevice && !configFile.isRemove()) {
                notInstalledCount.incrementAndGet();
            } else if (foundOnDevice && !skip) {
                correctCount.incrementAndGet();
            }
        });

        if (correctCount.get() == configurationFiles.size()) {
            return DeviceConfigFilesStatus.UP_TO_DATE;
        } else if (notInstalledCount.get() > 0) {
            return DeviceConfigFilesStatus.MISSING;
        } else {
            return DeviceConfigFilesStatus.OTHER;
        }
    }


    /**
     * <p>Checks if specified application versions are equal. Removes all non-digit characters from version numbers when
     * analyzing.</p>
     */
    BiPredicate<String, String> areVersionsEqual = (v1, v2) -> {
        String v1d = ApplicationUtil.normalizeVersion(v1);
        String v2d = ApplicationUtil.normalizeVersion(v2);
        return v1d.equals(v2d);

    };

    /**
     * <p>Checks if the installed version is up to date. </p>
     */
    BiPredicate<String, String> isVersionUpToDate = (installed, required) -> {
        return compareVersions(installed, required) >= 0;
    };

    // Returns -1 if v1 < v2, 0 if v1 == v2 and 1 if v1 > v2
    public static int compareVersions(String v1, String v2) {
        // Versions are numbers separated by a dot
        String v1d = v1.replaceAll("[^\\d.]", "");
        String v2d = v2.replaceAll("[^\\d.]", "");

        String[] v1n = v1d.split("\\.");
        String[] v2n = v2d.split("\\.");

        // One version could contain more digits than another
        int count = v1n.length < v2n.length ? v1n.length : v2n.length;

        for (int n = 0; n < count; n++) {
            try {
                int n1 = Integer.parseInt(v1n[n]);
                int n2 = Integer.parseInt(v2n[n]);
                if (n1 < n2) {
                    return -1;
                } else if (n1 > n2) {
                    return 1;
                }
                // If major version numbers are equals, continue to compare minor version numbers
            } catch (Exception e) {
                return 0;
            }
        }

        // Here we are if common parts are equal
        // Now we decide that if a version has more parts, it is considered as greater
        if (v1n.length < v2n.length) {
            return -1;
        } else if (v1n.length > v2n.length) {
            return 1;
        }
        return 0;
    }

}
