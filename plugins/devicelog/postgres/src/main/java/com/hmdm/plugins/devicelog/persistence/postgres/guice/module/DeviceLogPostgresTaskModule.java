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

package com.hmdm.plugins.devicelog.persistence.postgres.guice.module;

import com.google.inject.Inject;
import com.hmdm.plugin.PluginTaskModule;
import com.hmdm.plugins.devicelog.persistence.postgres.dao.PostgresDeviceLogDAO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * <p>$</p>
 *
 * @author isv
 */
public class DeviceLogPostgresTaskModule implements PluginTaskModule {

    private final ScheduledExecutorService logPurgeService = Executors.newScheduledThreadPool(1);

    private final PostgresDeviceLogDAO deviceLogDAO;

    /**
     * <p>Constructs new <code>DeviceLogPostgresTaskModule</code> instance. This implementation does nothing.</p>
     */
    @Inject
    public DeviceLogPostgresTaskModule(PostgresDeviceLogDAO deviceLogDAO) {
        this.deviceLogDAO = deviceLogDAO;
    }

    /**
     * <p>Initializes this module. The implementations are expected to initialize and setup any necessary services.</p>
     */
    @Override
    public void init() {
        logPurgeService.scheduleWithFixedDelay(new LogPurgeWorker(deviceLogDAO),
                0, 1, TimeUnit.DAYS);

        Runtime.getRuntime().addShutdownHook(new Thread(logPurgeService::shutdown));

    }

    /**
     * <p>A task to delete the device log records with lifespans longer than pre-defined limits.</p>
     */
    public static class LogPurgeWorker implements Runnable {
        private final static Logger log = LoggerFactory.getLogger(LogPurgeWorker.class);
        private final PostgresDeviceLogDAO deviceLogDAO;

        public LogPurgeWorker(PostgresDeviceLogDAO deviceLogDAO) {
            this.deviceLogDAO = deviceLogDAO;
        }

        @Override
        public void run() {
            log.info("Starting the iteration ...");
            try {
                this.deviceLogDAO.purgeLogRecords();
                log.info("Finished the iteration.");
            } catch (Exception e) {
                log.error("Unexpected error when purging the device log records", e);
            }
        }
    }
}
