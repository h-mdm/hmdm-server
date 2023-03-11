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

package com.hmdm.persistence.mapper;

import com.hmdm.persistence.domain.Customer;
import com.hmdm.persistence.domain.UsageStats;
import com.hmdm.rest.json.CustomerSearchRequest;
import org.apache.ibatis.annotations.*;

import java.util.List;

/**
 * <p>An ORM mapper for {@link UsageStats} domain object.</p>
 *
 * @author isv
 */
public interface UsageStatsMapper {

    @Insert({"INSERT INTO usageStats (instanceId, webVersion, community, devicesTotal, devicesOnline, " +
             "cpuTotal, cpuUsed, ramTotal, ramUsed, scheme, arch, os) " +
             "VALUES (#{instanceId}, #{webVersion}, #{community}, #{devicesTotal}, #{devicesOnline}, " +
             "#{cpuTotal}, #{cpuUsed}, #{ramTotal}, #{ramUsed}, #{scheme}, #{arch}, #{os}) ON CONFLICT DO NOTHING"})
    @SelectKey( statement = "SELECT currval('usagestats_id_seq')", keyColumn = "id",
            keyProperty = "id", before = false, resultType = int.class )
    void insert(UsageStats usageStats);
}
