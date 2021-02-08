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

package com.hmdm.plugins.devicelog.persistence.postgres.dao;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.hmdm.persistence.AbstractDAO;
import com.hmdm.persistence.CustomerDAO;
import com.hmdm.persistence.UnsecureDAO;
import com.hmdm.persistence.domain.Customer;
import com.hmdm.persistence.domain.Device;
import com.hmdm.plugins.devicelog.model.DeviceLogPluginSettings;
import com.hmdm.plugins.devicelog.model.DeviceLogRecord;
import com.hmdm.plugins.devicelog.model.DeviceLogRule;
import com.hmdm.plugins.devicelog.model.LogLevel;
import com.hmdm.plugins.devicelog.persistence.DeviceLogDAO;
import com.hmdm.plugins.devicelog.persistence.DeviceLogPluginSettingsDAO;
import com.hmdm.plugins.devicelog.persistence.postgres.dao.domain.PostgresDeviceLogRecord;
import com.hmdm.plugins.devicelog.persistence.postgres.dao.mapper.PostgresDeviceLogMapper;
import com.hmdm.plugins.devicelog.rest.json.AppliedDeviceLogRule;
import com.hmdm.plugins.devicelog.rest.json.DeviceLogFilter;
import com.hmdm.plugins.devicelog.rest.json.UploadedDeviceLogRecord;
import com.hmdm.security.SecurityContext;
import org.mybatis.guice.transactional.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * <p>A DAO for device log records backed by the <code>Postgres</code> database.</p>
 *
 * @author isv
 */
@Singleton
public class PostgresDeviceLogDAO extends AbstractDAO<PostgresDeviceLogRecord> implements DeviceLogDAO {

    /**
     * <p>A logger to be used for logging the events.</p>
     */
    private static final Logger logger = LoggerFactory.getLogger(PostgresDeviceLogDAO.class);

    private final PostgresDeviceLogMapper deviceLogMapper;

    private final UnsecureDAO unsecureDAO;

    private DeviceLogPluginSettingsDAO deviceLogPluginSettingsDAO;

    /**
     * <p>Constructs new <code>PostgresDeviceLogDAO</code> instance. This implementation does nothing.</p>
     */
    @Inject
    public PostgresDeviceLogDAO(PostgresDeviceLogMapper deviceLogMapper,
                                DeviceLogPluginSettingsDAO deviceLogPluginSettingsDAO,
                                UnsecureDAO unsecureDAO) {
        this.deviceLogMapper = deviceLogMapper;
        this.unsecureDAO = unsecureDAO;
        this.deviceLogPluginSettingsDAO = deviceLogPluginSettingsDAO;
    }

    /**
     * <p>Finds the log records matching the specified filter.</p>
     *
     * @param filter a filter used to narrowing down the search results.
     * @return a list of log records matching the specified filter.
     */
    @Override
    @Transactional
    public List<DeviceLogRecord> findAll(DeviceLogFilter filter) {
        prepareFilter(filter);

        final List<PostgresDeviceLogRecord> result = this.getListWithCurrentUser(currentUser -> {
            filter.setCustomerId(currentUser.getCustomerId());
            filter.setUserId(currentUser.getId());
            return this.deviceLogMapper.findAllLogRecordsByCustomerId(filter);
        });

        return new ArrayList<>(result);
    }

    /**
     * <p>Counts the log records matching the specified filter.</p>
     *
     * @param filter a filter used to narrowing down the search results.
     * @return a number of log records matching the specified filter.
     */
    @Override
    public long countAll(DeviceLogFilter filter) {
        prepareFilter(filter);
        return SecurityContext.get().getCurrentUser()
                .map(user -> {
                    filter.setCustomerId(user.getCustomerId());
                    return this.deviceLogMapper.countAll(filter);
                })
                .orElse(0L);
    }

    /**
     * <p>Inserts the specified log records uploaded by the specified device into underlying persistent data store.</p>
     *
     * @param deviceNumber an identifier of a device.
     * @param ipAddress    an IP-address of a device.
     * @param logs         a list of log records to be inserted.
     * @return a number of log records inserted into underlying persistent store.
     */
    @Override
    public int insertDeviceLogRecords(String deviceNumber, String ipAddress, List<UploadedDeviceLogRecord> logs) {
        final Device dbDevice = this.unsecureDAO.getDeviceByNumber(deviceNumber);
        if (dbDevice != null) {
            // Build the cache of applications
            final Set<String> appPackages
                    = logs.stream().map(UploadedDeviceLogRecord::getPackageId).collect(Collectors.toSet());
            final Map<String, Integer> appCache
                    = this.unsecureDAO.buildPackageIdMapping(dbDevice.getCustomerId(), appPackages);
            
            final List<PostgresDeviceLogRecord> postgresLogs = logs.stream().map(log -> {
                try {
                    PostgresDeviceLogRecord postgresRecord = new PostgresDeviceLogRecord();
                    postgresRecord.setCustomerId(dbDevice.getCustomerId());
                    postgresRecord.setApplicationId(appCache.get(log.getPackageId()));
                    postgresRecord.setCreateTime(log.getTimestamp());
                    postgresRecord.setDeviceId(dbDevice.getId());
                    postgresRecord.setMessage(log.getMessage());
                    postgresRecord.setSeverity(LogLevel.byId(log.getLogLevel()).orElse(LogLevel.NONE));
                    postgresRecord.setIpAddress(ipAddress);

                    return postgresRecord;
                } catch (Exception e) {
                    logger.error("Unexpected error when converting log record {}. This record will be skipped. ", log, e);
                    return null;
                }
            }).filter(Objects::nonNull).collect(Collectors.toList());

            if (!postgresLogs.isEmpty()) {
                int insertCount = this.deviceLogMapper.insertDeviceLogRecords(postgresLogs);

                return insertCount;
            }
        }

        return 0;
    }

    /**
     * <p>Gets the list of log rules applicable to specified device.</p>
     *
     * @param deviceNumber an identifier of device.
     * @return a list of applicable log rules for device.
     */
    @Override
    public List<AppliedDeviceLogRule> getDeviceLogRules(String deviceNumber) {
        final Device dbDevice = this.unsecureDAO.getDeviceByNumber(deviceNumber);
        if (dbDevice != null) {
            final DeviceLogPluginSettings deviceLogSettings
                    = this.deviceLogPluginSettingsDAO.getPluginSettings(dbDevice.getCustomerId());
            if (deviceLogSettings != null) {
                List<DeviceLogRule> rules = deviceLogSettings.getRules();
                if (rules != null && !rules.isEmpty()) {
                    rules = rules.stream()
                            .filter(DeviceLogRule::isActive)
                            .filter(r -> r.getSeverity() != LogLevel.NONE)
                            .collect(Collectors.toList());

                    final List<DeviceLogRule> defaultRules = rules.stream()
                            .filter(r -> r.getConfigurationId() == null)
                            .filter(r -> r.getGroupId() == null)
                            .filter(r -> r.getDevices() == null || r.getDevices().isEmpty())
                            .collect(Collectors.toList());

                    List<DeviceLogRule> resultingRules = defaultRules;

                    if (dbDevice.getConfigurationId() != null) {
                        final List<DeviceLogRule> configurationRules = rules.stream()
                                .filter(r -> r.getConfigurationId() != null)
                                .filter(r -> r.getConfigurationId().equals(dbDevice.getConfigurationId()))
                                .collect(Collectors.toList());

                        resultingRules = combineDeviceLogRules(resultingRules, configurationRules);
                    }

                    if (dbDevice.getGroups() != null && !dbDevice.getGroups().isEmpty()) {
                        final List<DeviceLogRule> groupRules = rules.stream()
                                .filter(r -> r.getGroupId() != null)
                                .filter(r -> dbDevice.getGroups().stream().anyMatch(g -> g.getId() == r.getGroupId()))
                                .collect(Collectors.toList());

                        resultingRules = combineDeviceLogRules(resultingRules, groupRules);
                    }

                    final List<DeviceLogRule> deviceRules = rules.stream()
                            .filter(r -> r.getDevices() != null)
                            .filter(r -> !r.getDevices().isEmpty())
                            .filter(r -> r.getDevices().stream().anyMatch(d -> d.getId() == dbDevice.getId()))
                            .collect(Collectors.toList());

                    resultingRules = combineDeviceLogRules(resultingRules, deviceRules);


                    final List<AppliedDeviceLogRule> result
                            = resultingRules.stream().map(AppliedDeviceLogRule::new).collect(Collectors.toList());

                    return result;
                }
            }
        }

        return new ArrayList<>();
    }

    /**
     * <p>Deletes the log records which are older than number of days configured in customer's profile.</p>
     */
    public void purgeLogRecords() {
        try {
            logger.info("Deleting outdated records from the device logs...");

            List<Customer> customers = unsecureDAO.getAllCustomersUnsecure();
            for (Customer c : customers) {
                final int count = this.deviceLogMapper.purgeLogRecords(c.getId());
                if (count > 0) {
                    logger.info("Deleted {} records from the device logs for customer {}", count, c.getId());
                }
            }

        } catch (Exception e) {
            logger.error("Unexpected error when purging the device log records", e);
        }
    }


    /**
     * <p>Prepares the filter for usage by mapper.</p>
     *
     * @param filter a filter provided by request.
     */
    private static void prepareFilter(DeviceLogFilter filter) {
        if (filter.getDeviceFilter() != null) {
            if (filter.getDeviceFilter().trim().isEmpty()) {
                filter.setDeviceFilter(null);
            } else {
                filter.setDeviceFilter('%' + filter.getDeviceFilter().trim() + '%');
            }
        }
        if (filter.getMessageFilter() != null) {
            if (filter.getMessageFilter().trim().isEmpty()) {
                filter.setMessageFilter(null);
            } else {
                filter.setMessageFilter('%' + filter.getMessageFilter().trim() + '%');
            }
        }
        if (filter.getApplicationFilter() != null) {
            if (filter.getApplicationFilter().trim().isEmpty()) {
                filter.setApplicationFilter(null);
            } else {
                filter.setApplicationFilter('%' + filter.getApplicationFilter().trim() + '%');
            }
        }
    }

    /**
     * <p>Combines the specified list of device log rules into a single list.</p>
     *
     * @param lessPreferred a list of less preferred rules.
     * @param morePreferred a list of more preferred rules.
     * @return a resulting list of rules.
     */
    private static List<DeviceLogRule> combineDeviceLogRules(List<DeviceLogRule> lessPreferred, List<DeviceLogRule> morePreferred) {
        final Map<String, DeviceLogRule> moreMapping
                = morePreferred.stream().collect(Collectors.toMap(DeviceLogRule::getApplicationPkg, r -> r, (r1, r2) -> r1));

        List<DeviceLogRule> result = new ArrayList<>();

        lessPreferred.stream()
                .filter(less -> !moreMapping.containsKey(less.getApplicationPkg()))
                .forEach(result::add);

        result.addAll(morePreferred);

        return result;
    }
}
