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

import com.google.inject.Inject;
import com.google.inject.Singleton;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import com.hmdm.persistence.domain.ApplicationSetting;
import com.hmdm.persistence.domain.ApplicationSettingType;
import com.hmdm.persistence.domain.ApplicationVersion;
import com.hmdm.rest.json.SyncApplicationSetting;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.codehaus.jackson.map.ObjectMapper;
import com.hmdm.persistence.UnsecureDAO;
import com.hmdm.persistence.domain.Application;
import com.hmdm.persistence.domain.Configuration;
import com.hmdm.persistence.domain.Device;
import com.hmdm.persistence.domain.Settings;
import com.hmdm.rest.json.DeviceInfo;
import com.hmdm.rest.json.Response;
import com.hmdm.rest.json.SyncResponse;

@Singleton
@Path("/public/sync")
@Api(tags = {"Device data synchronization"})
public class SyncResource {

    private UnsecureDAO unsecureDAO;

    /**
     * <p>A constructor required by Swagger.</p>
     */
    public SyncResource() {
    }

    /**
     * <p>Constructs new <code>SyncResource</code> instance. This implementation does nothing.</p>
     */
    @Inject
    public SyncResource(UnsecureDAO unsecureDAO) {
        this.unsecureDAO = unsecureDAO;
    }

    // =================================================================================================================
    @ApiOperation(
            value = "Get device info",
            notes = "Gets the device info and settings from the MDM server.",
            response = SyncResponse.class
    )
    @GET
    @Path("/configuration/{deviceId}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getDeviceSetting(@PathParam("deviceId")
                                         @ApiParam("An identifier of device within MDM server")
                                                 String number) {
        Device dbDevice = this.unsecureDAO.getDeviceByNumber(number);
        if (dbDevice != null) {

            Settings settings = this.unsecureDAO.getSettings(dbDevice.getCustomerId());
            final List<Application> applications = this.unsecureDAO.getPlainConfigurationApplications(
                    dbDevice.getCustomerId(), dbDevice.getConfigurationId()
            );

            Configuration configuration = this.unsecureDAO.getConfigurationByIdWithAppSettings(dbDevice.getConfigurationId());

            SyncResponse data;
            if (configuration.isUseDefaultDesignSettings()) {
                data = new SyncResponse(settings, configuration.getPassword(), applications, dbDevice);
            } else {
                data = new SyncResponse(configuration, applications, dbDevice);
            }

            data.setGps(configuration.getGps());
            data.setBluetooth(configuration.getBluetooth());
            data.setWifi(configuration.getWifi());
            data.setMobileData(configuration.getMobileData());
            data.setLockStatusBar(configuration.isBlockStatusBar());
            data.setSystemUpdateType(configuration.getSystemUpdateType());
            if (configuration.getSystemUpdateType() == 2) {
                data.setSystemUpdateFrom(configuration.getSystemUpdateFrom());
                data.setSystemUpdateTo(configuration.getSystemUpdateTo());
            }

            data.setKioskMode(configuration.isKioskMode());
            if (data.isKioskMode()) {
                Integer contentAppId = configuration.getContentAppId();
                if (contentAppId != null) {
                    ApplicationVersion applicationVersion = this.unsecureDAO.findApplicationVersionById(contentAppId);
                    if (applicationVersion != null) {
                        Application application = this.unsecureDAO.findApplicationById(applicationVersion.getApplicationId());
                        data.setMainApp(application.getPkg());
                    }
                }
            }

            // Evaluate the application settings
            final List<ApplicationSetting> deviceAppSettings = this.unsecureDAO.getDeviceAppSettings(dbDevice.getId());
            final List<ApplicationSetting> configApplicationSettings = configuration.getApplicationSettings();
            final List<ApplicationSetting> applicationSettings
                    = combineDeviceLogRules(configApplicationSettings, deviceAppSettings);

            data.setApplicationSettings(applicationSettings.stream().map(s -> {
                SyncApplicationSetting syncSetting = new SyncApplicationSetting();
                syncSetting.setPackageId(s.getApplicationPkg());
                syncSetting.setName(s.getName());
                syncSetting.setType(s.getType().getId());
                syncSetting.setReadonly(s.isReadonly());
                syncSetting.setValue(s.getValue());
                syncSetting.setLastUpdate(s.getLastUpdate());

                return syncSetting;
            }).collect(Collectors.toList()));

            return Response.OK(data);
        } else {
            return Response.DEVICE_NOT_FOUND_ERROR();
        }
    }

    // =================================================================================================================
    @ApiOperation(
            value = "Update device info",
            notes = "Updates the device info on the MDM server.",
            response = Response.class
    )
    @POST
    @Path("/info")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response updateDeviceInfo(DeviceInfo deviceInfo) {
        try {
            Device dbDevice = this.unsecureDAO.getDeviceByNumber(deviceInfo.getDeviceId());
            if (dbDevice != null) {
                try {
                    ObjectMapper objectMapper = new ObjectMapper();
                    this.unsecureDAO.updateDeviceInfo(dbDevice.getId(), objectMapper.writeValueAsString(deviceInfo));
                } catch (Exception e) {
                    e.printStackTrace();
                }

                return Response.OK();
            } else {
                return Response.DEVICE_NOT_FOUND_ERROR();
            }
        } catch (Exception e) {
            e.printStackTrace();
            return Response.INTERNAL_ERROR();
        }
    }

    // =================================================================================================================
    @ApiOperation(
            value = "Save application settings",
            notes = "Saves the application settings for the device on the MDM server.",
            response = Response.class
    )
    @POST
    @Path("/applicationSettings/{deviceId}")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response saveApplicationSettings(@PathParam("deviceId")
                                                @ApiParam("An identifier of device within MDM server")
                                                        String deviceNumber,
                                            List<SyncApplicationSetting> applicationSettings) {
        try {
            Device dbDevice = this.unsecureDAO.getDeviceByNumber(deviceNumber);
            if (dbDevice != null) {
                this.unsecureDAO.saveDeviceApplicationSettings(dbDevice, applicationSettings.stream().map(s -> {
                    ApplicationSetting applicationSetting = new ApplicationSetting();
                    applicationSetting.setApplicationPkg(s.getPackageId());
                    applicationSetting.setName(s.getName());
                    applicationSetting.setType(ApplicationSettingType.byId(s.getType()).orElse(ApplicationSettingType.STRING));
                    applicationSetting.setReadonly(s.isReadonly());
                    applicationSetting.setValue(s.getValue());
                    applicationSetting.setLastUpdate(s.getLastUpdate());

                    return applicationSetting;
                }).collect(Collectors.toList()));

                return Response.OK();
            } else {
                return Response.DEVICE_NOT_FOUND_ERROR();
            }
        } catch (Exception e) {
            e.printStackTrace();
            return Response.INTERNAL_ERROR();
        }
    }

    private static final Function<ApplicationSetting, String> appSettingMapKeyGenerator = (s) -> s.getApplicationId() + "," + s.getName();

    /**
     * <p>Combines the specified list of application settings into a single list.</p>
     *
     * @param lessPreferred a list of less preferred settings.
     * @param morePreferred a list of more preferred settings.
     * @return a resulting list of application settings.
     */
    private static List<ApplicationSetting> combineDeviceLogRules(List<ApplicationSetting> lessPreferred, List<ApplicationSetting> morePreferred) {

        lessPreferred = lessPreferred.stream()
                .filter(s -> s.getValue() != null && !s.getValue().trim().isEmpty())
                .collect(Collectors.toList());
        morePreferred = morePreferred.stream()
                .filter(s -> s.getValue() != null && !s.getValue().trim().isEmpty())
                .collect(Collectors.toList());

        final Map<String, ApplicationSetting> moreMapping
                = morePreferred.stream()
                .collect(Collectors.toMap(appSettingMapKeyGenerator, r -> r));

        List<ApplicationSetting> result = new ArrayList<>();

        lessPreferred.stream()
                .filter(less -> !moreMapping.containsKey(appSettingMapKeyGenerator.apply(less)))
                .forEach(result::add);

        result.addAll(morePreferred);

        return result;
    }

}