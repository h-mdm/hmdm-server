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

import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.google.inject.name.Named;
import com.sun.jersey.spi.container.servlet.ServletContainer;
import com.sun.jersey.spi.container.servlet.WebConfig;
import io.swagger.jaxrs.config.BeanConfig;

import javax.servlet.ServletException;

/**
 * <p>A bootstrap for Swagger integration.</p>
 *
 * @author isv
 */
@Singleton
public class Bootstrap extends ServletContainer {

    /**
     * <p>A host and port which the MDM application is running on.</p>
     */
    private final String host;

    /**
     * <p>A context path for REST API provided by MDM application.</p>
     */
    private final String basePath;


    /**
     * <p>Constructs new <code>Bootstrap</code> instance. This implementation does nothing.</p>
     */
    @Inject
    public Bootstrap(@Named("swagger.host") String host, @Named("swagger.base.path") String basePath) {
        this.host = host;
        this.basePath = basePath;
    }

    @Override
    protected void init(WebConfig config) throws ServletException {
        super.init(config);

        BeanConfig beanConfig = new BeanConfig();
        beanConfig.setTitle("Headwind MDM API");
        beanConfig.setVersion("0.0.2");
        beanConfig.setSchemes(new String[]{"http"});
        beanConfig.setHost(this.host);
        beanConfig.setBasePath(this.basePath);
        beanConfig.setResourcePackage("com.hmdm");
//        beanConfig.setFilterClass(MDMSwaggerSpecFilter.class.getName());
        beanConfig.setScan(true);
        beanConfig.setPrettyPrint(true);
    }

}
