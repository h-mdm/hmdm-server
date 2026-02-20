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

import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import com.hmdm.persistence.UnsecureDAO;
import com.hmdm.persistence.domain.Device;
import com.hmdm.plugin.service.PluginStatusCache;
import com.hmdm.plugins.devicelog.model.DeviceLogRecord;
import com.hmdm.plugins.devicelog.persistence.DeviceLogDAO;
import com.hmdm.plugins.devicelog.rest.json.AppliedDeviceLogRule;
import com.hmdm.plugins.devicelog.rest.json.DeviceLogFilter;
import com.hmdm.plugins.devicelog.rest.json.UploadedDeviceLogRecord;
import com.hmdm.plugins.devicelog.task.InsertDeviceLogRecordsTask;
import com.hmdm.rest.json.PaginatedData;
import com.hmdm.rest.json.Response;
import com.hmdm.security.SecurityContext;
import org.glassfish.jersey.media.multipart.ContentDisposition;

import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.headers.Header;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.StreamingOutput;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicBoolean;

import static com.hmdm.plugins.devicelog.DeviceLogPluginConfigurationImpl.PLUGIN_ID;

/**
 * <p>
 * A resource to be used for accessing the data for <code>Device Log</code>
 * records.
 * </p>
 *
 * @author isv
 */
@Tag(name = "Plugin - Device Log")
@Singleton
@Path("/plugins/devicelog/log")
public class DeviceLogResource {

    // A logging service
    private static final Logger logger = LoggerFactory.getLogger(DeviceLogResource.class);

    // An executor for the log recrods upload tasks
    private final ExecutorService executor = Executors.newFixedThreadPool(5);

    /**
     * <p>
     * An interface to device log records persistence layer.
     * </p>
     */
    private DeviceLogDAO deviceLogDAO;

    private PluginStatusCache pluginStatusCache;

    /**
     * <p>
     * An interface to persistence without security checks.
     * </p>
     */
    private UnsecureDAO unsecureDAO;

    /**
     * <p>
     * A constructor required by Swagger.
     * </p>
     */
    public DeviceLogResource() {
        // Empty
    }

    /**
     * <p>
     * Constructs new <code>DeviceLogResource</code> instance. This implementation
     * does nothing.
     * </p>
     */
    @Inject
    public DeviceLogResource(DeviceLogDAO deviceLogDAO,
            PluginStatusCache pluginStatusCache,
            UnsecureDAO unsecureDAO) {
        this.deviceLogDAO = deviceLogDAO;
        this.pluginStatusCache = pluginStatusCache;
        this.unsecureDAO = unsecureDAO;
    }

    /**
     * <p>
     * Gets the list of device log records matching the specified filter.
     * </p>
     *
     * @param filter a filter to be used for filtering the records.
     * @return a response with list of device log records matching the specified
     *         filter.
     */
    @Operation(summary = "Search logs", description = "Gets the list of log records matching the specified filter")
    @POST
    @Path("/private/search")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getLogs(DeviceLogFilter filter) {
        if (!SecurityContext.get().hasPermission("plugin_devicelog_access")) {
            logger.error("Unauthorized attempt to get device logs by user {}",
                    SecurityContext.get().getCurrentUserName());
            return Response.PERMISSION_DENIED();
        }
        try {
            List<DeviceLogRecord> records = this.deviceLogDAO.findAll(filter);
            long count = this.deviceLogDAO.countAll(filter);

            return Response.OK(new PaginatedData<>(records, count));
        } catch (Exception e) {
            logger.error("Failed to search the log records due to unexpected error. Filter: {}", filter, e);
            return Response.INTERNAL_ERROR();
        }
    }

    @Operation(summary = "Exports logs", description = "Export the list of log records matching the specified filter")
    @POST
    @Path("/private/search/export")
    @Produces(MediaType.APPLICATION_JSON)
    public jakarta.ws.rs.core.Response exportLogs(DeviceLogFilter filter) {
        if (!SecurityContext.get().hasPermission("plugin_devicelog_access")) {
            logger.error("Unauthorized attempt to get device logs by user {}",
                    SecurityContext.get().getCurrentUserName());
            return jakarta.ws.rs.core.Response.serverError().status(403).build();
        }

        filter.setPageNum(1);
        filter.setExport(true);

        ContentDisposition contentDisposition = ContentDisposition.type("attachment").fileName("logs.csv")
                .creationDate(new Date()).build();

        DateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss.SSS");

        AtomicBoolean stop = new AtomicBoolean(false);

        return jakarta.ws.rs.core.Response.ok((StreamingOutput) output -> {
            try {
                List<DeviceLogRecord> records = this.deviceLogDAO.findAll(filter);
                while (!stop.get() && !records.isEmpty()) {
                    records.forEach(log -> {
                        StringBuilder b = new StringBuilder();
                        b.append(log.getDeviceNumber());
                        b.append(",");
                        b.append(dateFormat.format(new Date(log.getCreateTime())));
                        b.append(",");
                        b.append(log.getApplicationPkg());
                        b.append(",");
                        b.append(log.getSeverity());
                        b.append(",");
                        b.append(log.getMessage());
                        b.append('\n');

                        try {
                            output.write(b.toString().getBytes());
                        } catch (IOException e) {
                            logger.error(
                                    "Failed to write log record {} to output stream. Stopping to export further log records.",
                                    log, e);
                            stop.set(true);
                        }
                    });

                    output.flush();

                    if (!stop.get()) {
                        filter.setPageNum(filter.getPageNum() + 1);
                        records = this.deviceLogDAO.findAll(filter);
                    }
                }

                output.flush();
            } catch (Exception e) {
                logger.error("Failed to export the device log records due to unexpected error. Filter: {}", filter, e);
            }
        })
                .header("Cache-Control", "no-cache")
                .header("Content-Type", "text/plain")
                .header("Content-Disposition", contentDisposition)
                .build();
    }

    @Operation(summary = "Upload logs", description = "Uploads the list of log records from device to server")
    @POST
    @Path("/list/{deviceNumber}")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response uploadLogs(@PathParam("deviceNumber") String deviceNumber,
            List<UploadedDeviceLogRecord> logs,
            @Context HttpServletRequest httpRequest) {
        logger.debug("#uploadLogs: {} => {}", deviceNumber, logs);
        try {
            final Device dbDevice = this.unsecureDAO.getDeviceByNumber(deviceNumber);
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

                this.executor.submit(
                        new InsertDeviceLogRecordsTask(deviceNumber, httpRequest.getRemoteAddr(), logs,
                                this.deviceLogDAO));
                return Response.OK();
            } finally {
                SecurityContext.release();
            }
        } catch (Exception e) {
            logger.error("Unexpected error when handling uploaded log records", e);
            return Response.INTERNAL_ERROR();
        }
    }

    @Operation(summary = "Get log rules", description = "Gets the list of log rules for device")
    @GET
    @Path("/rules/{deviceNumber}")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response getDeviceLogRules(@PathParam("deviceNumber") String deviceNumber) {
        try {
            final Device dbDevice = this.unsecureDAO.getDeviceByNumber(deviceNumber);
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

                final List<AppliedDeviceLogRule> deviceLogRules = this.deviceLogDAO.getDeviceLogRules(deviceNumber);
                logger.debug("#getDeviceLogRules: {} => {}", deviceNumber, deviceLogRules);
                return Response.OK(deviceLogRules);
            } finally {
                SecurityContext.release();
            }
        } catch (Exception e) {
            logger.error("Unexpected error when handling request for device log rules", e);
            return Response.INTERNAL_ERROR();
        }
    }

}
