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

package com.hmdm.plugins.devicelog.persistence;

import com.google.inject.Module;
import com.hmdm.plugin.PluginTaskModule;

import javax.servlet.ServletContext;
import java.util.List;
import java.util.Optional;

/**
 * <p>An interface for the configuration specific to concrete persistence mechanism used for storing and managing the
 * device log records.</p>
 *
 * @author isv
 */
public interface DeviceLogPersistenceConfiguration {

    /**
     * <p>Gets the list of modules to be used for initializing the persistence layer.</p>
     *
     * @param context a context for plugin usage.
     * @return a list of modules to be used for persistence layer initialization.
     */
    List<Module> getPersistenceModules(ServletContext context);

    /**
     * <p>Gets the list of task modules to be initialized upon application startup.</p>
     *
     * @param context a context for plugin usage.
     * @return an optional list of task modules for plugins.
     */
    default Optional<List<Class<? extends PluginTaskModule>>> getTaskModules(ServletContext context) {
        return Optional.empty();
    }
}
