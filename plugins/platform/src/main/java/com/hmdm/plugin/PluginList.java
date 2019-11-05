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
import io.github.classgraph.ClassGraph;
import io.github.classgraph.ClassInfoList;
import io.github.classgraph.ScanResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.ServletContext;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentSkipListSet;

/**
 * <p>A static list maintaining the list of plugins enabled for current build.</p>
 *
 * @author isv
 */
public final class PluginList {

    private static final Logger log = LoggerFactory.getLogger(PluginList.class);

    /**
     * <p>A collection of identifiers for plugins which have been enabled for the current build.</p>
     */
    private static final Set<String> enabledPlugins = new ConcurrentSkipListSet<>();

    /**
     * <p>Constructs new <code>PluginList</code> instance. This implementation does nothing.</p>
     */
    private PluginList() {
    }

    /**
     * <p>Checks if specified plugin is enabled for the current build.</p>
     *
     * @param pluginId an identifier of a plugin to check.
     * @return <code>true</code> if specified plugin is enabled for current build; <code>false</code> otherwise.
     * @see PluginConfiguration#getPluginId()
     */
    public static boolean isPluginEnabled(String pluginId) {
        return enabledPlugins.contains(pluginId);
    }

    private static boolean initialized = false;
    private static List<com.google.inject.Module> pluginModules;
    private static List<Class<? extends PluginTaskModule>> pluginTaskModules;

    public static synchronized void init(ServletContext context) {
        if (initialized) {
            return;
        }
        try (ScanResult scanResult = new ClassGraph().enableAllInfo().whitelistPackages("com.hmdm").scan()) {
            ClassInfoList pluginConfigClasses = scanResult.getClassesImplementing(PluginConfiguration.class.getName());
            List<String> plugins = pluginConfigClasses.getNames();

            List<Module> result = new ArrayList<>();
            List<Class<? extends PluginTaskModule>> pluginTaskModules = new ArrayList<>();
            Set<String> processedPlugins = new HashSet<>();

            for (String pluginConfigClassName : plugins) {
                pluginConfigClassName = pluginConfigClassName.trim();
                try {
                    PluginConfiguration pluginConfiguration
                            = (PluginConfiguration) Class.forName(pluginConfigClassName).newInstance();
                    String pluginId = pluginConfiguration.getPluginId().toLowerCase();
                    if (processedPlugins.contains(pluginId)) {
                        log.warn("Duplicate plugin found: {}. Skipping initialization of {}",
                                pluginId, pluginConfiguration.getClass().getName());
                        continue;
                    }
                    processedPlugins.add(pluginId);

                    List<Module> pluginModules = pluginConfiguration.getPluginModules(context);
                    if (pluginModules != null && !pluginModules.isEmpty()) {
                        result.addAll(pluginModules);
                    }

                    pluginConfiguration.getTaskModules(context).ifPresent(pluginTaskModules::addAll);

                    enabledPlugins.add(pluginId);
                    
                } catch (InstantiationException | IllegalAccessException e) {
                    log.error("Failed to instantiate plugin configuration for plugin '{}'", pluginConfigClassName, e);
                } catch (ClassNotFoundException e) {
                    log.error("Could not find plugin configuration class: {}", pluginConfigClassName);
                }
            }

            PluginList.pluginModules = result;
            PluginList.pluginTaskModules = pluginTaskModules;
            PluginList.initialized = true;
        }
    }

    public static synchronized List<Module> getPluginModules() {
        if (!PluginList.initialized) {
            throw new IllegalStateException("Not initialized yet");
        }
        return PluginList.pluginModules;
    }

    public static synchronized List<Class<? extends PluginTaskModule>> getPluginTaskModules() {
        if (!PluginList.initialized) {
            throw new IllegalStateException("Not initialized yet");
        }
        return PluginList.pluginTaskModules;
    }

    public static List<String> getEnabledPlugins() {
        return new ArrayList<>(enabledPlugins);
    }
}
