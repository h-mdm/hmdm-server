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

package com.hmdm.plugin.rest;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.hmdm.plugin.persistence.PluginDAO;
import com.hmdm.rest.json.Response;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

/**
 * <p>A resource to be used for accessing the data for available plugins.</p>
 *
 * @author isv
 */
@Singleton
@Path("/plugin/main")
public class PluginResource {

    /**
     * <p>A DAO for managing the plugin data.</p>
     */
    private PluginDAO pluginDAO;

    public PluginResource() {
    }

    /**
     * <p>Constructs new <code>PluginResource</code> instance. This implementation does nothing.</p>
     * @param pluginDAO
     */
    @Inject
    public PluginResource(PluginDAO pluginDAO) {
        this.pluginDAO = pluginDAO;
    }

    /**
     * <p>Gets the list of plugins for customer account associated with current user.</p>
     *
     * @return a list of available plugins.
     */
    @GET
    @Path("/private/available")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getAvailablePlugins() {
        return Response.OK(this.pluginDAO.findAvailablePlugins());
    }

    /**
     * <p>Gets the list of registered plugins, e.g. those plugins which are available in the application.</p>
     *
     * @return a list of registered plugins.
     */
    @GET
    @Path("/public/registered")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getRegisteredPlugins() {
        return Response.OK(this.pluginDAO.findRegisteredPlugins());
    }

    /**
     * <p>Gets the list of active plugins, e.g. those plugins which are installed in the application and are not marked
     * as disabled.</p>
     *
     * @return a list of active plugins.
     */
    @GET
    @Path("/private/active")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getActivePlugins() {
        return Response.OK(this.pluginDAO.findActivePlugins());
    }
}
