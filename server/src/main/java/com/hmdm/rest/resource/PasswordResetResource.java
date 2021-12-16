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

import com.hmdm.persistence.CommonDAO;
import com.hmdm.persistence.UnsecureDAO;
import com.hmdm.persistence.UserDAO;
import com.hmdm.persistence.domain.Settings;
import com.hmdm.persistence.domain.User;
import com.hmdm.persistence.domain.UserRole;
import com.hmdm.rest.json.Response;
import com.hmdm.security.SecurityContext;
import com.hmdm.service.EmailService;
import com.hmdm.util.PasswordUtil;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.Authorization;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Api(tags = {"Password-Reset"})
@Singleton
@Path("/public/passwordReset")
public class PasswordResetResource {

    private static final Logger logger = LoggerFactory.getLogger(PasswordResetResource.class);

    private String baseUrl;
    private CommonDAO commonDAO;
    private UnsecureDAO unsecureDAO;
    private EmailService emailService;

    /**
     * <p>A constructor required by Swagger.</p>
     */
    public PasswordResetResource() {
    }

    /**
     * <p>Constructs new <code>PasswordResetResource</code> instance. This implementation does nothing.</p>
     */
    @Inject
    public PasswordResetResource(CommonDAO commonDAO, UnsecureDAO unsecureDAO, EmailService emailService,
                                 @Named("base.url") String baseUrl) {
        this.commonDAO = commonDAO;
        this.unsecureDAO = unsecureDAO;
        this.emailService = emailService;
        this.baseUrl = baseUrl;
    }

    // =================================================================================================================
    @ApiOperation(
            value = "Get settings by token",
            notes = "Returns the user settings by password reset token.",
            response = User.class
    )
    @GET
    @Path("/settings/{token}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getSettings(@PathParam("token") @ApiParam("Password reset token") String token) {
        try {
            User user = unsecureDAO.findByPasswordResetToken(token);
            if (user == null) {
                return Response.ERROR("error.user.not.found");
            }

            SecurityContext.init(user);
            Settings settings = Optional.ofNullable(this.commonDAO.getSettings()).orElse(new Settings());
            settings.setSingleCustomer(unsecureDAO.isSingleCustomer());
            if (!settings.isSingleCustomer()) {
                this.commonDAO.loadCustomerSettings(settings);
            }
            return Response.OK(settings);
        } catch (Exception e) {
            logger.error("Unexpected error when getting the settings for customer", e);
            return Response.INTERNAL_ERROR();
        } finally {
            SecurityContext.release();
        }
    }

    // =================================================================================================================
    @ApiOperation(
            value = "Reset password",
            notes = "Resets the user password"
    )
    @POST
    @Path("/reset")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response updatePassword(User newData) {
        try {
            User user = unsecureDAO.findByPasswordResetToken(newData.getPasswordResetToken());
            if (user == null) {
                return Response.ERROR("error.user.not.found");
            }

            user.setNewPassword(PasswordUtil.getHashFromMd5(newData.getNewPassword()));
            user.setPasswordReset(false);
            user.setPasswordResetToken(null);
            unsecureDAO.setUserNewPasswordUnsecure(user);
            return Response.OK(user);
        } catch (Exception e) {
            logger.error("Unexpected error when resetting the user password", e);
            return Response.INTERNAL_ERROR();
        } finally {
            SecurityContext.release();
        }
    }

    // =================================================================================================================
    @ApiOperation(
            value = "Password recovery feature",
            notes = "Checks if the password can be recovered."
    )
    @GET
    @Path("/canRecover")
    @Produces(MediaType.APPLICATION_JSON)
    public Response canRecover() {
        if (emailService.isConfigured()) {
            return Response.OK();
        } else {
            return Response.ERROR();
        }
    }

    // =================================================================================================================
    @ApiOperation(
            value = "Request password recovery",
            notes = "Checks if the password can be recovered."
    )
    @GET
    @Path("/recover/{username}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response recover(@PathParam("username") @ApiParam("Login of the user who wants to recover the password") String username) {
        try {
            User user = unsecureDAO.findByLoginOrEmail(username);
            if (user == null) {
                logger.warn("Can't recover password for user " + username + ": user not found");
                return Response.ERROR("error.user.not.found");
            }
            if (!emailService.isConfigured()) {
                logger.warn("Can't recover password for user " + username + ": email sender not configured");
                return Response.ERROR("error.email.not.configured");
            }
            if (user.getEmail() == null || user.getEmail().trim().equals("")) {
                logger.warn("Can't recover password for user " + username + ": no email for user");
                return Response.ERROR("error.email.not.found");
            }

            if (user.getPasswordResetToken() == null || user.getPasswordResetToken().equals("")) {
                user.setPasswordResetToken(PasswordUtil.generateToken());
                user.setNewPassword(user.getPassword());
                unsecureDAO.setUserNewPasswordUnsecure(user);
            }

            if (emailService.sendEmail(user.getEmail(), emailService.getRecoveryEmailSubj(),
                    emailService.getRecoveryEmailBody(baseUrl, user.getPasswordResetToken()))) {
                return Response.OK();
            } else {
                return Response.INTERNAL_ERROR();
            }

        } catch (Exception e) {
            logger.error("Unexpected error when requesting the password recovery for user " + username, e);
            return Response.INTERNAL_ERROR();
        }
    }
}
