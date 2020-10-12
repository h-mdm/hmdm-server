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
import javax.inject.Singleton;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import com.hmdm.persistence.CustomerDAO;
import com.hmdm.persistence.UnsecureDAO;
import com.hmdm.persistence.UserRoleSettingsDAO;
import com.hmdm.persistence.domain.UserRoleSettings;
import com.hmdm.security.SecurityContext;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.Authorization;
import com.hmdm.persistence.CommonDAO;
import com.hmdm.persistence.domain.Settings;
import com.hmdm.rest.json.Response;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Optional;

@Api(tags = {"Settings"}, authorizations = {@Authorization("Bearer Token")})
@Singleton
@Path("/private/settings")
public class SettingsResource {

    private static final Logger log = LoggerFactory.getLogger(SettingsResource.class);

    private CommonDAO commonDAO;
    private UserRoleSettingsDAO userRoleSettingsDAO;
    private UnsecureDAO unsecureDAO;

    /**
     * <p>A constructor required by Swagger.</p>
     */
    public SettingsResource() {
    }

    @Inject
    public SettingsResource(CommonDAO commonDAO, UserRoleSettingsDAO userRoleSettingsDAO, UnsecureDAO unsecureDAO) {
        this.commonDAO = commonDAO;
        this.userRoleSettingsDAO = userRoleSettingsDAO;
        this.unsecureDAO = unsecureDAO;
    }

    // =================================================================================================================
    @ApiOperation(
            value = "Get settings",
            notes = "Gets the current settings",
            response = Settings.class
    )
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Response getSettings() {
        try {
            Settings settings = Optional.ofNullable(this.commonDAO.getSettings()).orElse(new Settings());
            settings.setSingleCustomer(unsecureDAO.isSingleCustomer());
            if (!settings.isSingleCustomer()) {
                this.commonDAO.loadCustomerSettings(settings);
            }
            return Response.OK(settings);
        } catch (Exception e) {
            log.error("Unexpected error when getting the settings for customer", e);
            return Response.INTERNAL_ERROR();
        }
    }

    // =================================================================================================================
    @ApiOperation(
            value = "Get user role settings",
            notes = "Gets the current settings for role of the current user",
            response = UserRoleSettings.class
    )
    @GET
    @Path("/userRole/{roleId}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getUserRoleSettings(@PathParam("roleId") int roleId) {
        try {
            UserRoleSettings settings = this.userRoleSettingsDAO.getUserRoleSettings(roleId);
            if (settings == null) {
                final UserRoleSettings defaultSettings = new UserRoleSettings();
                defaultSettings.setRoleId(roleId);

                settings = defaultSettings;
            }
            return Response.OK(settings);
        } catch (Exception e) {
            log.error("Unexpected error when getting the user role settings for current user", e);
            return Response.INTERNAL_ERROR();
        }
    }

    // =================================================================================================================
    @ApiOperation(
            value = "Save default design",
            notes = "Save the settings for Default Design for mobile application",
            response = Settings.class
    )
    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/design")
    public Response updateDefaultDesignSettings(Settings settings) {
        try {
            this.commonDAO.saveDefaultDesignSettings(settings);
            return Response.OK();
        } catch (Exception e) {
            log.error("Unexpected error when saving default design settings", e);
            return Response.INTERNAL_ERROR();
        }
    }

    // =================================================================================================================
    @ApiOperation(
            value = "Save user role common settings",
            notes = "Save the settings for user roles",
            response = Settings.class
    )
    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/userRoles/common")
    public Response updateUserRoleCommonSettings(List<UserRoleSettings> settings) {
        try {
            this.userRoleSettingsDAO.saveCommonSettings(settings);
            return Response.OK();
        } catch (Exception e) {
            log.error("Unexpected error when saving user roles common settings", e);
            return Response.INTERNAL_ERROR();
        }
    }

    // =================================================================================================================
    @ApiOperation(
            value = "Save language settings",
            notes = "Save the language settings for MDM web application",
            response = Settings.class
    )
    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/lang")
    public Response updateLanguageSettings(Settings settings) {
        try {
            this.commonDAO.saveLanguageSettings(settings);
            return Response.OK();
        } catch (Exception e) {
            log.error("Unexpected error when saving language settings", e);
            return Response.INTERNAL_ERROR();
        }
    }

    // =================================================================================================================
    @ApiOperation(
            value = "Save misc settings",
            notes = "Save the misc settings for MDM web application",
            response = Settings.class
    )
    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/misc")
    public Response updateMiscSettings(Settings settings) {
        try {
            if (!unsecureDAO.isSingleCustomer()) {
                // These settings are not allowed to setup in multi-tenant mode
                settings.setCreateNewDevices(false);
                settings.setNewDeviceGroupId(null);
                settings.setNewDeviceConfigurationId(null);
            }
            this.commonDAO.saveMiscSettings(settings);
            return Response.OK();
        } catch (Exception e) {
            log.error("Unexpected error when saving misc settings", e);
            return Response.INTERNAL_ERROR();
        }
    }
}
