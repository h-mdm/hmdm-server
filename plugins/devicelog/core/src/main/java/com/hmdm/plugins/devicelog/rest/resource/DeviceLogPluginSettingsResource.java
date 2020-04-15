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

package com.hmdm.plugins.devicelog.rest.resource;

import javax.inject.Inject;
import javax.inject.Singleton;

import com.hmdm.notification.PushService;
import com.hmdm.persistence.ConfigurationDAO;
import com.hmdm.persistence.DeviceDAO;
import com.hmdm.persistence.GroupDAO;
import com.hmdm.persistence.domain.Device;
import com.hmdm.persistence.domain.DeviceSearchRequest;
import com.hmdm.plugins.devicelog.model.DeviceLogPluginSettings;
import com.hmdm.plugins.devicelog.model.DeviceLogRule;
import com.hmdm.plugins.devicelog.persistence.DeviceLogPluginSettingsDAO;
import com.hmdm.plugins.devicelog.rest.json.DeviceLogFilter;
import com.hmdm.rest.json.LookupItem;
import com.hmdm.rest.json.Response;
import com.hmdm.security.SecurityContext;
import com.hmdm.security.SecurityException;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.Authorization;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * <p>A resource to be used for accessing the settings for <code>Photo Plugin</code>.</p>
 *
 * @author isv
 */
@Api(tags = {"Plugin - Device Log"})
@Singleton
@Path("/plugins/devicelog/devicelog-plugin-settings")
public class DeviceLogPluginSettingsResource {

    /**
     * <p>A logger to be used for logging the events.</p>
     */
    private static final Logger log = LoggerFactory.getLogger("plugin-photo");

    /**
     * <p>A DAO for managing the plugin settings.</p>
     */
    private DeviceLogPluginSettingsDAO settingsDAO;

    private DeviceDAO deviceDAO;

    private PushService pushService;

    /**
     * <p>A constructor required by Swagger.</p>
     */
    public DeviceLogPluginSettingsResource() {
    }

    /**
     * <p>Constructs new <code>PhotoPluginSettingsResource</code> instance. This implementation does nothing.</p>
     */
    @Inject
    public DeviceLogPluginSettingsResource(DeviceLogPluginSettingsDAO settingsDAO,
                                           DeviceDAO deviceDAO,
                                           PushService pushService) {
        this.settingsDAO = settingsDAO;
        this.deviceDAO = deviceDAO;
        this.pushService = pushService;
    }

    /**
     * <p>Gets the plugin settings for customer account associated with current user. If there are none found in DB
     * then returns default ones.</p>
     *
     * @return plugin settings for current customer account.
     */
    @ApiOperation(
            value = "Get settings",
            notes = "Gets the plugin settings for current user. If there are none found in DB then returns default ones.",
            response = DeviceLogPluginSettings.class,
            authorizations = {@Authorization("Bearer Token")}
    )
    @GET
    @Path("/private")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getSettings() {
        try {
            DeviceLogPluginSettings pluginSettings = this.settingsDAO.getPluginSettings();
            if (pluginSettings != null) {
                return Response.OK(pluginSettings);
            } else {
                DeviceLogPluginSettings defaultPluginSettings = this.settingsDAO.getDefaultSettings();
                this.settingsDAO.insertPluginSettings(defaultPluginSettings);
                pluginSettings = this.settingsDAO.getPluginSettings();
                if (pluginSettings != null) {
                    return Response.OK(pluginSettings);
                } else {
                    log.error("Failed to save default device log plugin settings");
                    return Response.INTERNAL_ERROR();
                }
            }
        } catch (Exception e) {
            log.error("Failed to retrieve device log plugin settings", e);
            return Response.INTERNAL_ERROR();
        }
    }

    @ApiOperation(
            value = "Create or update plugin settings",
            notes = "Creates a new plugin settings record (if id is not provided) or updates existing one otherwise",
            authorizations = {@Authorization("Bearer Token")}
    )
    @PUT
    @Path("/private")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response saveMainSettings(String settingsJSON) {
        try {
            ObjectMapper mapper = new ObjectMapper();
            DeviceLogPluginSettings settings = mapper.readValue(settingsJSON, this.settingsDAO.getSettingsClass());
            
            if (settings.getIdentifier() == null) {
                this.settingsDAO.insertPluginSettings(settings);
            } else {
                this.settingsDAO.updatePluginSettings(settings);
            }

            return Response.OK();
        } catch (Exception e) {
            log.error("Failed to create or update device log plugin settings", e);
            return Response.INTERNAL_ERROR();
        }
    }
    @ApiOperation(
            value = "Create or update plugin settings rule",
            notes = "Creates a new plugin settings rule record (if id is not provided) or updates existing one otherwise",
            authorizations = {@Authorization("Bearer Token")}
    )
    @PUT
    @Path("/private/rule")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response saveSettingsRule(String ruleJSON) {
        try {
            ObjectMapper mapper = new ObjectMapper();
            DeviceLogRule rule = mapper.readValue(ruleJSON, this.settingsDAO.getSettingsRuleClass());

            this.settingsDAO.savePluginSettingsRule(rule);
            notifyRuleDevices(rule);

            return Response.OK();
        } catch (Exception e) {
            log.error("Failed to create or update device log plugin settings rule", e);
            return Response.INTERNAL_ERROR();
        }
    }
    // =================================================================================================================
    @ApiOperation(
            value = "Delete rule",
            notes = "Delete an existing plugin settings rule"
    )
    @DELETE
    @Path("/private/rule/{id}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response removeRule(@PathParam("id") @ApiParam("Rule ID") Integer id) {
        try {
            DeviceLogRule rule = this.settingsDAO.getPluginSettingsRuleById(id);
            if (rule != null) {
                this.settingsDAO.deletePluginSettingRule(id);
                notifyRuleDevices(rule);
            }
            return Response.OK();
        } catch (SecurityException e) {
            log.error("Prohibited to delete device log plugin settings rule #{} by current user", id, e);
            return Response.PERMISSION_DENIED();
        } catch (Exception e) {
            log.error("Failed to delete device log plugin settings rule #{} due to unexpected error", id, e);
            return Response.INTERNAL_ERROR();
        }
    }

    private void notifyRuleDevices(DeviceLogRule rule) {
        if (rule.getDevices() != null && rule.getDevices().size() > 0) {
            for (LookupItem item : rule.getDevices()) {
                pushService.notifyDeviceOnSettingUpdate(item.getId());
            }
        } else {
            List<Device> devices = getDevicesByRule(rule);
            for (Device device : devices) {
                pushService.notifyDeviceOnSettingUpdate(device.getId());
            }
        }
    }


    // Applicable only to rules which do not contain explicit list of devices!
    private List<Device> getDevicesByRule(DeviceLogRule rule) {
        DeviceSearchRequest dsr = new DeviceSearchRequest();
        dsr.setPageSize(1000000); // No page limitations
        dsr.setCustomerId(SecurityContext.get().getCurrentCustomerId().get());
        dsr.setUserId(SecurityContext.get().getCurrentUser().get().getId());
        if (rule.getConfigurationId() != null) {
            dsr.setConfigurationId(rule.getConfigurationId());
        }
        if (rule.getGroupId() != null) {
            dsr.setGroupId(rule.getGroupId());
        }
        return deviceDAO.getAllDevices(dsr).getItems();
    }
}
