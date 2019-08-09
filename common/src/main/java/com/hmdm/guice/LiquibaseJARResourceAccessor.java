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

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import java.util.HashSet;
import java.util.Set;

/**
 * <p>A loader for Liquibase resources which are contained within the JAR-files.</p>
 *
 * @author isv
 */
public class LiquibaseJARResourceAccessor extends ClassLoaderResourceAccessor {

    /**
     * <p>Constructs new <code>LiquibaseJARResourceAccessor</code> instance. This implementation does nothing.</p>
     */
    public LiquibaseJARResourceAccessor() {
    }

    @Override
    public Set<InputStream> getResourcesAsStream(String path) throws IOException {
        URLConnection connection = new URL(path).openConnection();
        connection.setUseCaches(false);

        InputStream resourceAsStream = connection.getInputStream();
        Set<InputStream> returnSet = new HashSet<>();
        if (resourceAsStream != null) {
            returnSet.add(resourceAsStream);
        }

        return returnSet;
    }

}
