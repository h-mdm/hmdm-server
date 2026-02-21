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

import com.hmdm.persistence.CommonDAO;
import com.hmdm.persistence.UnsecureDAO;
import com.hmdm.persistence.UserRoleSettingsDAO;
import com.hmdm.persistence.domain.Settings;
import com.hmdm.persistence.domain.UserRoleSettings;
import com.hmdm.rest.json.Response;
import com.hmdm.security.SecurityContext;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import java.util.List;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Tag(name = "Settings")
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
    public SettingsResource() {}

    @Inject
    public SettingsResource(CommonDAO commonDAO, UserRoleSettingsDAO userRoleSettingsDAO, UnsecureDAO unsecureDAO) {
        this.commonDAO = commonDAO;
        this.userRoleSettingsDAO = userRoleSettingsDAO;
        this.unsecureDAO = unsecureDAO;
    }

    // =================================================================================================================
    @Operation(summary = "Get settings", description = "Gets the current settings")
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Response getSettings() {
        try {
            Settings settings =
                    Optional.ofNullable(this.commonDAO.getSettings()).orElse(new Settings());
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
    @Operation(
            summary = "Get user role settings",
            description = "Gets the current settings for role of the current user")
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
    @Operation(
            summary = "Save default design",
            description = "Save the settings for Default Design for mobile application")
    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/design")
    public Response updateDefaultDesignSettings(Settings settings) {
        if (!SecurityContext.get().hasPermission("settings")) {
            log.error("Unauthorized attempt to update settings by user "
                    + SecurityContext.get().getCurrentUserName());
            return Response.PERMISSION_DENIED();
        }
        try {
            this.commonDAO.saveDefaultDesignSettings(settings);
            return Response.OK();
        } catch (Exception e) {
            log.error("Unexpected error when saving default design settings", e);
            return Response.INTERNAL_ERROR();
        }
    }

    // =================================================================================================================
    @Operation(summary = "Save user role common settings", description = "Save the settings for user roles")
    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/userRoles/common")
    public Response updateUserRoleCommonSettings(List<UserRoleSettings> settings) {
        if (!SecurityContext.get().hasPermission("settings")) {
            log.error("Unauthorized attempt to update settings by user "
                    + SecurityContext.get().getCurrentUserName());
            return Response.PERMISSION_DENIED();
        }
        try {
            this.userRoleSettingsDAO.saveCommonSettings(settings);
            return Response.OK();
        } catch (Exception e) {
            log.error("Unexpected error when saving user roles common settings", e);
            return Response.INTERNAL_ERROR();
        }
    }

    // =================================================================================================================
    @Operation(summary = "Save language settings", description = "Save the language settings for MDM web application")
    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/lang")
    public Response updateLanguageSettings(Settings settings) {
        if (!SecurityContext.get().hasPermission("settings")) {
            log.error("Unauthorized attempt to update settings by user "
                    + SecurityContext.get().getCurrentUserName());
            return Response.PERMISSION_DENIED();
        }
        try {
            this.commonDAO.saveLanguageSettings(settings);
            return Response.OK();
        } catch (Exception e) {
            log.error("Unexpected error when saving language settings", e);
            return Response.INTERNAL_ERROR();
        }
    }

    // =================================================================================================================
    @Operation(summary = "Save misc settings", description = "Save the misc settings for MDM web application")
    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/misc")
    public Response updateMiscSettings(Settings settings) {
        if (!SecurityContext.get().hasPermission("settings")) {
            log.error("Unauthorized attempt to update settings by user "
                    + SecurityContext.get().getCurrentUserName());
            return Response.PERMISSION_DENIED();
        }
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
