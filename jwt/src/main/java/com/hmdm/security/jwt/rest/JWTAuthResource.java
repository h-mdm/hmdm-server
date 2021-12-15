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

import javax.inject.Inject;
import javax.inject.Singleton;
import com.hmdm.persistence.CustomerDAO;
import com.hmdm.util.BackgroundTaskRunnerService;
import com.hmdm.util.PasswordUtil;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import io.swagger.annotations.ResponseHeader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.hmdm.persistence.UnsecureDAO;
import com.hmdm.persistence.domain.User;
import com.hmdm.rest.json.UserCredentials;
import com.hmdm.security.jwt.TokenProvider;

import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

/**
 * <p>A resource for authenticating the requests bearing the JWT-token.</p>
 *
 * @author isv
 */
@Singleton
@Path( "/public/jwt" )
@Api(tags = {"Authentication"})
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
    public JWTAuthResource() {
    }

    /**
     * <p>Constructs new <code>JWTAuthResource</code> instance. This implementation does nothing.</p>
     */
    @Inject
    public JWTAuthResource(TokenProvider tokenProvider,
                           UnsecureDAO userDAO,
                           CustomerDAO customerDAO,
                           BackgroundTaskRunnerService taskRunner) {
        this.tokenProvider = tokenProvider;
        this.userDAO = userDAO;
        this.customerDAO = customerDAO;
        this.taskRunner = taskRunner;
    }

    // =================================================================================================================
    @ApiOperation(
            value = "Authenticate client",
            notes = "Authenticates the client using provided credentials and responds with JWT token in case of " +
                    "success. The password field should contain the MD5 hash of the actual password. " +
                    "The returned JWT token must be included into 'Authorization' header for all " +
                    "sub-sequent requests from the same client.",
            response = JWTToken.class,
            responseHeaders = {@ResponseHeader(name = AUTHORIZATION_HEADER)}
    )
    @ApiResponses({
            @ApiResponse(code = 400, message = "Bad request"),
            @ApiResponse(code = 401, message = "Unauthorized")
    })
    @POST
    @Path( "/login" )
    @Consumes( MediaType.APPLICATION_JSON )
    @Produces( MediaType.APPLICATION_JSON )
    public Response login(UserCredentials credentials){
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
                    user.setNewPassword(user.getPassword());        // copy value for setUserNewPasswordUnsecure
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
