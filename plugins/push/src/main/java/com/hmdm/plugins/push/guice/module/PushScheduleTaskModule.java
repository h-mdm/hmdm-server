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

package com.hmdm.plugins.push.guice.module;

import com.google.inject.Inject;
import com.hmdm.notification.PushService;
import com.hmdm.notification.persistence.domain.PushMessage;
import com.hmdm.persistence.DeviceDAO;
import com.hmdm.persistence.UnsecureDAO;
import com.hmdm.persistence.domain.Device;
import com.hmdm.persistence.domain.DeviceSearchRequest;
import com.hmdm.plugin.PluginTaskModule;
import com.hmdm.plugins.push.persistence.PushDAO;
import com.hmdm.plugins.push.persistence.PushScheduleDAO;
import com.hmdm.plugins.push.persistence.domain.PluginPushMessage;
import com.hmdm.plugins.push.persistence.domain.PluginPushSchedule;
import com.hmdm.rest.json.Response;
import com.hmdm.security.SecurityContext;
import com.hmdm.util.BackgroundTaskRunnerService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * <p>A module used for initializing the tasks to be executed in background.</p>
 *
 * @author isv
 */
public class PushScheduleTaskModule implements PluginTaskModule {

    private static final Logger logger = LoggerFactory.getLogger(PushScheduleTaskModule.class);

    /**
     * <p>An interface to push message records persistence.</p>
     */
    private PushDAO pushDAO;

    /**
     * <p>An interface to persistence layer.</p>
     */
    private final UnsecureDAO unsecureDAO;

    /**
     * <p>An interface to persistence layer.</p>
     */
    private final PushScheduleDAO pushScheduleDAO;

    /**
     * <p>An interface to notification services.</p>
     */
    private PushService pushService;

    /**
     * <p>A runner for the repeatable tasks.</p>
     */
    private final BackgroundTaskRunnerService taskRunner;

    /**
     * <p>Constructs new <code>DeviceInfoTaskModule</code> instance. This implementation does nothing.</p>
     */
    @Inject
    public PushScheduleTaskModule(
            PushDAO pushDAO,
            UnsecureDAO unsecureDAO,
            PushScheduleDAO pushScheduleDAO,
            PushService pushService,
            BackgroundTaskRunnerService taskRunner) {
        this.pushDAO = pushDAO;
        this.unsecureDAO = unsecureDAO;
        this.pushScheduleDAO = pushScheduleDAO;
        this.pushService = pushService;
        this.taskRunner = taskRunner;
    }

    /**
     * <p>Initializes this module. Schedules the task for sending scheduled messages.</p>
     */
    @Override
    public void init() {
        taskRunner.submitRepeatableTask(this::sendScheduledMessages, 1, 1, TimeUnit.MINUTES);
    }


    /**
     * <p>Retrieves scheduled messages from the database and sends them.</p>
     */
    public void sendScheduledMessages() {
        try {
            List<PluginPushSchedule> taskList = pushScheduleDAO.findMatchingTime();
            taskList.forEach(task -> sendScheduledMessage(task));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void sendScheduledMessage(PluginPushSchedule task) {
        List<PluginPushMessage> messages = new LinkedList<>();
        List<Device> devices = new LinkedList<>();
        logger.info("Processing scheduled message: type " + task.getMessageType() +
                ", customer " + task.getCustomerId() +
                ", scope " + task.getScope() + ", device " + task.getDeviceId() +
                ", group " + task.getGroupId() + ", config " + task.getConfigurationId());

        if (task.getScope().equals("device")) {
            PluginPushMessage message = new PluginPushMessage();
            message.setDeviceId(task.getDeviceId());
            messages.add(message);
        } else if (task.getScope().equals("group")) {
            devices = unsecureDAO.getAllGroupDevices(task.getGroupId(), task.getCustomerId());
        } else if (task.getScope().equals("configuration")) {
            devices = unsecureDAO.getAllConfigurationDevices(task.getConfigurationId(), task.getCustomerId());
        } else {
            devices = unsecureDAO.getAllCustomerDevices(task.getCustomerId());
        }

        for (Device device : devices) {
            PluginPushMessage message = new PluginPushMessage();
            message.setDeviceId(device.getId());
            messages.add(message);
        }

        for (PluginPushMessage message : messages) {
            message.setCustomerId(task.getCustomerId());
            message.setMessageType(task.getMessageType());
            if (task.getPayload() != null && !task.getPayload().trim().equals("")) {
                message.setPayload(task.getPayload());
            }
            message.setTs(System.currentTimeMillis());
            sendSingleMessage(message);
        }

    }

    private boolean sendSingleMessage(PluginPushMessage message) {
        logger.info("Sending Push message " + message.getMessageType() + " to device " + message.getDeviceId());
        try {
            this.pushDAO.insertRawMessage(message);

            PushMessage pushMessage = new PushMessage();
            pushMessage.setDeviceId(message.getDeviceId());
            pushMessage.setMessageType(message.getMessageType());
            pushMessage.setPayload(message.getPayload());

            this.pushService.send(pushMessage);

            return true;

        } catch (Exception e) {
            e.printStackTrace();
            logger.error("Unexpected error when sending a Push message to " + message.getDeviceId(), e);
            return false;
        }
    }
}
