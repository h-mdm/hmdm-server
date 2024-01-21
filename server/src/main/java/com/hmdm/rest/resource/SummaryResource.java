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

import com.hmdm.persistence.DeviceDAO;
import com.hmdm.persistence.domain.DeviceSummaryRequest;
import com.hmdm.persistence.domain.SummaryConfigItem;
import com.hmdm.rest.json.Response;
import com.hmdm.rest.json.SummaryResponse;
import com.hmdm.service.DeviceApplicationsStatus;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.Authorization;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.inject.Singleton;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import java.util.Calendar;
import java.util.LinkedList;
import java.util.List;

@Api(tags = {"Summary"}, authorizations = {@Authorization("Bearer Token")})
@Singleton
@Path("/private/summary")
public class SummaryResource {

    private static final Logger log = LoggerFactory.getLogger(SummaryResource.class);

    private DeviceDAO deviceDAO;

    /**
     * <p>A constructor required by Swagger.</p>
     */
    public SummaryResource() {
    }

    @Inject
    public SummaryResource(DeviceDAO deviceDAO) {
        this.deviceDAO = deviceDAO;
    }

    // =================================================================================================================
    @ApiOperation(
            value = "Get device statistics",
            notes = "Get statistics of device enrollment",
            response = SummaryResponse.class
    )
    @GET
    @Path("/devices")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getDeviceStats() {
        SummaryResponse summaryResponse = new SummaryResponse();

        summaryResponse.setInstallSummary(deviceDAO.getInstallSummary());
        if (summaryResponse.getInstallSummary() == null) {
            log.error("Failed to get installation statistics!");
            return Response.INTERNAL_ERROR();
        }

        summaryResponse.setStatusSummary(deviceDAO.getStatusSummary());
        if (summaryResponse.getStatusSummary() == null) {
            log.error("Failed to get device status statistics!");
            return Response.INTERNAL_ERROR();
        }

        summaryResponse.setDevicesTotal(deviceDAO.getTotalDevicesCount());
        summaryResponse.setDevicesEnrolled(deviceDAO.countEnrolled(0l));
        summaryResponse.setDevicesEnrolledLastMonth(deviceDAO.countEnrolled(
                System.currentTimeMillis() - 30 * 86400 * 1000l));

        // Top 5 configs by devices
        DeviceSummaryRequest condition = new DeviceSummaryRequest();
        condition.setMinOnlineTime(0l);
        List<SummaryConfigItem> topConfigs = deviceDAO.getSummaryByConfig(condition, null);

        summaryResponse.setTopConfigs(new LinkedList<>());
        for (SummaryConfigItem summaryConfigItem : topConfigs) {
            summaryResponse.getTopConfigs().add(summaryConfigItem.getName());
        };

        // Offline devices
        long now = System.currentTimeMillis();
        condition.setMaxOnlineTime(now - 3600*1000l);
        List<SummaryConfigItem> offline = deviceDAO.getSummaryByConfig(condition, topConfigs);
        summaryResponse.setStatusOfflineByConfig(new LinkedList<>());
        for (SummaryConfigItem item : offline) {
            summaryResponse.getStatusOfflineByConfig().add(item.getCounter());
        }

        // Idle devices
        condition.setMinOnlineTime(now - 3600*4000l);
        condition.setMaxOnlineTime(now - 3600*1000l);
        List<SummaryConfigItem> idle = deviceDAO.getSummaryByConfig(condition, topConfigs);
        summaryResponse.setStatusIdleByConfig(new LinkedList<>());
        for (SummaryConfigItem item : idle) {
            summaryResponse.getStatusIdleByConfig().add(item.getCounter());
        }

        // Online devices
        condition.setMinOnlineTime(now - 3600*1000l);
        condition.setMaxOnlineTime(null);
        List<SummaryConfigItem> online = deviceDAO.getSummaryByConfig(condition, topConfigs);
        summaryResponse.setStatusOnlineByConfig(new LinkedList<>());
        for (SummaryConfigItem item : online) {
            summaryResponse.getStatusOnlineByConfig().add(item.getCounter());
        }

        // Application status: failed
        condition.setMinOnlineTime(0l);
        condition.setMaxOnlineTime(null);
        condition.setAppStatus(DeviceApplicationsStatus.FAILURE);
        List<SummaryConfigItem> failed = deviceDAO.getSummaryByConfig(condition, topConfigs);
        summaryResponse.setAppFailureByConfig(new LinkedList<>());
        for (SummaryConfigItem item : failed) {
            summaryResponse.getAppFailureByConfig().add(item.getCounter());
        }

        // Application status: mismatch
        condition.setAppStatus(DeviceApplicationsStatus.VERSION_MISMATCH);
        List<SummaryConfigItem> mismatch = deviceDAO.getSummaryByConfig(condition, topConfigs);
        summaryResponse.setAppMismatchByConfig(new LinkedList<>());
        for (SummaryConfigItem item : mismatch) {
            summaryResponse.getAppMismatchByConfig().add(item.getCounter());
        }

        // Application status: success
        condition.setAppStatus(DeviceApplicationsStatus.SUCCESS);
        List<SummaryConfigItem> success = deviceDAO.getSummaryByConfig(condition, topConfigs);
        summaryResponse.setAppSuccessByConfig(new LinkedList<>());
        for (SummaryConfigItem item : success) {
            summaryResponse.getAppSuccessByConfig().add(item.getCounter());
        }

        summaryResponse.setDevicesEnrolledMonthly(deviceDAO.getDevicesEnrolledMonthly());

        return Response.OK(summaryResponse);
    }

}
