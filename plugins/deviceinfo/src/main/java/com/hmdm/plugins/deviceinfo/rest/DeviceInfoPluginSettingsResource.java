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

package com.hmdm.plugins.deviceinfo.rest;

import com.hmdm.notification.PushService;
import com.hmdm.persistence.DeviceDAO;
import com.hmdm.persistence.UnsecureDAO;
import com.hmdm.persistence.domain.Device;
import com.hmdm.persistence.domain.Settings;
import com.hmdm.plugin.service.PluginStatusCache;
import com.hmdm.plugins.deviceinfo.persistence.DeviceInfoSettingsDAO;
import com.hmdm.plugins.deviceinfo.persistence.domain.DeviceInfoPluginSettings;
import com.hmdm.plugins.deviceinfo.rest.json.DeviceSettings;
import com.hmdm.rest.json.Response;
import com.hmdm.security.SecurityContext;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.PUT;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import java.util.List;
import java.util.Optional;

import static com.hmdm.plugins.deviceinfo.DeviceInfoPluginConfigurationImpl.PLUGIN_ID;

/**
 * <p>A resource to be used for managing the <code>Device Info</code> plugin settings for customer account associated
 * with current user.</p>
 *
 * @author isv
 */
@Singleton
@Path("/plugins/deviceinfo/deviceinfo-plugin-settings")
@Tag(name="Device Info plugin settings")
public class DeviceInfoPluginSettingsResource {

    private static final Logger logger = LoggerFactory.getLogger(DeviceInfoPluginSettingsResource.class);

    private DeviceInfoSettingsDAO settingsDAO;

    private PluginStatusCache pluginStatusCache;

    /**
     * <p>An interface to persistence without security checks.</p>
     */
    private UnsecureDAO unsecureDAO;

    /**
     * <p>A DAO for getting device list.</p>
     */
    private DeviceDAO deviceDAO;

    /**
     * <p>A DAO for sending Push messages to devices.</p>
     */
    private PushService pushService;

    /**
     * <p>A constructor required by Swagger.</p>
     */
    public DeviceInfoPluginSettingsResource() {
    }

    /**
     * <p>Constructs new <code>DeviceInfoPluginSettingsResource</code> instance. This implementation does nothing.</p>
     */
    @Inject
    public DeviceInfoPluginSettingsResource(DeviceInfoSettingsDAO settingsDAO,
                                            UnsecureDAO unsecureDAO,
                                            PluginStatusCache pluginStatusCache,
                                            DeviceDAO deviceDAO,
                                            PushService pushService) {
        this.settingsDAO = settingsDAO;
        this.unsecureDAO = unsecureDAO;
        this.pluginStatusCache = pluginStatusCache;
        this.deviceDAO = deviceDAO;
        this.pushService = pushService;
    }

    /**
     * <p>Gets the plugin settings for customer account associated with current user. If there are none found in DB
     * then returns default ones.</p>
     *
     * @return plugin settings for current customer account.
     */
    @Operation(summary = "Get settings",
            description = "Gets the plugin settings for current user. If there are none found in DB then returns default ones."
    )
    @GET
    @Path("/private")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getSettings() {
        return Response.OK(
                Optional.ofNullable(this.settingsDAO.getPluginSettings())
                        .orElse(new DeviceInfoPluginSettings())
        );
    }

    // =================================================================================================================
    @Operation(summary = "Save settings",
            description = "Save the Device Info plugin settings"
    )
    @PUT
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/private")
    public Response saveSettings(DeviceInfoPluginSettings settings) {
        try {
            this.settingsDAO.savePluginSettings(settings);
            List<Device> devices = deviceDAO.getAllDevices();
            for (Device device : devices) {
                pushService.notifyDeviceOnSettingUpdate(device.getId());
            }
            return Response.OK();
        } catch (Exception e) {
            logger.error("Unexpected error when saving Device Info plugin settings", e);
            return Response.INTERNAL_ERROR();
        }
    }

    // =================================================================================================================
    @Operation(summary = "Get plugin settings by device",
            description = "Gets the plugin settings for usage by device "
    )
    @GET
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/device/{deviceNumber}")
    public Response lookupDevices(@PathParam("deviceNumber") String deviceNumber) {
        try {
            // Find device and set the device ID for records
            Device dbDevice = this.unsecureDAO.getDeviceByNumber(deviceNumber);
            if (dbDevice == null) {
                logger.error("Device {} was not found", deviceNumber);
                return Response.DEVICE_NOT_FOUND_ERROR();
            }

            SecurityContext.init(dbDevice.getCustomerId());
            try {
                if (this.pluginStatusCache.isPluginDisabled(PLUGIN_ID)) {
                    logger.error("Rejecting request from device {} due to disabled plugin", deviceNumber);
                    return Response.PLUGIN_DISABLED();
                }

                final DeviceInfoPluginSettings pluginSettings = this.settingsDAO.getPluginSettings(dbDevice.getCustomerId());

                return Response.OK(new DeviceSettings(pluginSettings));
            } finally {
                SecurityContext.release();
            }
        } catch (Exception e) {
            logger.error("Unexpected error when retrieving device info", e);
            return Response.INTERNAL_ERROR();
        }
    }
}
