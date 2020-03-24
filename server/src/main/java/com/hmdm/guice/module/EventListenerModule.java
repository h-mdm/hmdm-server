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

package com.hmdm.guice.module;

import com.google.inject.Inject;
import com.hmdm.event.EventService;
import com.hmdm.persistence.ConfigurationUpdatedEventListener;
import com.hmdm.persistence.DeviceInfoUpdatedEventListener;
import com.hmdm.persistence.mapper.DeviceMapper;
import com.hmdm.service.DeviceStatusService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * <p>$</p>
 */
public class EventListenerModule {

    private final EventService eventService;
    private final DeviceMapper deviceMapper;
    private final DeviceStatusService deviceStatusService;

    private final ExecutorService executorService = Executors.newFixedThreadPool(1);

    private static final Logger logger = LoggerFactory.getLogger(EventListenerModule.class);


    /**
     * <p>Constructs new <code>EventListenerModule</code> instance. This implementation does nothing.</p>
     */
    @Inject
    public EventListenerModule(EventService eventService, DeviceMapper deviceMapper, DeviceStatusService deviceStatusService) {
        this.eventService = eventService;
        this.deviceMapper = deviceMapper;
        this.deviceStatusService = deviceStatusService;
    }

    public void init() {
        this.eventService.addEventListener(new DeviceInfoUpdatedEventListener(deviceStatusService));
        this.eventService.addEventListener(new ConfigurationUpdatedEventListener(deviceMapper, deviceStatusService));

        executorService.submit(() -> {
            List<Integer> deviceIds = this.deviceMapper.getAllDeviceIds();
            deviceIds.forEach(deviceId -> {
                try {
                    this.deviceStatusService.recalcDeviceStatuses(deviceId);
                } catch (Exception e) {
                    logger.warn("Failed to recalculate statuses for device: {}", deviceId, e);
                }
            });
        });

        Runtime.getRuntime().addShutdownHook(new Thread(executorService::shutdown));
    }

}
