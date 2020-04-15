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

package com.hmdm.plugins.messaging.rest;

import com.hmdm.notification.PushService;
import com.hmdm.notification.persistence.domain.PushMessage;
import com.hmdm.persistence.DeviceDAO;
import com.hmdm.persistence.UnsecureDAO;
import com.hmdm.persistence.domain.Device;
import com.hmdm.persistence.domain.DeviceSearchRequest;
import com.hmdm.plugin.service.PluginStatusCache;
import com.hmdm.plugins.messaging.persistence.MessagingDAO;
import com.hmdm.plugins.messaging.persistence.domain.Message;
import com.hmdm.plugins.messaging.rest.json.MessageFilter;
import com.hmdm.plugins.messaging.rest.json.SendRequest;
import com.hmdm.rest.json.PaginatedData;
import com.hmdm.rest.json.Response;
import com.hmdm.security.SecurityContext;
import com.hmdm.security.SecurityException;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
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
 * <p>A resource to be used for managing the <code>Messaging</code> plugin data for customer account associated
 * with current user.</p>
 *
 * @author isv
 */
@Singleton
@Path("/plugins/messaging")
@Api(tags = {"Messaging plugin"})
public class MessagingResource {

    private static final Logger logger = LoggerFactory.getLogger(MessagingResource.class);

    /**
     * <p>An interface to message records persistence.</p>
     */
    private MessagingDAO messagingDAO;

    /**
     * <p>An interface to persistence without security checks.</p>
     */
    private UnsecureDAO unsecureDAO;

    /**
     * <p>An interface to device records persistence.</p>
     */
    private DeviceDAO deviceDAO;

    /**
     * <p>An interface to notification services.</p>
     */
    private PushService pushService;

    private PluginStatusCache pluginStatusCache;

    /**
     * <p>A constructor required by swagger.</p>
     */
    public MessagingResource() {
    }

    /**
     * <p>Constructs new <code>MessagingResource</code> instance. This implementation does nothing.</p>
     */
    @Inject
    public MessagingResource(MessagingDAO messagingDAO,
                             UnsecureDAO unsecureDAO,
                             DeviceDAO deviceDAO,
                             PushService pushService,
                             PluginStatusCache pluginStatusCache) {
        this.messagingDAO = messagingDAO;
        this.unsecureDAO = unsecureDAO;
        this.deviceDAO = deviceDAO;
        this.pushService = pushService;
        this.pluginStatusCache = pluginStatusCache;
    }

    // =================================================================================================================

    /**
     * <p>Gets the list of device log records matching the specified filter.</p>
     *
     * @param filter a filter to be used for filtering the records.
     * @return a response with list of device log records matching the specified filter.
     */
    @ApiOperation(
            value = "Search messages",
            notes = "Gets the list of message records matching the specified filter",
            response = PaginatedData.class,
            authorizations = {@Authorization("Bearer Token")}
    )
    @POST
    @Path("/private/search")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getMessages(MessageFilter filter) {
        try {
            List<Message> records = this.messagingDAO.findAll(filter);
            long count = this.messagingDAO.countAll(filter);

            return Response.OK(new PaginatedData<>(records, count));
        } catch (Exception e) {
            logger.error("Failed to search the message records due to unexpected error. Filter: {}", filter, e);
            return Response.INTERNAL_ERROR();
        }
    }

    // =================================================================================================================
    @ApiOperation(
            value = "Send new message",
            notes = "Sends a new message to a specified device.",
            authorizations = {@Authorization("Bearer Token")}
    )
    @POST
    @Path("/private/send")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response sendMessage(SendRequest sendRequest) {
        try {
            final boolean canSendMessages = SecurityContext.get().hasPermission("plugin_messaging_send");

            if (!canSendMessages) {
                logger.error("Unauthorized attempt to send a message",
                        SecurityException.onCustomerDataAccessViolation(0, "message"));
                return Response.PERMISSION_DENIED();
            }

            List<Message> messages = new LinkedList<>();

            if (sendRequest.getScope().equals("device")) {
                // Send by device number
                if (sendRequest.getDeviceNumber() != null) {
                    Message message = new Message();
                    Device device = deviceDAO.getDeviceByNumber(sendRequest.getDeviceNumber());
                    if (device == null) {
                        String error = "Attempt to send message to wrong device number " + sendRequest.getDeviceNumber();
                        logger.error(error);
                        return Response.ERROR(error);
                    }
                    message.setDeviceId(device.getId());
                    messages.add(message);
                } else {
                    String error = "Empty device number while trying to send a message!";
                    logger.error(error);
                    return Response.ERROR(error);
                }
            } else {
                DeviceSearchRequest dsr = new DeviceSearchRequest();
                dsr.setPageSize(1000000); // No page limitations
                dsr.setCustomerId(SecurityContext.get().getCurrentCustomerId().get());
                dsr.setUserId(SecurityContext.get().getCurrentUser().get().getId());
                if (sendRequest.getScope().equals("group")) {
                    if (sendRequest.getGroupId() == null || sendRequest.getGroupId() == 0) {
                        String error = "Empty group id while trying to send a message to group!";
                        logger.error(error);
                        return Response.ERROR(error);
                    }
                    dsr.setGroupId(sendRequest.getGroupId());
                }
                else if (sendRequest.getScope().equals("configuration")) {
                    if (sendRequest.getConfigurationId() == null || sendRequest.getConfigurationId() == 0) {
                        String error = "Empty configuration id while trying to send a message to configuration!";
                        logger.error(error);
                        return Response.ERROR(error);
                    }
                    dsr.setConfigurationId(sendRequest.getConfigurationId());

                }

                List<Device> devices = deviceDAO.getAllDevices(dsr).getItems();
                for (Device device : devices) {
                    Message message = new Message();
                    message.setDeviceId(device.getId());
                    messages.add(message);
                }
            }

            for (Message message : messages) {
                message.setMessage(sendRequest.getMessage());
                message.setTs(System.currentTimeMillis());
                sendSingleMessage(message);
            }

            return Response.OK();
        } catch (Exception e) {
            logger.error("Unexpected error when sending a message", e);
            return Response.ERROR();
        }
    }

    private boolean sendSingleMessage(Message message) {
         try {
             this.messagingDAO.insertMessage(message);

             PushMessage pushMessage = new PushMessage();
             pushMessage.setDeviceId(message.getDeviceId());
             pushMessage.setPayload("{id:" + message.getId() + ",text:\"" + message.getMessage().trim().replace("\"", "\\\"") + "\"}");
             pushMessage.setMessageType("textMessage");

             this.pushService.send(pushMessage);

             return true;

         } catch (Exception e) {
             logger.error("Unexpected error when sending a message to " + message.getDeviceId(), e);
             return false;
         }
    }

    // =================================================================================================================
    @ApiOperation(
            value = "Delete message",
            notes = "Delete an existing message"
    )
    @DELETE
    @Path("/{id}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response removeDevice(@PathParam("id") @ApiParam("Message ID") Integer id) {
        final boolean canSendMessages = SecurityContext.get().hasPermission("plugin_messaging_delete");

        if (!(canSendMessages)) {
            logger.error("Unauthorized attempt to delete message",
                    SecurityException.onCustomerDataAccessViolation(id, "message"));
            return Response.PERMISSION_DENIED();
        }

        this.messagingDAO.deleteMessage(id);
        return Response.OK();
    }


    // =================================================================================================================
    @ApiOperation(
            value = "Purge old messages",
            notes = "Deletes all messages older than a specified number of days.",
            authorizations = {@Authorization("Bearer Token")}
    )
    @GET
    @Path("/private/purge/{days}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response purgeMessages(@PathParam("days") Integer days) {
        try {
            final boolean canPurgeMessages = SecurityContext.get().hasPermission("plugin_messaging_delete");

            if (!canPurgeMessages) {
                logger.error("Unauthorized attempt to purge old messages",
                        SecurityException.onCustomerDataAccessViolation(0, "message"));
                return Response.PERMISSION_DENIED();
            }

            this.messagingDAO.purgeOldMessages(days);

            return Response.OK();
        } catch (Exception e) {
            logger.error("Unexpected error when purging old messages", e);
            return Response.ERROR();
        }
    }

    // =================================================================================================================
    @ApiOperation(
            value = "Sets the message status",
            notes = "Marks message as delivered or read."
    )
    @GET
    @Path("/public/status/{id}/{status}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response setMessageStatus(@PathParam("id") Integer id, @PathParam("status") Integer status) {
        if (status == null || status < 0 || status > Message.STATUS_READ) {
            logger.error("Wrong status " + status + " for message id " + id);
            return Response.ERROR();
        }
        try {
            this.messagingDAO.updateMessageStatus(id, status);
            return Response.OK();
        } catch (Exception e) {
            logger.error("Unexpected error when marking the message " + id + " as read", e);
            return Response.ERROR();
        }
    }
}
