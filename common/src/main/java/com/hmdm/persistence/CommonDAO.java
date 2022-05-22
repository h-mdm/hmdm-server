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
import com.google.inject.Singleton;
import com.hmdm.persistence.domain.*;
import com.hmdm.persistence.mapper.CommonMapper;
import com.hmdm.persistence.mapper.CustomerMapper;
import com.hmdm.persistence.mapper.DeviceMapper;
import com.hmdm.security.SecurityContext;
import com.hmdm.security.SecurityException;

@Singleton
public class CommonDAO extends AbstractDAO<Settings> {
    
    private final CommonMapper mapper;
    private final CustomerMapper customerMapper;
    private final DeviceMapper deviceMapper;

    @Inject
    public CommonDAO(CommonMapper mapper, CustomerMapper customerMapper, DeviceMapper deviceMapper) {
        this.mapper = mapper;
        this.customerMapper = customerMapper;
        this.deviceMapper = deviceMapper;
    }

    public Settings getSettings() {
        return getSingleRecord(this.mapper::getSettings);
    }

    public void loadCustomerSettings(Settings settings) {
        User currentUser = SecurityContext.get()
                .getCurrentUser()
                .get();
        if (currentUser != null) {
            Customer customer = customerMapper.findCustomerById(currentUser.getCustomerId());
            if (!customer.isMaster()) {
                settings.setAccountType(customer.getAccountType());
                settings.setExpiryTime(customer.getExpiryTime());
                settings.setDeviceLimit(customer.getDeviceLimit());
            } else {
                settings.setAccountType(Customer.Primary);
            }
            Long deviceCount = deviceMapper.countAllDevicesForCustomer(currentUser.getCustomerId());
            if (deviceCount != null) {
                settings.setDeviceCount(deviceCount.intValue());
            }
        }
    }

    public void saveDefaultDesignSettings(Settings settings) {
        insertRecord(settings, this.mapper::saveDefaultDesignSettings);
    }

    public void saveLanguageSettings(Settings settings) {
        insertRecord(settings, this.mapper::saveLanguageSettings);
    }

    public void saveMiscSettings(Settings settings) {
        insertRecord(settings, this.mapper::saveMiscSettings);
    }

    public void saveDefaultDesignSettingsBySuperAdmin(Settings settings) {
        if (SecurityContext.get().getCurrentUser().get().isSuperAdmin()) {
            this.mapper.saveDefaultDesignSettings(settings);
        } else {
            throw SecurityException.onAdminDataAccessViolation("save customer settings");
        }
    }

    public boolean isDatabaseInitialized() {
        return mapper.getSettingsCount() > 0;
    }

    public void executeRawQuery(String query) {
        mapper.executeRawQuery(query);
    }
}
