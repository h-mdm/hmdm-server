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

import javax.inject.Inject;
import javax.inject.Singleton;
import com.hmdm.plugin.persistence.PluginDAO;
import com.hmdm.plugin.service.PluginStatusCache;
import com.hmdm.rest.json.Response;
import com.hmdm.security.SecurityContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
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

    private static final Logger logger = LoggerFactory.getLogger(PluginResource.class);

    /**
     * <p>A DAO for managing the plugin data.</p>
     */
    private PluginDAO pluginDAO;

    private PluginStatusCache pluginStatusCache;

    /**
     * <p>A constructor required by Swagger.</p>
     */
    public PluginResource() {
    }

    /**
     * <p>Constructs new <code>PluginResource</code> instance. This implementation does nothing.</p>
     */
    @Inject
    public PluginResource(PluginDAO pluginDAO, PluginStatusCache pluginStatusCache) {
        this.pluginDAO = pluginDAO;
        this.pluginStatusCache = pluginStatusCache;
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
        try {
            return Response.OK(this.pluginDAO.findAvailablePlugins());
        } catch (Exception e) {
            logger.error("Unexpected error when getting available plugins", e);
            return Response.INTERNAL_ERROR();
        }
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
        try {
            return Response.OK(this.pluginDAO.findRegisteredPlugins());
        } catch (Exception e) {
            logger.error("Unexpected error when getting registered plugins", e);
            return Response.INTERNAL_ERROR();
        }
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
        try {
            return Response.OK(this.pluginDAO.findActivePlugins());
        } catch (Exception e) {
            logger.error("Unexpected error when getting active plugins", e);
            return Response.INTERNAL_ERROR();
        }
    }

    /**
     * <p>Disables the specified plugins from usage for customer account associated with the current user.</p>
     *
     * @param pluginIds a list of IDs of plugins to be disabled.
     * @return empty response.
     */
    @POST
    @Path("/private/disabled")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    public Response saveUsedPlugins(Integer[] pluginIds) {
        try {
            if (!SecurityContext.get().hasPermission("plugins_customer_access_management")) {
                logger.error("The user is not granted the 'plugins_customer_access_management' permission");
                return Response.PERMISSION_DENIED();
            }
            
            this.pluginDAO.saveDisabledPlugins(pluginIds);
            this.pluginStatusCache.setCustomerDisabledPlugins(SecurityContext.get().getCurrentUser().get().getCustomerId(), pluginIds);
            return Response.OK();
        } catch (Exception e) {
            logger.error("Unexpected error when disabling plugins", e);
            return Response.INTERNAL_ERROR();
        }
    }
}
