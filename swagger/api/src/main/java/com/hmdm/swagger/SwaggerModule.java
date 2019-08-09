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

package com.hmdm.swagger;

import com.google.inject.servlet.ServletModule;

import java.util.HashMap;
import java.util.Map;

/**
 * <p>A Guice module to use for integration with Swagger.</p>
 *
 * @author isv
 */
public class SwaggerModule extends ServletModule {

    /**
     * <p>Constructs new <code>SwaggerModule</code> instance. This implementation does nothing.</p>
     */
    public SwaggerModule() {
    }

    /**
     * <p>Configures the environment for integration with Swagger.</p>
     */
    @Override
    protected void configureServlets() {
        super.configureServlets();

        Map<String, String> params = new HashMap<String, String>();
        params.put("com.sun.jersey.config.property.packages",
                "io.swagger.jaxrs.json;io.swagger.jaxrs.listing;com.hmdm");
        serve("/api/*").with(Bootstrap.class, params);
    }
}
