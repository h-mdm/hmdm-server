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

import javax.inject.Inject;
import javax.inject.Singleton;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;

import com.hmdm.persistence.UserDAO;
import com.hmdm.persistence.domain.User;
import com.hmdm.rest.json.LookupItem;
import com.hmdm.security.SecurityContext;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.Authorization;
import com.hmdm.persistence.GroupDAO;
import com.hmdm.persistence.domain.Group;
import com.hmdm.rest.json.Response;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.stream.Collectors;

@Api(tags = {"Device Group"}, authorizations = {@Authorization("Bearer Token")})
@Singleton
@Path("/private/groups")
public class GroupResource {
    private GroupDAO groupDAO;
    private UserDAO userDAO;

    /**
     * <p>A logger to be used for logging the events.</p>
     */
    private static final Logger log = LoggerFactory.getLogger(GroupResource.class);

    /**
     * <p>A constructor required by Swagger.</p>
     */
    public GroupResource() {
    }

    @Inject
    public GroupResource(GroupDAO groupDAO,
                         UserDAO userDAO) {
        this.groupDAO = groupDAO;
        this.userDAO = userDAO;
    }

    // =================================================================================================================
    @ApiOperation(
            value = "Get all device groups",
            notes = "Gets the list of all available device groups",
            response = Group.class,
            responseContainer = "List"
    )
    @GET
    @Path("/search")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getAllGroups() {
        return Response.OK(this.groupDAO.getAllGroups());
    }

    // =================================================================================================================
    @ApiOperation(
            value = "Search device groups",
            notes = "Search device groups meeting the specified filter value",
            response = Group.class,
            responseContainer = "List"
    )
    @GET
    @Path("/search/{value}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response searchGroups(@PathParam("value") @ApiParam("A filter value") String value) {
        return Response.OK(this.groupDAO.getAllGroupsByValue(value));
    }


    // =================================================================================================================
    /**
     * <p>Gets the list of group id/names matching the specified filter for autocompletion.</p>
     *
     * @param filter a filter to be used for filtering the records.
     * @return a response with list of groups matching the specified filter.
     */
    @ApiOperation(value = "Get group ids/names for autocomplete")
    @POST
    @Path("/autocomplete")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getGroupsForAutocomplete(String filter) {
        try {
            List<LookupItem> groups = this.groupDAO.getAllGroupsByValue(filter)
                    .stream()
                    .map(group -> new LookupItem(group.getId(), group.getName()))
                    .collect(Collectors.toList());
            return Response.OK(groups);
        } catch (Exception e) {
            log.error("Failed to search the groups due to unexpected error. Filter: {}", filter, e);
            return Response.INTERNAL_ERROR();
        }
    }

    // =================================================================================================================
    @ApiOperation(
            value = "Create or update device group",
            notes = "Create a new device group (if id is not provided) or update existing one otherwise."
    )
    @PUT
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response updateGroup(Group group) {
        if (!SecurityContext.get().hasPermission("settings")) {
            log.error("Unauthorized attempt to update groups by user " +
                    SecurityContext.get().getCurrentUserName());
            return Response.PERMISSION_DENIED();
        }
        Group dbGroup = this.groupDAO.getGroupByName(group.getName());
        if (dbGroup != null && !dbGroup.getId().equals(group.getId())) {
            return Response.DUPLICATE_ENTITY("error.duplicate.group");
        } else {
            if (group.getId() == null) {
                this.groupDAO.insertGroup(group);
                User user = SecurityContext.get().getCurrentUser().get();
                if (!user.isAllDevicesAvailable()) {
                    // User should get permissions to view and edit a group he created
                    user.getGroups().add(new LookupItem(group.getId(), null));
                    userDAO.updateUserMainDetails(user);
                }
            } else {
                this.groupDAO.updateGroup(group);
            }

            return Response.OK();
        }
    }

    // =================================================================================================================
    @ApiOperation(
            value = "Delete device group",
            notes = "Delete an existing device group"
    )
    @DELETE
    @Path("/{id}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response removeGroup(@PathParam("id") @ApiParam("Device group ID") Integer id) {
        if (!SecurityContext.get().hasPermission("settings")) {
            log.error("Unauthorized attempt to update groups by user " +
                    SecurityContext.get().getCurrentUserName());
            return Response.PERMISSION_DENIED();
        }
        Long count = this.groupDAO.countDevicesByGroupId(id);
        if (count > 0) {
            return Response.ERROR("error.notempty.group");
        } else {
            this.groupDAO.removeGroupById(id);
            return Response.OK();
        }
    }
}
