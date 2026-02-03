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

package com.hmdm.plugin.guice.module;

import com.hmdm.guice.LiquibaseJARResourceAccessor;
import liquibase.resource.ClassLoaderResourceAccessor;
import liquibase.resource.InputStreamList;

import java.io.IOException;

/**
 * <p>
 * Plugin-specific resource accessor for Liquibase changelog files.
 * </p>
 *
 * <p>
 * Updated for Liquibase 4.x API compatibility.
 * </p>
 *
 * @author isv
 */
public class PluginLiquibaseResourceAccessor extends ClassLoaderResourceAccessor {

    /**
     * <p>
     * Constructs new <code>PluginLiquibaseResourceAccessor</code> instance. This
     * implementation does nothing.
     * </p>
     */
    public PluginLiquibaseResourceAccessor() {
    }

    /**
     * <p>
     * Opens streams for the specified path. Updated for Liquibase 4.x API.
     * </p>
     *
     * @param relativeTo base path (can be null)
     * @param streamPath the path to open
     * @return InputStreamList containing the opened streams
     * @throws IOException if an I/O error occurs
     */
    @Override
    public InputStreamList openStreams(String relativeTo, String streamPath) throws IOException {
        LiquibaseJARResourceAccessor accessor = new LiquibaseJARResourceAccessor();
        return accessor.openStreams(relativeTo, streamPath);
    }
}
