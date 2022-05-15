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

package com.hmdm.plugins.push.guice.module;

import com.hmdm.guice.module.AbstractLiquibaseModule;
import com.hmdm.plugin.guice.module.PluginLiquibaseResourceAccessor;
import liquibase.resource.ResourceAccessor;

import javax.servlet.ServletContext;

/**
 * <p>A module used for initializing and managing the state of the database tables related to <code>Push</code>
 * plugin.</p>
 *
 * @author isv
 */
public class PushLiquibaseModule extends AbstractLiquibaseModule {

    /**
     * <p>Constructs new <code>PushLiquibaseModule</code> instance. This implementation does nothing.</p>
     */
    public PushLiquibaseModule(ServletContext context) {
        super(context);
    }

    /**
     * <p>Gets the path to the DB change log to be used by this module.</p>
     *
     * <p>Plugins MUST override this method to provide the path to specific Db change log.</p>
     *
     * @return a path to resource with Db change log.
     */
    @Override
    protected String getChangeLogResourcePath() {
        String path = this.getClass().getResource("/liquibase/push.changelog.xml").getPath();
        if (!path.startsWith("jar:")) {
            path = "jar:" + path;
        }
        return path;
    }

    /**
     * <p>Gets the resource accessor to be used for loading the change log file.</p>
     *
     * @return a resource accessor for change log file.
     */
    @Override
    protected ResourceAccessor getResourceAccessor() {
        return new PluginLiquibaseResourceAccessor();
    }

}
