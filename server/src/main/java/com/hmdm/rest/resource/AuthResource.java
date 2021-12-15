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

import com.hmdm.persistence.CustomerDAO;
import com.hmdm.persistence.UnsecureDAO;
import com.hmdm.persistence.UserDAO;
import com.hmdm.rest.json.Response;
import com.hmdm.rest.json.UserCredentials;
import com.hmdm.persistence.domain.User;
import com.hmdm.util.BackgroundTaskRunnerService;
import com.hmdm.util.PasswordUtil;

import javax.inject.Inject;
import javax.inject.Singleton;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;

/**
 * <p>A resource for authenticating the users based on provided login/password credentials.</p>
 *
 * @author isv
 */
@Singleton
@Path( "/public/auth" )
public class AuthResource {

    private final String sessionCredentials = "credentials";

    private UnsecureDAO userDAO;
    private CustomerDAO customerDAO;
    private BackgroundTaskRunnerService taskRunner;

    /**
     * <p>A constructor required by Swagger.</p>
     */
    public AuthResource() {
    }

    /**
     * <p>Constructs new <code>AuthResource</code> instance. This implementation does nothing.</p>
     */
    @Inject
    public AuthResource(UnsecureDAO userDAO, CustomerDAO customerDAO, BackgroundTaskRunnerService taskRunner) {
        this.userDAO = userDAO;
        this.customerDAO = customerDAO;
        this.taskRunner = taskRunner;
    }

    /**
     * <p>Authenticates the user based on provided credentials and responds with the user account details in case of
     * successful authentication.</p>
     *
     * @param credentials the credentials to be used for authenticating the user to application.
     * @param req an incoming request.
     * @return a response containing the details for authenticated user.
     */
    @POST
    @Path( "/login" )
    @Consumes( MediaType.APPLICATION_JSON )
    @Produces( MediaType.APPLICATION_JSON )
    public Response login( UserCredentials credentials,
                           @Context HttpServletRequest req ) throws InterruptedException {
        if ( credentials.getLogin() == null || credentials.getPassword() == null ) {
            return Response.ERROR();
        }

        User user = userDAO.findByLoginOrEmail( credentials.getLogin() );
        if ( user == null ) {
            Thread.sleep(1000);
            return Response.ERROR();
        }

        // Web app sends MD5 hash, we need to re-hash it to compare with the DB value
        if (!PasswordUtil.passwordMatch(credentials.getPassword(), user.getPassword())) {
            Thread.sleep(1000);
            return Response.ERROR();
        }

        try {
            this.taskRunner.submitTask(() -> {
                this.customerDAO.recordLastLoginTime(user.getCustomerId(), System.currentTimeMillis());
            });

            HttpSession userSession = req.getSession();
            userSession.setAttribute( sessionCredentials, user );

            if (user.getAuthToken() == null || user.getAuthToken().length() == 0) {
                user.setAuthToken(PasswordUtil.generateToken());
                user.setNewPassword(user.getPassword());        // copy value for setUserNewPasswordUnsecure
                userDAO.setUserNewPasswordUnsecure(user);
            }

            user.setPassword(null);

            return Response.OK( user );
        } catch (Exception e) {
            e.printStackTrace();
            return Response.INTERNAL_ERROR();
        }
    }

    /**
     * <p>Logs the current user out by invalidating the current session.</p>
     *
     * @param req an incoming request.
     */
    @POST
    @Path( "/logout" )
    public void logout( @Context HttpServletRequest req ) {
        HttpSession session = req.getSession( false );
        if ( session != null ) {
            session.invalidate();
        }
    }
}
