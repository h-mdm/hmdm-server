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

package com.hmdm.plugins.audit.rest;

import javax.inject.Inject;
import javax.inject.Singleton;
import com.hmdm.plugins.audit.persistence.AuditDAO;
import com.hmdm.plugins.audit.persistence.domain.AuditLogRecord;
import com.hmdm.plugins.audit.rest.json.AuditLogFilter;
import com.hmdm.rest.json.PaginatedData;
import com.hmdm.rest.json.Response;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.Authorization;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import java.util.List;

/**
 * <p>A resource to be used for accessing the data for <code>Audit</code> log records.</p>
 *
 * @author isv
 */
@Singleton
@Path("/plugins/audit")
@Api(tags = {"Audit"})
public class AuditResource {

    private static final Logger logger = LoggerFactory.getLogger(AuditResource.class);

    private AuditDAO auditDAO;

    /**
     * <p>A constructor required by Swagger.</p>
     */
    public AuditResource() {
    }

    /**
     * <p>Constructs new <code>AuditResource</code> instance. This implementation does nothing.</p>
     */
    @Inject
    public AuditResource(AuditDAO auditDAO) {
        this.auditDAO = auditDAO;
    }

    /**
     * <p>Gets the list of audit log records matching the specified filter.</p>
     *
     * @param filter a filter to be used for filtering the records.
     * @return a response with list of audit log records matching the specified filter.
     */
    @ApiOperation(
            value = "Search logs",
            notes = "Gets the list of audit log records matching the specified filter",
            response = PaginatedData.class,
            authorizations = {@Authorization("Bearer Token")}
    )
    @POST
    @Path("/private/log/search")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getLogs(AuditLogFilter filter) {
        try {
            List<AuditLogRecord> records = this.auditDAO.findAll(filter);
            long count = this.auditDAO.countAll(filter);

            return Response.OK(new PaginatedData<>(records, count));
        } catch (Exception e) {
            logger.error("Failed to search the audit log records due to unexpected error. Filter: {}", filter, e);
            return Response.INTERNAL_ERROR();
        }
    }

}
