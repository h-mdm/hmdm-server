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

import com.hmdm.event.CustomerCreatedEvent;
import com.hmdm.event.DeviceLocationUpdatedEvent;
import com.hmdm.event.EventListener;
import com.hmdm.event.EventType;
import com.hmdm.persistence.ApplicationDAO;
import com.hmdm.persistence.CustomerDAO;
import com.hmdm.persistence.UnsecureDAO;
import com.hmdm.persistence.domain.Application;
import com.hmdm.persistence.domain.Customer;
import com.hmdm.plugins.deviceinfo.persistence.domain.DeviceInfoPluginSettings;
import com.hmdm.rest.json.DeviceLocation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.LinkedList;
import java.util.List;

/**
 * <p>A listener for the events of {@link CustomerCreatedEvent} type. This listener is responsible for saving the
 * new customer settings to DB.</p>
 *
 * @author seva
 */
public class CustomerCreatedEventListener implements EventListener<CustomerCreatedEvent> {

    /**
     * <p>An interface to the persistence layer.</p>
     */
    private final DeviceInfoSettingsDAO settingsDAO;

    /**
     * <p>Constructs new <code>CustomerCreatedEventListener</code> instance. This implementation does nothing.</p>
     */
    public CustomerCreatedEventListener(DeviceInfoSettingsDAO settingsDAO) {
        this.settingsDAO = settingsDAO;
    }

    /**
     * <p>Handles the event. Saves the new customer settings to DB.</p>
     *
     * @param event an event fired from the external source.
     */
    @Override
    public void onEvent(CustomerCreatedEvent event) {
        DeviceInfoPluginSettings settings = settingsDAO.getPluginSettings(event.getCustomer().getId());
        if (settings == null) {
            settings = new DeviceInfoPluginSettings();
            settings.setCustomerId(event.getCustomer().getId());
        }
        settings.setSendData(true);
        settingsDAO.savePluginSettingsUnsecure(settings);
    }

    /**
     * <p>Gets the type of supported events.</p>
     *
     * @return {@link EventType#CUSTOMER_CREATED} always.
     */
    @Override
    public EventType getSupportedEventType() {
        return EventType.CUSTOMER_CREATED;
    }
}
