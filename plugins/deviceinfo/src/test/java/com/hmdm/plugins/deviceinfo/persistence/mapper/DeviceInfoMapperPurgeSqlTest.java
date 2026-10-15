/*
 * Headwind MDM: Open Source Android MDM Software https://h-mdm.com
 *
 * Copyright (C) 2019 Headwind Solutions LLC (https://h-mdm.com)
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License. You may obtain a
 * copy of the License at http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the specific language governing permissions and limitations
 * under the License.
 */

package com.hmdm.plugins.deviceinfo.persistence.mapper;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.HashMap;
import java.util.Map;
import org.apache.ibatis.mapping.BoundSql;
import org.apache.ibatis.mapping.MappedStatement;
import org.apache.ibatis.session.Configuration;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

/**
 * <p>Guards the tenant-isolation predicate on the Device Info purge statement.</p>
 *
 * <p>This asserts on the SQL MyBatis generates, not on database behaviour. It is the documented no-Docker substitute for the Testcontainers
 * integration test: it catches removal of the predicate, but it cannot prove that the purge actually leaves another customer's rows in place. The
 * behavioural test remains the real merge gate.</p>
 *
 * <p>The assertions deliberately inspect only the part of the statement <em>before</em> the cutoff subquery. The subquery has always contained
 * {@code pds.customerId = #{customerId}}, so a plain "the SQL mentions customerId" check passes both before and after the fix and proves nothing.</p>
 */
class DeviceInfoMapperPurgeSqlTest {

    private static String sql;
    private static int parameterCount;

    @BeforeAll
    static void resolvePurgeStatement() {
        Configuration configuration = new Configuration();
        configuration.addMapper(DeviceInfoMapper.class);

        MappedStatement statement =
                configuration.getMappedStatement(DeviceInfoMapper.class.getName() + ".purgeDeviceInfoRecords");

        Map<String, Object> parameters = new HashMap<>();
        parameters.put("customerId", 1);

        BoundSql boundSql = statement.getBoundSql(parameters);
        sql = boundSql.getSql();
        parameterCount = boundSql.getParameterMappings().size();
    }

    /** Everything up to the opening parenthesis of the cutoff subquery. */
    private static String outerClause() {
        int subqueryStart = sql.indexOf('(');
        return (subqueryStart < 0 ? sql : sql.substring(0, subqueryStart)).toLowerCase();
    }

    @Test
    void purgeFiltersOnCustomerIdOutsideTheCutoffSubquery() {
        assertTrue(
                outerClause().contains("customerid"),
                "The DELETE has no customerId predicate of its own, so it deletes every customer's "
                        + "records older than this customer's cutoff. Outer clause was: " + outerClause());
    }

    @Test
    void purgeBindsCustomerIdTwice() {
        // Once in the DELETE's own WHERE, once in the cutoff subquery. One binding means the outer
        // predicate is missing.
        assertEquals(2, parameterCount, "Expected customerId to be bound in both the outer DELETE and the subquery");
    }

    @Test
    void purgeTargetsOnlyTheDeviceParamsTable() {
        assertTrue(sql.toLowerCase().contains("delete from plugin_deviceinfo_deviceparams"), "Unexpected purge target: " + sql);
    }
}
