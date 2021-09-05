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

import com.hmdm.notification.PushService;
import com.hmdm.notification.persistence.domain.PushMessage;
import com.hmdm.persistence.DeviceDAO;
import com.hmdm.persistence.GroupDAO;
import com.hmdm.persistence.IconDAO;
import com.hmdm.persistence.domain.Device;
import com.hmdm.persistence.domain.DeviceSearchRequest;
import com.hmdm.persistence.domain.Group;
import com.hmdm.persistence.domain.Icon;
import com.hmdm.rest.json.PushRequest;
import com.hmdm.rest.json.Response;
import com.hmdm.security.SecurityContext;
import com.hmdm.security.SecurityException;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.Authorization;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.inject.Singleton;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import java.util.LinkedList;
import java.util.List;

/**
 * <p>A resource providing interface to send Push messages.</p>
 *
 * @author isv
 */
@Api(tags = {"Push API"})
@Path("/private/push")
@Singleton
public class PushApiResource {

    private static final Logger logger = LoggerFactory.getLogger(PushApiResource.class);

    /**
     * <p>An interface to notification services.</p>
     */
    private PushService pushService;

    private DeviceDAO deviceDAO;

    private GroupDAO groupDAO;

    public PushApiResource() {
    }

    /**
     * <p>Constructs new <code>PushApiResource</code> instance. This implementation does nothing.</p>
     */
    @Inject
    public PushApiResource(PushService pushService, DeviceDAO deviceDAO, GroupDAO groupDAO) {
        this.pushService = pushService;
        this.deviceDAO = deviceDAO;
        this.groupDAO = groupDAO;
    }

    private DeviceSearchRequest initDsr() {
        DeviceSearchRequest dsr = new DeviceSearchRequest();
        dsr.setPageSize(1000000); // No page limitations
        dsr.setCustomerId(SecurityContext.get().getCurrentCustomerId().get());
        dsr.setUserId(SecurityContext.get().getCurrentUser().get().getId());
        return dsr;
    }

    private void createPushMessages(DeviceSearchRequest dsr, String messageType, String payload, List<PushMessage> messages) {
        List<Device> devices = deviceDAO.getAllDevices(dsr).getItems();
        for (Device device : devices) {
            PushMessage pushMessage = new PushMessage(messageType, payload, device.getId());
            messages.add(pushMessage);
        }
    }

    /**
     * <p>Sends a Push message to devices.</p>
     *
     * @param pushRequest A command to send a Push message.
     * @return a response to client.
     */
    // =================================================================================================================
    @ApiOperation(
            value = "Send a Push message",
            notes = "Sends a Push message to specified devices.",
            authorizations = {@Authorization("Bearer Token")}
    )
    @POST
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    public Response sendPush(PushRequest pushRequest) {
        final boolean canSendMessages = SecurityContext.get().hasPermission("push_api");

        if (!canSendMessages) {
            logger.error("Unauthorized attempt to send a message",
                    SecurityException.onCustomerDataAccessViolation(0, "message"));
            return Response.PERMISSION_DENIED();
        }

        List<PushMessage> messages = new LinkedList<>();
        if (pushRequest.getBroadcast() != null && pushRequest.getBroadcast()) {
            // send to all devices, ignoring groups and devices
            DeviceSearchRequest dsr = initDsr();
            createPushMessages(dsr, pushRequest.getMessageType(), pushRequest.getPayload(), messages);
        } else {
            if (pushRequest.getGroups() != null) {
                for (String groupName : pushRequest.getGroups()) {
                    Group group = groupDAO.getGroupByName(groupName);
                    if (group != null) {
                        DeviceSearchRequest dsr = initDsr();
                        dsr.setGroupId(group.getId());
                        createPushMessages(dsr, pushRequest.getMessageType(), pushRequest.getPayload(), messages);
                    } else {
                        logger.warn("Failed to send Push message to group '" + groupName + "': group not found");
                    }
                }
            }
            if (pushRequest.getDeviceNumbers() != null) {
                for (String deviceNumber : pushRequest.getDeviceNumbers()) {
                    Device device = deviceDAO.getDeviceByNumber(deviceNumber);
                    if (device != null) {
                        PushMessage pushMessage = new PushMessage(pushRequest.getMessageType(),
                                pushRequest.getPayload(), device.getId());
                        messages.add(pushMessage);
                    } else {
                        logger.warn("Failed to send Push message to device '" + deviceNumber + "': device not found");
                    }
                }
            }
        }
        String logString = "Push message type '" + pushRequest.getMessageType() + "', payload '" + pushRequest.getPayload() +
                "' is sent to device ids: ";
        String logDevices = "";
        if (messages.size() > 0) {
            for (PushMessage pushMessage : messages) {
                if (logDevices.length() > 0) {
                    logDevices += ",";
                }
                logDevices += pushMessage.getDeviceId();
                pushService.send(pushMessage);
            }
            logger.debug(logString + logDevices);
        } else {
            logger.warn("Empty set of target devices, Push message is not sent");
        }

        return Response.OK();

    }

}
