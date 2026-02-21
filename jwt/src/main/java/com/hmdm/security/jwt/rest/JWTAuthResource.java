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

package com.hmdm.security.jwt.rest;

import com.hmdm.persistence.CustomerDAO;
import com.hmdm.persistence.UnsecureDAO;
import com.hmdm.persistence.domain.User;
import com.hmdm.rest.json.UserCredentials;
import com.hmdm.security.jwt.TokenProvider;
import com.hmdm.util.BackgroundTaskRunnerService;
import com.hmdm.util.PasswordUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * <p>A resource for authenticating the requests bearing the JWT-token.</p>
 *
 * @author isv
 */
@Singleton
@Path("/public/jwt")
@Tag(name = "Authentication")
public class JWTAuthResource {

    /**
     * <p>A name of HTTP request's "Authorization" header.</p>
     */
    private static final String AUTHORIZATION_HEADER = "Authorization";

    /**
     * <p>A logger used for logging various events encountered during the lifecycle.</p>
     */
    private final Logger log = LoggerFactory.getLogger("JWTAuth");

    private TokenProvider tokenProvider;

    private UnsecureDAO userDAO;

    private CustomerDAO customerDAO;

    private BackgroundTaskRunnerService taskRunner;

    /**
     * <p>A constructor required by Swagger.</p>
     */
    public JWTAuthResource() {}

    /**
     * <p>Constructs new <code>JWTAuthResource</code> instance. This implementation does nothing.</p>
     */
    @Inject
    public JWTAuthResource(
            TokenProvider tokenProvider,
            UnsecureDAO userDAO,
            CustomerDAO customerDAO,
            BackgroundTaskRunnerService taskRunner) {
        this.tokenProvider = tokenProvider;
        this.userDAO = userDAO;
        this.customerDAO = customerDAO;
        this.taskRunner = taskRunner;
    }

    // =================================================================================================================
    @Operation(
            summary = "Authenticate client",
            description = "Authenticates the client using provided credentials and responds with JWT token in case of "
                    + "success. The password field should contain the MD5 hash of the actual password. "
                    + "The returned JWT token must be included into 'Authorization' header for all "
                    + "sub-sequent requests from the same client.")
    @ApiResponses({
        @ApiResponse(responseCode = "400", description = "Bad request"),
        @ApiResponse(responseCode = "401", description = "Unauthorized")
    })
    @POST
    @Path("/login")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response login(UserCredentials credentials) {
        try {
            if (credentials.getLogin() == null || credentials.getPassword() == null) {
                return Response.status(Response.Status.BAD_REQUEST).build();
            }

            User user = userDAO.findByLoginOrEmail(credentials.getLogin());
            if (user == null) {
                return Response.status(Response.Status.UNAUTHORIZED).build();
            } else if (!PasswordUtil.passwordMatch(credentials.getPassword(), user.getPassword())) {
                return Response.status(Response.Status.UNAUTHORIZED).build();
            } else {
                this.taskRunner.submitTask(() -> {
                    this.customerDAO.recordLastLoginTime(user.getCustomerId(), System.currentTimeMillis());
                });

                if (user.getAuthToken() == null || user.getAuthToken().length() == 0) {
                    user.setAuthToken(PasswordUtil.generateToken());
                    user.setNewPassword(user.getPassword()); // copy value for setUserNewPasswordUnsecure
                    userDAO.setUserNewPasswordUnsecure(user);
                }
                user.setPassword(null);

                String token = tokenProvider.createToken(user, false);

                JWTToken result = new JWTToken(token);

                Response.ResponseBuilder response = Response.ok(result);
                response.header(AUTHORIZATION_HEADER, "Bearer " + token);

                return response.build();
            }
        } catch (Exception e) {
            log.error("Unexpected error when authenticating user", e);
            return Response.serverError().build();
        }
    }
}
