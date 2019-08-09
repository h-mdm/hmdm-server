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

package com.hmdm.plugins.devicelog.persistence.postgres;

import com.google.inject.Module;
import com.hmdm.plugin.PluginTaskModule;
import com.hmdm.plugins.devicelog.persistence.DeviceLogPersistenceConfiguration;
import com.hmdm.plugins.devicelog.persistence.postgres.guice.module.DeviceLogPostgresLiquibaseModule;
import com.hmdm.plugins.devicelog.persistence.postgres.guice.module.DeviceLogPostgresPersistenceModule;
import com.hmdm.plugins.devicelog.persistence.postgres.guice.module.DeviceLogPostgresServiceModule;
import com.hmdm.plugins.devicelog.persistence.postgres.guice.module.DeviceLogPostgresTaskModule;

import javax.servlet.ServletContext;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * <p>A device log persistence configuration backed by the postgres services.</p>
 *
 * @author isv
 */
public class DeviceLogPostgresPersistenceConfiguration implements DeviceLogPersistenceConfiguration {

    /**
     * <p>Constructs new <code>DeviceLogPostgresPersistenceConfiguration</code> instance. This implementation does nothing.</p>
     */
    public DeviceLogPostgresPersistenceConfiguration() {
    }

    /**
     * <p>Gets the list of modules to be used for initializing the persistence layer.</p>
     *
     * @param context a context for plugin usage.
     * @return a list of modules to be used for persistence layer initialization.
     */
    @Override
    public List<Module> getPersistenceModules(ServletContext context) {
        List<Module> modules = new ArrayList<>();

        modules.add(new DeviceLogPostgresLiquibaseModule(context));
        modules.add(new DeviceLogPostgresServiceModule());
        modules.add(new DeviceLogPostgresPersistenceModule(context));

        return modules;
    }

    /**
     * <p>Gets the list of task modules to be initialized upon application startup.</p>
     *
     * @param context a context for plugin usage.
     * @return an optional list of task modules for plugins.
     */
    @Override
    public Optional<List<Class<? extends PluginTaskModule>>> getTaskModules(ServletContext context) {
        List<Class<? extends PluginTaskModule>> modules = new ArrayList<>();

        modules.add(DeviceLogPostgresTaskModule.class);

        return Optional.of(modules);
    }
}
