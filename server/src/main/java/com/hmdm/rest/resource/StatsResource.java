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
import com.hmdm.persistence.UsageStatsDAO;
import com.hmdm.persistence.domain.Icon;
import com.hmdm.persistence.domain.UsageStats;
import com.hmdm.rest.json.Response;
import io.swagger.annotations.Api;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.inject.Singleton;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import java.util.List;

/**
 * <p>A resource providing interface to usage statistics collection functionality.</p>
 *
 * @author isv
 */
@Path("/public/stats")
@Singleton
public class StatsResource {

    private static final Logger logger = LoggerFactory.getLogger(StatsResource.class);

    private UsageStatsDAO usageStatsDAO;

    public StatsResource() {
    }

    /**
     * <p>Constructs new <code>StatsResource</code> instance. This implementation does nothing.</p>
     */
    @Inject
    public StatsResource(UsageStatsDAO usageStatsDAO) {
        this.usageStatsDAO = usageStatsDAO;
    }

    @PUT
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    public Response saveStats(UsageStats usageStats) {
        usageStatsDAO.insertUsageStats(usageStats);
        return Response.OK();
    }

}
