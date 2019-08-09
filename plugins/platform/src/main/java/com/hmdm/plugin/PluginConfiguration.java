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

package com.hmdm.plugin;

import com.google.inject.Module;

import javax.servlet.ServletContext;
import java.util.List;
import java.util.Optional;

/**
 * <p>An interface for plugin configuration. Each plugin is required to provide the implementation of this interface
 * named as <code>PluginConfigurationImpl</code> and located in the plugin's root package. The objects of this type
 * must provide a public no-argument constructor.</p>
 *
 * @author isv
 */
public interface PluginConfiguration {

    /**
     * <p>Gets the unique identifier for this plugin.</p>
     *
     * <p>This is a sort of logical name for the plugin which is used widely by <code>Plugin Platform</code> and plays
     * a major role in plugins development and management.</p>
     *
     * @return a plugin identifier.
     */
    String getPluginId();

    /**
     * <p>Gets the root package for the classes comprising the plugin.</p>
     *
     * @return a fully-qualified name of the root package for plugin code.
     */
    String getRootPackage();

    /**
     * <p>Gets the list of modules to be used for initializing the plugin.</p>
     *
     * @param context a context for plugin usage.
     * @return a list of modules to be used for plugin initialization.
     */
    List<Module> getPluginModules(ServletContext context);

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
