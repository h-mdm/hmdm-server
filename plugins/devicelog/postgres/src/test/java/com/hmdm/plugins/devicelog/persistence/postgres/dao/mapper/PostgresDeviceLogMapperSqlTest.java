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

package com.hmdm.plugins.devicelog.persistence.postgres.dao.mapper;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.hmdm.plugins.devicelog.rest.json.DeviceLogFilter;
import java.util.HashMap;
import java.util.Map;
import org.apache.ibatis.mapping.BoundSql;
import org.apache.ibatis.mapping.MappedStatement;
import org.apache.ibatis.session.Configuration;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

/**
 * <p>Guards the tenant-isolation predicates on the Device Log purge and search statements.</p>
 *
 * <p>This asserts on the SQL MyBatis generates, not on database behaviour. It is the documented no-Docker substitute for the Testcontainers
 * integration test: it catches removal of a predicate, but it cannot prove isolation actually holds at runtime. The behavioural test remains the real
 * merge gate.</p>
 */
class PostgresDeviceLogMapperSqlTest {

    private static Configuration configuration;
    private static String purgeSql;
    private static int purgeParameterCount;

    @BeforeAll
    static void buildConfiguration() {
        configuration = new Configuration();
        // Aliases must be registered BEFORE the mapper is added: PostgresDeviceLogMapper.xml refers to
        // types by simple name, and a programmatic Configuration resolves them at parse time with no
        // deferral (unlike the mybatis-guice DSL used in production).
        configuration
                .getTypeAliasRegistry()
                .registerAliases("com.hmdm.plugins.devicelog.persistence.postgres.dao.domain");
        configuration.addMapper(PostgresDeviceLogMapper.class);

        MappedStatement purge =
                configuration.getMappedStatement(PostgresDeviceLogMapper.class.getName() + ".purgeLogRecords");
        Map<String, Object> parameters = new HashMap<>();
        parameters.put("customerId", 1);
        BoundSql boundSql = purge.getBoundSql(parameters);
        purgeSql = boundSql.getSql();
        purgeParameterCount = boundSql.getParameterMappings().size();
    }

    private static String selectSql(String statementId) {
        DeviceLogFilter filter = new DeviceLogFilter();
        filter.setCustomerId(1);
        filter.setUserId(1);
        return configuration
                .getMappedStatement(PostgresDeviceLogMapper.class.getName() + "." + statementId)
                .getBoundSql(filter)
                .getSql()
                .toLowerCase();
    }

    /** Everything up to the opening parenthesis of the cutoff subquery. */
    private static String purgeOuterClause() {
        int subqueryStart = purgeSql.indexOf('(');
        return (subqueryStart < 0 ? purgeSql : purgeSql.substring(0, subqueryStart)).toLowerCase();
    }

    @Test
    void purgeFiltersOnCustomerIdOutsideTheCutoffSubquery() {
        assertTrue(
                purgeOuterClause().contains("customerid"),
                "The DELETE has no customerId predicate of its own, so it deletes every customer's "
                        + "records older than this customer's cutoff. Outer clause was: " + purgeOuterClause());
    }

    @Test
    void purgeBindsCustomerIdTwice() {
        assertEquals(2, purgeParameterCount, "Expected customerId to be bound in both the outer DELETE and the subquery");
    }

    @Test
    void findAllFiltersOnTheLogRowsOwnCustomerId() {
        // devices.customerId is the device's owner; data.customerId is the log row's own tenant. A row
        // whose data.customerId differs from its device's owner is visible to the wrong tenant unless
        // both predicates are present.
        assertTrue(
                selectSql("findAllLogRecordsByCustomerId").contains("data.customerid ="),
                "findAllLogRecordsByCustomerId filters only on the device owner, not on the log row's own customerId");
    }

    @Test
    void countAllFiltersOnTheLogRowsOwnCustomerId() {
        assertTrue(
                selectSql("countAll").contains("data.customerid ="),
                "countAll filters only on the device owner, not on the log row's own customerId");
    }

    @Test
    void findAllAndCountAllAgreeOnTheirPredicates() {
        // The two statements must stay in step: a filter added to one and not the other makes the paged
        // list and its total disagree.
        String findAll = selectSql("findAllLogRecordsByCustomerId");
        String countAll = selectSql("countAll");
        assertEquals(
                findAll.split("data\\.customerid =", -1).length,
                countAll.split("data\\.customerid =", -1).length,
                "findAll and countAll disagree on the number of data.customerId predicates");
    }
}
