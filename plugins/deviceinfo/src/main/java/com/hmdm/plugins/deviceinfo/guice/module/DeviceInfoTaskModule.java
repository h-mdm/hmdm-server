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

package com.hmdm.plugins.deviceinfo.guice.module;

import com.google.inject.Inject;
import com.hmdm.event.EventService;
import com.hmdm.plugin.PluginTaskModule;
import com.hmdm.plugins.deviceinfo.persistence.CustomerCreatedEventListener;
import com.hmdm.plugins.deviceinfo.persistence.DeviceInfoDAO;
import com.hmdm.plugins.deviceinfo.persistence.DeviceInfoSettingsDAO;
import com.hmdm.util.BackgroundTaskRunnerService;

import java.util.concurrent.TimeUnit;

/**
 * <p>A module used for initializing the tasks to be executed in background.</p>
 *
 * @author isv
 */
public class DeviceInfoTaskModule implements PluginTaskModule {
    /**
     * <p>An interface to application's events.</p>
     */
    private final EventService eventService;

    /**
     * <p>An interface to persistence layer.</p>
     */
    private final DeviceInfoDAO deviceInfoDAO;

    private final DeviceInfoSettingsDAO settingsDAO;
    /**
     * <p>A runner for the repeatable tasks.</p>
     */
    private final BackgroundTaskRunnerService taskRunner;

    /**
     * <p>Constructs new <code>DeviceInfoTaskModule</code> instance. This implementation does nothing.</p>
     */
    @Inject
    public DeviceInfoTaskModule(EventService eventService,
                                DeviceInfoDAO deviceInfoDAO,
                                DeviceInfoSettingsDAO settingsDAO,
                                BackgroundTaskRunnerService taskRunner) {
        this.eventService = eventService;
        this.deviceInfoDAO = deviceInfoDAO;
        this.settingsDAO = settingsDAO;
        this.taskRunner = taskRunner;
    }

    /**
     * <p>Initializes this module. Schedules the task for purging the outdated device info records from DB on a daily
     * basis.</p>
     */
    @Override
    public void init() {
        taskRunner.submitRepeatableTask(deviceInfoDAO::purgeDeviceInfoRecords, 1, 24, TimeUnit.HOURS);

        this.eventService.addEventListener(new CustomerCreatedEventListener(this.settingsDAO));
    }

}
