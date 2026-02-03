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

package com.hmdm.guice;

import liquibase.resource.ClassLoaderResourceAccessor;
import liquibase.resource.InputStreamList;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URL;
import java.net.URLConnection;

/**
 * <p>
 * A loader for Liquibase resources which are contained within the JAR-files.
 * </p>
 *
 * <p>
 * Updated for Liquibase 4.x API compatibility.
 * </p>
 *
 * @author isv
 */
public class LiquibaseJARResourceAccessor extends ClassLoaderResourceAccessor {

    /**
     * <p>
     * Constructs new <code>LiquibaseJARResourceAccessor</code> instance. This
     * implementation does nothing.
     * </p>
     */
    public LiquibaseJARResourceAccessor() {
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
        // For JAR resources, we handle the path directly
        String fullPath = streamPath;
        if (relativeTo != null && !relativeTo.isEmpty()) {
            // Resolve relative paths
            if (!streamPath.startsWith("/") && !streamPath.contains(":")) {
                int lastSlash = relativeTo.lastIndexOf('/');
                if (lastSlash >= 0) {
                    fullPath = relativeTo.substring(0, lastSlash + 1) + streamPath;
                }
            }
        }

        // If it's a URL (jar:file:... or file:...), open it directly
        if (fullPath.contains(":")) {
            try {
                URLConnection connection = new URL(fullPath).openConnection();
                connection.setUseCaches(false);
                InputStream resourceAsStream = connection.getInputStream();
                if (resourceAsStream != null) {
                    InputStreamList list = new InputStreamList();
                    list.add(URI.create(fullPath), resourceAsStream);
                    return list;
                }
            } catch (Exception e) {
                // Fall through to parent implementation
            }
        }

        // Delegate to parent for classpath resources
        return super.openStreams(relativeTo, streamPath);
    }

}
