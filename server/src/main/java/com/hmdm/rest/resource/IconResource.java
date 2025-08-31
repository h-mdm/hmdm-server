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

import com.hmdm.persistence.IconDAO;
import com.hmdm.persistence.domain.Icon;
import com.hmdm.rest.json.Response;
import com.hmdm.security.SecurityContext;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.inject.Singleton;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import java.util.List;

/**
 * <p>A resource providing interface to icon management functionality.</p>
 *
 * @author isv
 */
@Api(tags = {"Icons"})
@Path("/private/icons")
@Singleton
public class IconResource {

    private static final Logger logger = LoggerFactory.getLogger(IconResource.class);

    private IconDAO iconDAO;

    public IconResource() {
    }

    /**
     * <p>Constructs new <code>IconResource</code> instance. This implementation does nothing.</p>
     */
    @Inject
    public IconResource(IconDAO iconDAO) {
        this.iconDAO = iconDAO;
    }

    // =================================================================================================================
    /**
     * <p>Creates new icon record on server.</p>
     *
     * @param icon the data for new icon.
     * @return a response to client containing the created icon.
     */
    @PUT
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    public Response createIcon(Icon icon) {
        try {
            final Icon newIcon = icon.getId() == null ?
                        iconDAO.insertIcon(icon) :
                        iconDAO.updateIcon(icon);
            return Response.OK(newIcon);
        } catch (Exception e) {
            return Response.INTERNAL_ERROR();
        }
    }

    // =================================================================================================================
    @GET
    @Path("/search")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    public Response getIcons() {
        try {
            final List<Icon> allIcons = this.iconDAO.getAllIcons();
            return Response.OK(allIcons);
        } catch (Exception e) {
            return Response.INTERNAL_ERROR();
        }
    }

    // =================================================================================================================
    @GET
    @Path("/search/{value}")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    public Response searchIcons(@PathParam("value") @ApiParam("A filter value") String value) {
        try {
            final List<Icon> allIcons = this.iconDAO.getAllIconsByValue(value);
            return Response.OK(allIcons);
        } catch (Exception e) {
            return Response.INTERNAL_ERROR();
        }
    }

    // =================================================================================================================
    @ApiOperation(
            value = "Delete an icon",
            notes = "Delete an existing icon"
    )
    @DELETE
    @Path("/{id}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response removeIcon(@PathParam("id") @ApiParam("Icon ID") Integer id) {
        if (!SecurityContext.get().hasPermission("settings")) {
            logger.error("Unauthorized attempt to update icons by user " +
                    SecurityContext.get().getCurrentUserName());
            return Response.PERMISSION_DENIED();
        }
        this.iconDAO.removeById(id);
        return Response.OK();
    }
}
