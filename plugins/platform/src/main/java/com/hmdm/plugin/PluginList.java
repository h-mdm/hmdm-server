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

import java.util.HashSet;
import java.util.Set;

/**
 * <p>A static list maintaining the list of plugins enabled for current build.</p>
 *
 * @author isv
 */
public final class PluginList {

    /**
     * <p>A collection of identifiers for plugins which have been enabled for the current build.</p>
     */
    private static final Set<String> enabledPlugins = new HashSet<>();

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

    /**
     * <p>Marks the specified plugin as enabled for the current build. This method is intended for use only at
     * application initialization stage.</p>
     *
     * @param pluginId an identifier of a plugin to enable.
     */
    public static void enablePlugin(String pluginId) {
        if (new Exception().getStackTrace()[1].getClassName().equals("com.hmdm.guice.Initializer")) {
            enabledPlugins.add(pluginId);
        } else {
            throw new IllegalStateException("Inappropriate call");
        }
    }
}
