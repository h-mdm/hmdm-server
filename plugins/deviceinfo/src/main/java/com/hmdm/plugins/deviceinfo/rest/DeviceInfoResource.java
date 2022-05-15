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

import com.hmdm.event.DeviceLocationUpdatedEvent;
import com.hmdm.event.EventService;
import com.hmdm.persistence.DeviceDAO;
import com.hmdm.persistence.UnsecureDAO;
import com.hmdm.persistence.domain.Device;
import com.hmdm.plugin.service.PluginStatusCache;
import com.hmdm.plugins.deviceinfo.persistence.DeviceInfoDAO;
import com.hmdm.plugins.deviceinfo.persistence.domain.DeviceDynamicInfo;
import com.hmdm.plugins.deviceinfo.rest.json.DeviceDynamicInfoRecord;
import com.hmdm.plugins.deviceinfo.rest.json.DeviceInfo;
import com.hmdm.plugins.deviceinfo.rest.json.DynamicInfoExportFilter;
import com.hmdm.plugins.deviceinfo.rest.json.DynamicInfoFilter;
import com.hmdm.plugins.deviceinfo.service.DeviceInfoExportService;
import com.hmdm.rest.json.DeviceLocation;
import com.hmdm.rest.json.DeviceLookupItem;
import com.hmdm.rest.json.PaginatedData;
import com.hmdm.rest.json.Response;
import com.hmdm.security.SecurityContext;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.Authorization;
import org.glassfish.jersey.media.multipart.ContentDisposition;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.inject.Singleton;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.StreamingOutput;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;

import static com.hmdm.plugins.deviceinfo.DeviceInfoPluginConfigurationImpl.PLUGIN_ID;

/**
 * <p>A resource to be used for managing the <code>Device Info</code> plugin data for customer account associated
 * with current user.</p>
 *
 * @author isv
 */
@Singleton
@Path("/plugins/deviceinfo/deviceinfo")
@Api(tags = {"Device Info plugin"})
public class DeviceInfoResource {

    private static final Logger logger = LoggerFactory.getLogger(DeviceInfoResource.class);

    /**
     * <p>An interface to device info records persistence.</p>
     */
    private DeviceInfoDAO deviceInfoDAO;

    /**
     * <p>An interface to persistence without security checks.</p>
     */
    private UnsecureDAO unsecureDAO;

    /**
     * <p>An interface to device records persistence.</p>
     */
    private DeviceDAO deviceDAO;

    /**
     * <p>An interface to device records persistence.</p>
     */
    private DeviceInfoExportService deviceInfoExportService;

    private PluginStatusCache pluginStatusCache;

    /**
     * <p>A service used for sending notifications on location update for device</p>
     */
    private EventService eventService;

    /**
     * <p>A constructor required by swagger.</p>
     */
    public DeviceInfoResource() {
    }

    /**
     * <p>Constructs new <code>DeviceInfoResource</code> instance. This implementation does nothing.</p>
     */
    @Inject
    public DeviceInfoResource(DeviceInfoDAO deviceInfoDAO,
                              UnsecureDAO unsecureDAO,
                              DeviceDAO deviceDAO,
                              DeviceInfoExportService deviceInfoExportService,
                              PluginStatusCache pluginStatusCache,
                              EventService eventService) {
        this.deviceInfoDAO = deviceInfoDAO;
        this.unsecureDAO = unsecureDAO;
        this.deviceDAO = deviceDAO;
        this.deviceInfoExportService = deviceInfoExportService;
        this.pluginStatusCache = pluginStatusCache;
        this.eventService = eventService;
    }

    // =================================================================================================================
    @ApiOperation(
            value = "Save device info",
            notes = "Save the Device Info dynamic data",
            response = Void.class
    )
    @PUT
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/public/{deviceNumber}")
    public Response saveDeviceInfo(@PathParam("deviceNumber") String deviceNumber, List<DeviceDynamicInfo> data) {
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

                data.forEach(record -> {
                    record.setDeviceId(dbDevice.getId());
                    record.setCustomerId(dbDevice.getCustomerId());
                });

                this.deviceInfoDAO.saveDeviceDynamicData(data);

                // Send locations to the location plugin
                List<DeviceLocation> locations = new LinkedList<>();
                for (DeviceDynamicInfo info : data) {
                    if (info.getGps() != null && info.getGps().getLat() != null && info.getGps().getLon() != null) {
                        DeviceLocation location = new DeviceLocation();
                        location.setLat(info.getGps().getLat());
                        location.setLon(info.getGps().getLon());
                        location.setTs(info.getTs());
                        locations.add(location);
                    }
                }
                if (locations.size() > 0) {
                    this.eventService.fireEvent(
                            new DeviceLocationUpdatedEvent(dbDevice.getId(), locations, true)
                    );
                }

                return Response.OK();
            } finally {
                SecurityContext.release();
            }
        } catch (Exception e) {
            logger.error("Unexpected error when saving device dynamic info", e);
            return Response.INTERNAL_ERROR();
        }
    }

    // =================================================================================================================
    @ApiOperation(
            value = "Get device info",
            notes = "Get the current detailed info for device",
            response = DeviceInfo.class
    )
    @GET
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/private/{deviceNumber}")
    public Response getDeviceDetailedInfo(@PathParam("deviceNumber") String deviceNumber) {
        try {
            Device dbDevice = this.deviceDAO.getDeviceByNumber(deviceNumber);
            if (dbDevice == null) {
                logger.error("Device {} was not found", deviceNumber);
                return Response.DEVICE_NOT_FOUND_ERROR();
            }

            final DeviceInfo deviceInfo = this.deviceInfoDAO.getDeviceInfo(dbDevice.getId());

            return Response.OK(deviceInfo);
        } catch (Exception e) {
            logger.error("Unexpected error when retrieving device info", e);
            return Response.INTERNAL_ERROR();
        }
    }

    // =================================================================================================================
    @ApiOperation(
            value = "Search devices",
            notes = "Search ",
            response = DeviceLookupItem.class,
            responseContainer = "List"
    )
    @GET
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/private/search/device")
    public Response lookupDevices(@QueryParam("filter") String filter, @QueryParam("limit") int limit) {
        try {
            final List<DeviceLookupItem> devices = this.deviceDAO.findDevices(filter, limit);
            return Response.OK(devices);
        } catch (Exception e) {
            logger.error("Unexpected error when retrieving device info", e);
            return Response.INTERNAL_ERROR();
        }
    }


    /**
     * <p>Gets the list of dynamic info records matching the specified filter.</p>
     *
     * @param filter a filter to be used for filtering the records.
     * @return a response with list of dynamic info records matching the specified filter.
     */
    @ApiOperation(
            value = "Search dynamic info",
            notes = "Gets the list of dynamic info records matching the specified filter",
            response = PaginatedData.class,
            authorizations = {@Authorization("Bearer Token")}
    )
    @POST
    @Path("/private/search/dynamic")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getPhotos(DynamicInfoFilter filter) {
        try {
            final String deviceNumber = filter.getDeviceNumber();

            Device dbDevice = this.deviceDAO.getDeviceByNumber(deviceNumber);
            if (dbDevice == null) {
                logger.error("Device {} was not found", deviceNumber);
                return Response.DEVICE_NOT_FOUND_ERROR();
            }

            filter.setDeviceId(dbDevice.getId());

            final List<DeviceDynamicInfoRecord> items = this.deviceInfoDAO.searchDynamicData(filter);
            long count = this.deviceInfoDAO.countAllDynamicData(filter);

            return Response.OK(new PaginatedData<>(items, count));
        } catch (Exception e) {
            logger.error("Unexpected error when searching for dynamic device info", e);
            return Response.INTERNAL_ERROR();
        }
    }

    /**
     * <p>Exports the devices related to selected group or configuration to Excel file and sends it back to client.</p>
     *
     * @param request the parameters of device export process.
     * @return a response to be sent to client.
     */
    @POST
    @Path("/private/export")
    @Produces(MediaType.APPLICATION_OCTET_STREAM)
    public javax.ws.rs.core.Response exportDevices(DynamicInfoExportFilter request) {
        logger.debug("Export device dynamic info request: {}", request);
        try {
            if (!SecurityContext.get().hasPermission("plugin_deviceinfo_access")) {
                if (SecurityContext.get().getCurrentUser().isPresent()) {
                    logger.error("Forbidding access to Device Info for user: {}",
                            SecurityContext.get().getCurrentUser().get().getLogin());
                } else {
                    logger.error("Forbidding access to Device Info for anonymous user");
                }
                return javax.ws.rs.core.Response.status(javax.ws.rs.core.Response.Status.FORBIDDEN).build();
            }

            Device dbDevice = this.deviceDAO.getDeviceByNumber(request.getDeviceNumber());
            if (dbDevice == null) {
                logger.error("Device {} was not found", request.getDeviceNumber());
                return javax.ws.rs.core.Response.serverError().build();
            }

            request.setDeviceId(dbDevice.getId());

            String fileName = request.getDeviceNumber();
            ContentDisposition contentDisposition = ContentDisposition.type("attachment")
                    .fileName(fileName + ".csv")
                    .creationDate(new Date())
                    .build();
            return javax.ws.rs.core.Response.ok( (StreamingOutput) output -> {
                try {
                    this.deviceInfoExportService.exportDeviceDynamicInfo(request, output);
                    output.flush();
                } catch ( Exception e ) {
                    logger.error("Unexpected error when exporting the device dynamic info to CSV format", e);
                }
            } ).header( "Content-Disposition", contentDisposition ).build();
        } catch (Exception e) {
            logger.error("Unexpected error while exporting the device dynamic info records to CSV file", e);
            return javax.ws.rs.core.Response.serverError().build();
        }
    }

}
