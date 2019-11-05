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

import com.google.inject.AbstractModule;
import com.google.inject.name.Names;
import javax.servlet.ServletContext;

public class ConfigureModule extends AbstractModule {
    private final String filesDirectory = "files.directory";
    private final String baseUrl = "base.url";
    private final ServletContext context;

    public ConfigureModule(ServletContext context) {
        this.context = context;
    }

    protected void configure() {
        this.bindConstant().annotatedWith(Names.named(filesDirectory)).to(this.context.getInitParameter(filesDirectory));
        this.bindConstant().annotatedWith(Names.named(baseUrl)).to(this.context.getInitParameter(baseUrl));
        this.bindConstant().annotatedWith(Names.named("plugins.files.directory")).to(this.context.getInitParameter("plugins.files.directory"));
        this.bindConstant().annotatedWith(Names.named("usage.scenario")).to(this.context.getInitParameter("usage.scenario"));
        this.bindConstant().annotatedWith(Names.named("hash.secret")).to(this.context.getInitParameter("hash.secret"));
        this.bindConstant().annotatedWith(Names.named("aapt.command")).to(this.context.getInitParameter("aapt.command"));
        this.bindConstant().annotatedWith(Names.named("role.orgadmin.id")).to(
                Integer.parseInt(this.context.getInitParameter("role.orgadmin.id"))
        );
    }
}