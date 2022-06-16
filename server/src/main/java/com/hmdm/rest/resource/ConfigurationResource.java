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

package com.hmdm.rest.resource;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;

import com.hmdm.notification.PushService;
import com.hmdm.persistence.ConfigurationReferenceExistsException;
import com.hmdm.persistence.CustomerDAO;
import com.hmdm.persistence.domain.ConfigurationFile;
import com.hmdm.persistence.domain.Customer;
import com.hmdm.rest.json.LookupItem;
import com.hmdm.rest.json.UpgradeConfigurationApplicationRequest;
import com.hmdm.util.FileUtil;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.Authorization;
import com.hmdm.persistence.ApplicationDAO;
import com.hmdm.persistence.ConfigurationDAO;
import com.hmdm.persistence.domain.Application;
import com.hmdm.persistence.domain.Configuration;
import com.hmdm.rest.json.Response;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.stream.Collectors;

@Api(tags = {"Configuration"}, authorizations = {@Authorization("Bearer Token")})
@Singleton
@Path("/private/configurations")
public class ConfigurationResource {

    private static final Logger log = LoggerFactory.getLogger(ConfigurationResource.class);

    private ConfigurationDAO configurationDAO;
    private ApplicationDAO applicationDAO;
    private PushService pushService;
    private CustomerDAO customerDAO;
    private String baseUrl;

    /**
     * <p>A constructor required by Swagger.</p>
     */
    public ConfigurationResource() {
    }

    @Inject
    public ConfigurationResource(ConfigurationDAO configurationDAO,
                                 ApplicationDAO applicationDAO,
                                 PushService pushService,
                                 CustomerDAO customerDAO,
                                 @Named("base.url") String baseUrl) {
        this.configurationDAO = configurationDAO;
        this.applicationDAO = applicationDAO;
        this.pushService = pushService;
        this.customerDAO = customerDAO;
        this.baseUrl = baseUrl;
    }
    // =================================================================================================================
    @ApiOperation(
            value = "Get configurations",
            notes = "Gets the list of available configurations",
            response = Configuration.class,
            responseContainer = "List"
    )
    @GET
    @Path("/search")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getAllConfigurations() {
        List<Configuration> configurations = this.configurationDAO.getAllConfigurationsByType(0);
        configurations.forEach(c -> c.setBaseUrl(this.configurationDAO.getBaseUrl()));
        return Response.OK(configurations);
    }

    // =================================================================================================================
    @ApiOperation(
            value = "Search configurations",
            notes = "Searches configurations meeting the specified filter value",
            response = Configuration.class,
            responseContainer = "List"
    )
    @GET
    @Path("/search/{value}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response searchConfigurations(@PathParam("value") String value) {
        List<Configuration> configurations = this.configurationDAO.getAllConfigurationsByTypeAndValue(0, value);
        configurations.forEach(c -> c.setBaseUrl(this.configurationDAO.getBaseUrl()));
        return Response.OK(configurations);
    }

    @ApiOperation(value = "", hidden = true)
    @GET
    @Path("/typical/search")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getAllTypicalConfigurations() {
        List<Configuration> configurations = this.configurationDAO.getAllConfigurationsByType(1);
        configurations.forEach(c -> c.setBaseUrl(this.configurationDAO.getBaseUrl()));
        return Response.OK(configurations);
    }

    @ApiOperation(value = "", hidden = true)
    @GET
    @Path("/typical/search/{value}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response searchTypicalConfigurations(@PathParam("value") String value) {
        List<Configuration> configurations = this.configurationDAO.getAllConfigurationsByTypeAndValue(1, value);
        configurations.forEach(c -> c.setBaseUrl(this.configurationDAO.getBaseUrl()));
        return Response.OK(configurations);
    }


    // =================================================================================================================
    /**
     * <p>Gets the list of configuration id/names matching the specified filter for autocompletions.</p>
     *
     * @param filter a filter to be used for filtering the records.
     * @return a response with list of configurations matching the specified filter.
     */
    @ApiOperation(value = "Get configurations for autocompletions")
    @POST
    @Path("/autocomplete")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getConfigurations(String filter) {
        try {
            List<LookupItem> groups = this.configurationDAO.getAllConfigurationsByTypeAndValue(0, filter)
                    .stream()
                    .map(configuration -> new LookupItem(configuration.getId(), configuration.getName()))
                    .collect(Collectors.toList());
            return Response.OK(groups);
        } catch (Exception e) {
            log.error("Failed to search the configurations due to unexpected error. Filter: {}", filter, e);
            return Response.INTERNAL_ERROR();
        }
    }

    // =================================================================================================================
    @ApiOperation(
            value = "Create or update configuration",
            notes = "Creates a new configuration (if id is not provided) or update existing one otherwise."
    )
    @PUT
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response updateConfiguration(Configuration configuration) {
        try {
            Configuration dbConfiguration = this.configurationDAO.getConfigurationByName(configuration.getName());
            final Integer id = configuration.getId();
            if (dbConfiguration != null && !dbConfiguration.getId().equals(id)) {
                return Response.DUPLICATE_ENTITY("error.duplicate.configuration");
            } else {
                if (id == null) {
                    this.configurationDAO.insertConfiguration(configuration);
                } else {
                    this.configurationDAO.updateConfiguration(configuration);
                    this.pushService.notifyDevicesOnUpdate(configuration.getId());
                }
                configuration = getConfiguration(configuration.getId());

                return Response.OK(configuration);
            }
        } catch (Exception e) {
            log.error("Unexpected error when saving the configuration", e);
            return Response.INTERNAL_ERROR();
        }
    }

    // =================================================================================================================
    @ApiOperation(
            value = "Upgrade configuration application",
            notes = "Upgrades the application used by configuration to most recent version",
            response = Configuration.class
    )
    @PUT
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/application/upgrade")
    public Response upgradeConfiguration(UpgradeConfigurationApplicationRequest request) {
        try {
            this.configurationDAO.upgradeConfigurationApplication(request.getConfigurationId(), request.getApplicationId());
            final Configuration configuration = this.getConfiguration(request.getConfigurationId());
            return Response.OK(configuration);
        } catch (Exception e) {
            log.error("Failed to upgrade application #{} for configuration #{} to latest version due to unexpected error",
                    request.getConfigurationId(), request.getApplicationId(), e);
            return Response.INTERNAL_ERROR();
        }
    }

    // =================================================================================================================
    @ApiOperation(
            value = "Copy configuration",
            notes = "Creates a new copy of configuration referenced by the id and names it with provided name."
    )
    @PUT
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/copy")
    public Response copyConfiguration(Configuration configuration) {
        Configuration dbConfiguration = this.configurationDAO.getConfigurationByName(configuration.getName());
        if (dbConfiguration != null) {
            return Response.DUPLICATE_ENTITY("error.duplicate.configuration");
        } else {
            dbConfiguration = this.getConfiguration(configuration.getId());
            List<Application> configurationApplications = this.configurationDAO.getPlainConfigurationApplications(configuration.getId());
            Configuration copy = dbConfiguration.newCopy();
            copy.setName(configuration.getName());
            copy.setApplications(configurationApplications);
            copy.setBaseUrl(this.configurationDAO.getBaseUrl());
            this.configurationDAO.insertConfiguration(copy);
            return Response.OK();
        }
    }

    // =================================================================================================================
    @ApiOperation(
            value = "Delete configuration",
            notes = "Deletes a configuration referenced by the specified ID."
    )
    @DELETE
    @Path("/{id}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response removeConfiguration(@PathParam("id") @ApiParam("Configuration ID") Integer id) {
        try {
            this.configurationDAO.removeConfigurationById(id);
            return Response.OK();
        } catch (ConfigurationReferenceExistsException e) {
            log.error("Failed to delete configuration #{}", id, e);
            return Response.CONFIGURATION_DEVICE_REFERENCE_EXISTS();
        } catch (Exception e) {
            log.error("Failed to delete configuration #{}", id, e);
            return Response.INTERNAL_ERROR();
        }
    }

    @ApiOperation(value = "", hidden = true)
    @GET
    @Path("/applications")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getAllApplications() {
        return Response.OK(this.applicationDAO.getAllApplications());
    }

    // =================================================================================================================
    @ApiOperation(
            value = "Get configuration applications",
            notes = "Gets the list of all applications in context of usage by the requested configuration",
            response = Application.class,
            responseContainer = "List"
    )
    @GET
    @Path("/applications/{id}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getConfigurationApplications(@PathParam("id") @ApiParam("Configuration ID") Integer id) {
        return Response.OK(this.configurationDAO.getConfigurationApplications(id));
    }

    // =================================================================================================================
    @ApiOperation(
            value = "Get configuration",
            notes = "Gets the details for configuration referenced by the specified ID",
            response = Configuration.class
    )
    @GET
    @Path("/{id}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getConfigurationById(@PathParam("id") Integer id) {
        Configuration configurationById = getConfiguration(id);

        return Response.OK(configurationById);
    }

    /**
     * <p>Gets the configuration referenced by the specified ID from DB.</p>
     *
     * @param id an ID of a configuration to get data for.
     *
     * @return a configuration referenced by the specified ID or <code>null</code> if there is no such configuration.
     */
    private Configuration getConfiguration(Integer id) {
        Configuration configuration = this.configurationDAO.getConfigurationByIdFull(id);
        if (configuration != null) {
            configuration.setBaseUrl(this.configurationDAO.getBaseUrl());
            final List<ConfigurationFile> files = configuration.getFiles();
            if (files != null && !files.isEmpty()) {
                final Customer customer = this.customerDAO.findById(configuration.getCustomerId());

                files.forEach(file -> {
                    if (file.getExternalUrl() != null) {
                        file.setUrl(file.getExternalUrl());
                    } else if (file.getFilePath() != null) {
                        final String url = FileUtil.createFileUrl(this.baseUrl, customer.getFilesDir(), file.getFilePath());
                        file.setUrl(url);
                    }
                });
            }

        }
        return configuration;
    }
}
