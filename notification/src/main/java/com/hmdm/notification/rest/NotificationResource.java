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

package com.hmdm.notification.rest;

import javax.inject.Inject;
import javax.inject.Singleton;
import com.hmdm.notification.persistence.NotificationDAO;
import com.hmdm.notification.persistence.domain.PushMessage;
import com.hmdm.notification.rest.json.PlainPushMessage;
import com.hmdm.persistence.UnsecureDAO;
import com.hmdm.persistence.domain.Device;
import com.hmdm.rest.json.Response;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.Authorization;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import java.util.List;
import java.util.stream.Collectors;

/**
 * <p>A resource to be used for publishing/receiving the notification messages.</p>
 *
 * @author isv
 */
@Api(tags = {"Notifications"})
@Singleton
@Path("/notifications")
public class NotificationResource {

    private static final Logger log = LoggerFactory.getLogger(NotificationResource.class);
    private UnsecureDAO unsecureDAO;
    private NotificationDAO notificationDAO;

    /**
     * <p>A constructor required by Swagger.</p>
     */
    public NotificationResource() {
    }

    /**
     * <p>Constructs new <code>NotificationResource</code> instance. This implementation does nothing.</p>
     */
    @Inject
    public NotificationResource(UnsecureDAO unsecureDAO, NotificationDAO notificationDAO) {
        this.unsecureDAO = unsecureDAO;
        this.notificationDAO = notificationDAO;
    }

    // =================================================================================================================
    @ApiOperation(
            value = "Get device notifications",
            notes = "Gets the notifications for device from the MDM server.",
            response = PlainPushMessage.class,
            responseContainer = "List"
    )
    @Path("/device/{deviceId}")
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Response getPushMessages(@PathParam("deviceId")
                                        @ApiParam("An identifier of device within MDM server")
                                                String deviceId) {
        log.debug("#getPushMessages: deviceId = {}", deviceId);
        try {
            Device dbDevice = this.unsecureDAO.getDeviceByNumber(deviceId);
            if (dbDevice == null) {
                dbDevice = this.unsecureDAO.getDeviceByOldNumber(deviceId);
            }
            if (dbDevice != null) {
                List<PushMessage> messages = this.notificationDAO.getPendingMessagesForDelivery(deviceId);
                log.info("Delivering push-messages to device '{}': {}", deviceId, messages);

                final List<PlainPushMessage> messagesToDeliver
                        = messages.stream().map(PlainPushMessage::new).collect(Collectors.toList());
                return Response.OK(messagesToDeliver);
            } else {
                return Response.DEVICE_NOT_FOUND_ERROR();
            }
        } catch (Exception e) {
            log.error("Unexpected error when querying for pending messages for device: {}", deviceId, e);
            return Response.INTERNAL_ERROR();
        }
    }

}
