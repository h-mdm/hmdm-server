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

import com.hmdm.persistence.UserDAO;
import com.hmdm.rest.json.Response;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * <p>A resource for tracking the status of hints shown to users.</p>
 *
 * @author isv
 */
@Tag(name = "Hint")
@Singleton
@Path("/private/hints")
public class HintResource {

    private static final Logger logger = LoggerFactory.getLogger(HintResource.class);

    private UserDAO userDAO;

    /**
     * <p>A constructor required by Swagger.</p>
     */
    public HintResource() {}

    /**
     * <p>Constructs new <code>HintResource</code> instance. This implementation does nothing.</p>
     */
    @Inject
    public HintResource(UserDAO userDAO) {
        this.userDAO = userDAO;
    }

    // =================================================================================================================
    @Operation(
            summary = "Get shown hints",
            description = "Gets the list of identifiers for the hints already presented to current user")
    @GET
    @Path("/history")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getShownHints() {
        try {
            List<String> shownHints = this.userDAO.getShownHints();
            return Response.OK(shownHints);
        } catch (Exception e) {
            logger.error("Unexpected error while getting the list of hints shown to user", e);
            return Response.INTERNAL_ERROR();
        }
    }

    // =================================================================================================================
    @Operation(summary = "Enable hints", description = "Enables the hints to be presented to current user")
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
    @Operation(summary = "Disable hints", description = "Disables the hints from to be presented to current user")
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
    @Operation(summary = "On hint shown", description = "Marks the hint as shown to current user")
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
