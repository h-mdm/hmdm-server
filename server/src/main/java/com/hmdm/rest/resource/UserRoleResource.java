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

import com.hmdm.persistence.GroupDAO;
import com.hmdm.persistence.UserDAO;
import com.hmdm.persistence.UserRoleDAO;
import com.hmdm.persistence.domain.Group;
import com.hmdm.persistence.domain.User;
import com.hmdm.persistence.domain.UserRole;
import com.hmdm.persistence.domain.UserRolePermission;
import com.hmdm.rest.json.LookupItem;
import com.hmdm.rest.json.Response;
import com.hmdm.security.SecurityContext;
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
import java.util.List;
import java.util.stream.Collectors;

@Api(tags = {"UserRole"}, authorizations = {@Authorization("Bearer Token")})
@Singleton
@Path("/private/roles")
public class UserRoleResource {
    private UserRoleDAO userRoleDAO;

    /**
     * <p>A logger to be used for logging the events.</p>
     */
    private static final Logger log = LoggerFactory.getLogger(UserRoleResource.class);

    /**
     * <p>A constructor required by Swagger.</p>
     */
    public UserRoleResource() {
    }

    @Inject
    public UserRoleResource(UserRoleDAO userRoleDAO) {
        this.userRoleDAO = userRoleDAO;
    }

    // =================================================================================================================
    @ApiOperation(
            value = "Get all permissions",
            notes = "Gets the list of all permissions",
            response = UserRolePermission.class,
            responseContainer = "List"
    )
    @GET
    @Path("/permissions")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getPermissions() {
        return Response.OK(this.userRoleDAO.getPermissionsList());
    }

    // =================================================================================================================
    @ApiOperation(
            value = "Get all roles",
            notes = "Get the list of all user roles",
            response = UserRole.class,
            responseContainer = "List"
    )
    @GET
    @Path("/all")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getAllRoles() {
        return Response.OK(this.userRoleDAO.findAll());
    }

    // =================================================================================================================
    @ApiOperation(
            value = "Create or update user role",
            notes = "Create a new user role (if id is not provided) or update existing one otherwise."
    )
    @PUT
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response updateRole(UserRole userRole) {
        if (!userRoleDAO.hasAccess()) {
            log.error("Unauthorized attempt to update a user role by user " +
                    SecurityContext.get().getCurrentUserName());
            return Response.PERMISSION_DENIED();
        }

        UserRole dbUserRole = this.userRoleDAO.findByName(userRole.getName());
        if (dbUserRole != null && !dbUserRole.getId().equals(userRole.getId())) {
            return Response.DUPLICATE_ENTITY("error.duplicate.role");
        } else {
            if (userRole.getId() == null) {
                this.userRoleDAO.insert(userRole);
            } else {
                this.userRoleDAO.update(userRole);
            }

            return Response.OK();
        }
    }

    // =================================================================================================================
    @ApiOperation(
            value = "Delete user role",
            notes = "Delete an existing user role"
    )
    @DELETE
    @Path("/{id}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response removeUserRole(@PathParam("id") @ApiParam("User role ID") Integer id) {
        if (!userRoleDAO.hasAccess()) {
            log.error("Unauthorized attempt to remove a user role by user " +
                    SecurityContext.get().getCurrentUserName());
            return Response.PERMISSION_DENIED();
        }
        this.userRoleDAO.delete(id);
        return Response.OK();
    }
}
