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
import com.hmdm.persistence.domain.Settings;
import com.hmdm.persistence.mapper.CommonMapper;
import com.hmdm.security.SecurityContext;
import com.hmdm.security.SecurityException;

@Singleton
public class CommonDAO extends AbstractDAO<Settings> {
    
    private final CommonMapper mapper;

    @Inject
    public CommonDAO(CommonMapper mapper) {
        this.mapper = mapper;
    }

    public Settings getSettings() {
        return getSingleRecord(this.mapper::getSettings);
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
}
