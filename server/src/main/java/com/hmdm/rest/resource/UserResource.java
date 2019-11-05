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
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.Authorization;
import com.hmdm.persistence.UserDAO;
import com.hmdm.persistence.domain.User;
import com.hmdm.persistence.domain.UserRole;
import com.hmdm.rest.json.Response;
import com.hmdm.security.SecurityContext;

import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Api(tags = {"User"}, authorizations = {@Authorization("Bearer Token")})
@Singleton
@Path("/private/users")
public class UserResource {

    private UserDAO userDAO;

    /**
     * <p>A constructor required by Swagger.</p>
     */
    public UserResource() {
    }

    /**
     * <p>Constructs new <code>UserResource</code> instance. This implementation does nothing.</p>
     */
    @Inject
    public UserResource(UserDAO userDAO) {
        this.userDAO = userDAO;
    }

    // =================================================================================================================
    @ApiOperation(
            value = "Get user details",
            notes = "Returns the details for the user account referenced by the specified ID.",
            response = User.class
    )
    @GET
    @Path("/{id}")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response getUserDetails(@PathParam("id") @ApiParam("User ID") int id) {
        User userDetails = userDAO.getUserDetails(id);
        userDetails.setPassword(null);

        return Response.OK(userDetails);
    }

    // =================================================================================================================
    @ApiOperation(
            value = "Get current user details",
            notes = "Returns the details for the current user account",
            response = User.class
    )
    @GET
    @Path("/current")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response getCurrentUserDetails() {
        return SecurityContext.get().getCurrentUser().map(u -> {
            User userDetails = userDAO.getUserDetails(u.getId());
            userDetails.setPassword(null);

            return Response.OK(userDetails);
        }).orElse(Response.OK(null));
    }

    // =================================================================================================================
    @ApiOperation(
            value = "List all users",
            notes = "Gets the list of all existing user accounts",
            response = User.class,
            responseContainer = "List"
    )
    @GET
    @Path("/all")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response getUsers(@QueryParam("filter") String filter) {
        return SecurityContext.get().getCurrentUser()
                .map(currentUser -> {
                    List<User> userDetails;
                    if (filter == null || filter.isEmpty()) {
                        userDetails = userDAO.findAllUsers();
                    } else {
                        userDetails = userDAO.findAllUsers("%" + filter + "%");
                    }
                    userDetails.forEach(u -> {
                        u.setPassword(null);
                        u.setEditable(!u.getId().equals(currentUser.getId()));
                    });
                    return Response.OK(userDetails);
                })
                .orElse(Response.OK(new ArrayList()));
    }

    // =================================================================================================================
    @ApiOperation(
            value = "Update password",
            notes = "Updates the password for current user",
            response = User.class
    )
    @PUT
    @Path("/current")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response updatePassword(User user) {
        User dbUser = userDAO.findByLoginOrEmail(user.getLogin());
        return updatePassword(dbUser, user);
    }

    // =================================================================================================================
    @ApiOperation(
            value = "Create or update user",
            notes = "Creates a new user account (if id is not provided) or update existing one otherwise."
    )
    @PUT
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response updateUser(User user) {
        try {
            if (user.getId() == null) {
                if (user.getNewPassword() == null) {
                    return Response.ERROR("error.password.empty");
                }
                user.setPassword(user.getNewPassword());
                user.setCustomerId(SecurityContext.get().getCurrentUser().get().getCustomerId());
                this.userDAO.insert(user);
            } else {
                this.userDAO.updateUserMainDetails(user);
                if (user.getNewPassword() != null && !user.getNewPassword().isEmpty()) {
                    user.setPassword(user.getNewPassword());
                    this.userDAO.updatePassword(user);
                }
            }

            return Response.OK();
        } catch (Exception e) {
            e.printStackTrace();
            return Response.ERROR("error.duplicate.login");
        }
    }

    // =================================================================================================================
    @ApiOperation(
            value = "Delete user",
            notes = "Deletes a user account referenced by the specified ID"
    )
    @DELETE
    @Path("/other/{id}")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response deleteUser(@PathParam("id") @ApiParam("User ID") int id) {
        try {
            userDAO.deleteUser(id);
            return Response.OK();
        } catch (Exception e) {
            return Response.ERROR(e.getMessage());
        }
    }

    // =================================================================================================================
    @ApiOperation(
            value = "List user roles",
            notes = "Gets the list of all available user roles",
            response = UserRole.class,
            responseContainer = "List"
    )
    @GET
    @Path("/roles")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response listUserRoles() {
        try {
            List<UserRole> roles = userDAO.findAllUserRoles();
            return Response.OK(roles);
        } catch (Exception e) {
            return Response.ERROR(e.getMessage());
        }
    }

    @ApiOperation(value = "", hidden = true)
    @GET
    @Path("/superadmin/all/{customerId}")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response getCustomerUsersBySuperAdmin(@PathParam("customerId") Integer customerId) {
        if (SecurityContext.get().isSuperAdmin()) {
            return Response.OK(userDAO.findAllCustomerUsers(customerId)
                    .stream()
                    .peek(user -> user.setPassword(null))
                    .collect(Collectors.toList()));
        } else {
            return Response.PERMISSION_DENIED();
        }
    }

    @ApiOperation(value = "", hidden = true)
    @PUT
    @Path("/superadmin/password")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response updatePasswordBySuperAdmin(User user) {
        if (SecurityContext.get().isSuperAdmin()) {
            userDAO.updatePasswordBySuperAdmin(user);
            return Response.OK("success.operation.completed");
        } else {
            return Response.PERMISSION_DENIED();
        }
    }

    private Response updatePassword(User dbUser, User user) {
        if (user.getNewPassword() == null || user.getOldPassword() == null || !dbUser.getPassword().equalsIgnoreCase(user.getOldPassword())) {
            return Response.ERROR("error.password.wrong");
        }

        dbUser.setPassword(user.getNewPassword());
        userDAO.updatePassword(dbUser);

        return Response.OK("success.operation.completed", dbUser);
    }


}
