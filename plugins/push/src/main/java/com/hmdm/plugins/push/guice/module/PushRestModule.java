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

import com.google.inject.servlet.ServletModule;
import com.hmdm.plugin.rest.PluginAccessFilter;
import com.hmdm.plugins.push.rest.PushResource;
import com.hmdm.rest.filter.AuthFilter;
import com.hmdm.security.jwt.JWTFilter;

import java.util.Arrays;
import java.util.List;

/**
 * <p>A <code>Guice</code> module for <code>Push Plugin</code> REST resources.</p>
 *
 * @author isv
 */
public class PushRestModule extends ServletModule {

    /**
     * <p>A list of patterns for URIs for plugin resources which prohibit anonymous access.</p>
     */
    private static final List<String> protectedResources = Arrays.asList(
            "/rest/plugins/push/private/*"
    );

    /**
     * <p>Constructs new <code>PushRestModule</code> instance. This implementation does nothing.</p>
     */
    public PushRestModule() {
    }

    /**
     * <p>Configures the <code>Push Plugin</code> REST resources.</p>
     */
    protected void configureServlets() {
        this.filter(protectedResources).through(JWTFilter.class);
        this.filter(protectedResources).through(AuthFilter.class);
        this.filter(protectedResources).through(PluginAccessFilter.class);
        this.bind(PushResource.class);
    }

}
