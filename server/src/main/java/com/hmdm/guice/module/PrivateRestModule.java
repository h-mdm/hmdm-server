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

import com.google.inject.Scopes;
import com.google.inject.servlet.ServletModule;
import com.hmdm.rest.resource.HintResource;
import com.hmdm.rest.filter.AuthFilter;
import com.hmdm.rest.resource.ApplicationResource;
import com.hmdm.rest.resource.ConfigurationResource;
import com.hmdm.rest.resource.CustomerResource;
import com.hmdm.rest.resource.DeviceResource;
import com.hmdm.rest.resource.FilesResource;
import com.hmdm.rest.resource.GroupResource;
import com.hmdm.rest.resource.IconFileResource;
import com.hmdm.rest.resource.IconResource;
import com.hmdm.rest.resource.SettingsResource;
import com.hmdm.rest.resource.UserResource;
import com.hmdm.security.jwt.JWTFilter;

public class PrivateRestModule extends ServletModule {
    public PrivateRestModule() {
    }

    protected void configureServlets() {
        this.filter("/rest/private/*").through(JWTFilter.class);
        this.filter("/rest/private/*").through(AuthFilter.class);
        this.bind(DeviceResource.class);
        this.bind(GroupResource.class);
        this.bind(ConfigurationResource.class);
        this.bind(CustomerResource.class);
        this.bind(SettingsResource.class);
        this.bind(ApplicationResource.class);
        this.bind(IconResource.class);
        this.bind(UserResource.class);
        this.bind(FilesResource.class);
        this.bind(IconFileResource.class);
        this.bind(HintResource.class);
    }
}
