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

package com.hmdm.guice.module;

import javax.servlet.ServletContext;

import liquibase.resource.FileSystemResourceAccessor;
import liquibase.resource.ResourceAccessor;

/**
 * <p>A module used for initializing or modifying the database based on the provided Liquibase change log.</p>
 *
 * @author isv
 */
public class LiquibaseModule extends AbstractLiquibaseModule {

    /**
     * <p>Constructs new <code>LiquibaseModule</code> instance for use in specified context.</p>
     *
     * @param context a context for module usage.
     */
    public LiquibaseModule(ServletContext context) {
        super(context);
    }
    /**
     * <p>Gets the path to the DB change log to be used by this module.</p>
     *
     * <p>Plugins MUST override this method to provide the path to specific Db change log.</p>
     *
     * @return a path to resource with Db change log.
     */
    protected String getChangeLogResourcePath() {
        return this.getClass().getResource("/liquibase/db.changelog.xml").getPath();
    }

    /**
     * <p>Gets the resource accessor to be uused for loading the change log file.</p>
     *
     * @return a resource accessor for change log file.
     */
    @Override
    protected ResourceAccessor getResourceAccessor() {
        return new FileSystemResourceAccessor();
    }
}