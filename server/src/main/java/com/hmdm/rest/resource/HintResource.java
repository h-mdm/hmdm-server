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
import com.hmdm.persistence.UserDAO;
import com.hmdm.persistence.domain.Application;
import com.hmdm.rest.json.Response;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.Authorization;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import java.util.List;

/**
 * <p>A resource for tracking the status of hints shown to users.</p>
 *
 * @author isv
 */
@Api(tags = {"Hint"}, authorizations = {@Authorization("Bearer Token")})
@Singleton
@Path("/private/hints")
public class HintResource {

    private static final Logger logger = LoggerFactory.getLogger(HintResource.class);

    private UserDAO userDAO;

    /**
     * <p>A constructor required by Swagger.</p>
     */
    public HintResource() {
    }

    /**
     * <p>Constructs new <code>HintResource</code> instance. This implementation does nothing.</p>
     */
    @Inject
    public HintResource(UserDAO userDAO) {
        this.userDAO = userDAO;
    }

    // =================================================================================================================
    @ApiOperation(
            value = "Get shown hints",
            notes = "Gets the list of identifiers for the hints already presented to current user",
            response = String.class,
            responseContainer = "List"
    )
    @GET
    @Path("/history")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getShownHints() {
        try {
            List<String> shownHints  = this.userDAO.getShownHints();
            return Response.OK(shownHints);
        } catch (Exception e) {
            logger.error("Unexpected error while getting the list of hints shown to user", e);
            return Response.INTERNAL_ERROR();
        }
    }

    // =================================================================================================================
    @ApiOperation(
            value = "Enable hints",
            notes = "Enables the hints to be presented to current user",
            response = Response.class
    )
    @POST
    @Path("/enable")
    @Produces(MediaType.APPLICATION_JSON)
    public Response enableHints() {
        try {
            this.userDAO.enableHints();
            return Response.OK();
        } catch (Exception e) {
            logger.error("Unexpected error while enabling hints for user", e);
            return Response.INTERNAL_ERROR();
        }
    }

    // =================================================================================================================
    @ApiOperation(
            value = "Disable hints",
            notes = "Disables the hints from to be presented to current user",
            response = Response.class
    )
    @POST
    @Path("/disable")
    @Produces(MediaType.APPLICATION_JSON)
    public Response disableHints() {
        try {
            this.userDAO.disableHints();
            return Response.OK();
        } catch (Exception e) {
            logger.error("Unexpected error while disabling hints for user", e);
            return Response.INTERNAL_ERROR();
        }
    }

    // =================================================================================================================
    @ApiOperation(
            value = "On hint shown",
            notes = "Marks the hint as shown to current user",
            response = Response.class
    )
    @POST
    @Path("/history")
    @Produces(MediaType.APPLICATION_JSON)
    public Response markHintAsShown(String hintKey) {
        try {
            this.userDAO.onHintShown(hintKey);
            return Response.OK();
        } catch (Exception e) {
            logger.error("Unexpected error while marking the hint as shown to user", e);
            return Response.INTERNAL_ERROR();
        }
    }
}
