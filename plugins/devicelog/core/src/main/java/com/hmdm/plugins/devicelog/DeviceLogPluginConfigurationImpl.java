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

package com.hmdm.plugins.devicelog;

import com.google.inject.Module;
import com.hmdm.plugin.PluginConfiguration;
import com.hmdm.plugin.PluginTaskModule;
import com.hmdm.plugins.devicelog.guice.module.DeviceLogLiquibaseModule;
import com.hmdm.plugins.devicelog.guice.module.DeviceLogRestModule;
import com.hmdm.plugins.devicelog.guice.module.DeviceLogTaskModule;
import com.hmdm.plugins.devicelog.persistence.DeviceLogPersistenceConfiguration;

import javax.servlet.ServletContext;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * <p>A configuration for <code>Device Log</code> plugin.</p>
 *
 * @author isv
 */
public class DeviceLogPluginConfigurationImpl implements PluginConfiguration {

    public static final String PLUGIN_ID = "devicelog";

    /**
     * <p>Constructs new <code>DeviceLogPluginConfigurationImpl</code> instance. This implementation does nothing.</p>
     */
    public DeviceLogPluginConfigurationImpl() {
    }

    /**
     * <p>Gets the unique identifier for this plugin. This is a sort of logical name for the plugin which is used widely
     * by <code>Plugin Platform</code>.</p>
     *
     * @return a plugin identifier.
     */
    @Override
    public String getPluginId() {
        return PLUGIN_ID;
    }

    /**
     * <p>Gets the root package for the classes comprising the plugin.</p>
     *
     * @return a fully-qualified name of the root package for plugin code.
     */
    @Override
    public String getRootPackage() {
        return "com.hmdm.plugins.devicelog";
    }

    /**
     * <p>Gets the list of modules to be used for initializing the plugin.</p>
     *
     * @param context a context for plugin usage.
     * @return a list of modules to be used for plugin initialization.
     */
    @Override
    public List<Module> getPluginModules(ServletContext context) {
        try {
            List<Module> modules = new ArrayList<>();

            modules.add(new DeviceLogLiquibaseModule(context));

            final String configClass = context.getInitParameter("plugin.devicelog.persistence.config.class");
            if (configClass != null && !configClass.trim().isEmpty()) {
                DeviceLogPersistenceConfiguration config
                        = (DeviceLogPersistenceConfiguration) Class.forName(configClass).newInstance();
                modules.addAll(config.getPersistenceModules(context));
            }

            modules.add(new DeviceLogRestModule());

            return modules;

        } catch (InstantiationException | IllegalAccessException | ClassNotFoundException e) {
            throw new IllegalArgumentException("Could not initialize persistence layer for Device Log plugin", e);
        }

    }

    /**
     * <p>Gets the list of task modules to be initialized upon application startup.</p>
     *
     * @return an optional list of task modules for plugins.
     */
    @Override
    public Optional<List<Class<? extends PluginTaskModule>>> getTaskModules(ServletContext context) {
        try {
            List<Class<? extends PluginTaskModule>> modules = new ArrayList<>();

            modules.add(DeviceLogTaskModule.class);

            final String configClass = context.getInitParameter("plugin.devicelog.persistence.config.class");
            if (configClass != null && !configClass.trim().isEmpty()) {
                DeviceLogPersistenceConfiguration config
                        = (DeviceLogPersistenceConfiguration) Class.forName(configClass).newInstance();
                config.getTaskModules(context).ifPresent(modules::addAll);
            }

            return Optional.of(modules);
        } catch (InstantiationException | IllegalAccessException | ClassNotFoundException e) {
            throw new IllegalArgumentException("Could not get list of task modules for Device Log plugin", e);
        }
    }
}
