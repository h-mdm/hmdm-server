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

package com.hmdm.plugins.deviceinfo;

import static com.hmdm.plugins.TenantIsolationSupport.bootstrapSchema;
import static com.hmdm.plugins.TenantIsolationSupport.connect;
import static com.hmdm.plugins.TenantIsolationSupport.createCustomer;
import static com.hmdm.plugins.TenantIsolationSupport.createDevice;
import static com.hmdm.plugins.TenantIsolationSupport.daysAgoMillis;
import static com.hmdm.plugins.TenantIsolationSupport.exec;
import static com.hmdm.plugins.TenantIsolationSupport.newContainer;
import static com.hmdm.plugins.TenantIsolationSupport.scalarInt;
import static com.hmdm.plugins.TenantIsolationSupport.setRetentionDays;
import static org.junit.jupiter.api.Assertions.assertEquals;

import com.hmdm.plugins.deviceinfo.persistence.mapper.DeviceInfoMapper;
import java.sql.Connection;
import org.apache.ibatis.mapping.Environment;
import org.apache.ibatis.session.Configuration;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.ibatis.session.SqlSessionFactoryBuilder;
import org.apache.ibatis.transaction.jdbc.JdbcTransactionFactory;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.postgresql.ds.PGSimpleDataSource;
import org.testcontainers.postgresql.PostgreSQLContainer;

/**
 * <p>Proves that the Device Info purge deletes only the calling customer's records.</p>
 *
 * <p>Purge isolation only. A Device Info <em>search</em> test is deliberately absent from this suite: it would pass both before and after the fix, so
 * it would prove nothing.</p>
 *
 * <p><b>Every test here fails pre-fix on its own isolation assertion.</b> The two tests are mirrors — each purges as one customer and asserts the
 * other's records survive — so neither depends on the other having run, and neither is a positive-control-only case. The controls that cannot fail
 * pre-fix ("the caller's own expired record is deleted", "the caller's in-retention record is kept") are folded in as extra assertions on the same
 * fixture rather than standing as tests of their own.</p>
 *
 * <p>Records are recreated per test. Sharing one {@code @BeforeAll} fixture across purge tests makes the pre-fix result order-dependent: whichever
 * purge runs first deletes every customer's expired rows, and a later test then sees an empty table and fails for a reason that has nothing to do
 * with what it asserts.</p>
 *
 * <p>Manual container lifecycle rather than {@code @Testcontainers}/{@code @Container}: this branch is on JUnit 6, and that extension targets the
 * JUnit 5 extension API.</p>
 */
class TenantIsolationIT {

    /** Older than either customer's retention period in either test. */
    private static final long EXPIRED_TS = daysAgoMillis(30);

    /** Inside the 1-day retention period, so a correctly-scoped purge must keep it. */
    private static final long FRESH_TS = daysAgoMillis(0);

    private static PostgreSQLContainer pg;
    private static Connection connection;
    private static SqlSessionFactory sessionFactory;

    private static int customerA;
    private static int customerB;
    private static int deviceA;
    private static int deviceB;

    @BeforeAll
    static void startContainer() throws Exception {
        pg = newContainer();
        pg.start();
        connection = connect(pg);
        bootstrapSchema(pg);
        sessionFactory = buildSessionFactory();

        customerA = createCustomer(connection, "tenantA");
        customerB = createCustomer(connection, "tenantB");

        // Distinct device numbers per customer: devices.number is UNIQUE and ids are global.
        deviceA = createDevice(connection, customerA, "IT-A-0001");
        deviceB = createDevice(connection, customerB, "IT-B-0001");
    }

    @AfterAll
    static void stopContainer() throws Exception {
        if (connection != null) {
            connection.close();
        }
        if (pg != null) {
            pg.stop();
        }
    }

    /** Customers and devices are immutable and stay; the records every test mutates do not. */
    @BeforeEach
    void clearRecords() throws Exception {
        exec(connection, "DELETE FROM plugin_deviceinfo_deviceParams");
    }

    private static SqlSessionFactory buildSessionFactory() {
        PGSimpleDataSource dataSource = new PGSimpleDataSource();
        dataSource.setUrl(pg.getJdbcUrl());
        dataSource.setUser(pg.getUsername());
        dataSource.setPassword(pg.getPassword());

        Configuration configuration =
                new Configuration(new Environment("it", new JdbcTransactionFactory(), dataSource));
        // Aliases before the mapper: DeviceInfoMapper.xml is parsed when the mapper is added, and a
        // programmatic Configuration resolves type names at parse time with no deferral.
        configuration.getTypeAliasRegistry().registerAliases("com.hmdm.plugins.deviceinfo.persistence.domain");
        configuration.getTypeAliasRegistry().registerAliases("com.hmdm.plugins.deviceinfo.rest.json");
        configuration.addMapper(DeviceInfoMapper.class);
        return new SqlSessionFactoryBuilder().build(configuration);
    }

    private static void insertRecord(int customerId, int deviceId, long ts) throws Exception {
        exec(
                connection,
                "INSERT INTO plugin_deviceinfo_deviceParams (deviceId, customerId, ts) VALUES (" + deviceId + ", "
                        + customerId + ", " + ts + ")");
    }

    private static int countAt(int customerId, long ts) throws Exception {
        return scalarInt(
                connection,
                "SELECT count(*) FROM plugin_deviceinfo_deviceParams WHERE customerId = " + customerId + " AND ts = " + ts);
    }

    private static void purgeAs(int customerId) {
        try (SqlSession session = sessionFactory.openSession(true)) {
            session.getMapper(DeviceInfoMapper.class).purgeDeviceInfoRecords(customerId);
        }
    }

    /**
     * A keeps data for 1 day, B for 365. Both hold a 30-day-old record, so A's cutoff catches B's record as well — pre-fix the DELETE carries no
     * customer predicate and removes it.
     */
    @Test
    void purgeAsCustomerAKeepsCustomerBsRecords() throws Exception {
        setRetentionDays(connection, "plugin_deviceinfo_settings", "dataPreservePeriod", customerA, 1);
        setRetentionDays(connection, "plugin_deviceinfo_settings", "dataPreservePeriod", customerB, 365);
        insertRecord(customerA, deviceA, EXPIRED_TS);
        insertRecord(customerA, deviceA, FRESH_TS);
        insertRecord(customerB, deviceB, EXPIRED_TS);

        purgeAs(customerA);

        // Controls: pass before and after the fix, and would catch a "fix" that purged nothing at all
        // or ignored the caller's retention period.
        assertEquals(0, countAt(customerA, EXPIRED_TS), "customer A's expired record should have been purged");
        assertEquals(1, countAt(customerA, FRESH_TS), "customer A's record is inside A's own retention period and must survive");

        // The assertion that detects the fix.
        assertEquals(
                1,
                countAt(customerB, EXPIRED_TS),
                "customer B's record was deleted by a purge run as customer A — the purge is not tenant-scoped");
    }

    /** The mirror of the above, so the fix is detected from both directions and neither test depends on the other. */
    @Test
    void purgeAsCustomerBKeepsCustomerAsRecords() throws Exception {
        setRetentionDays(connection, "plugin_deviceinfo_settings", "dataPreservePeriod", customerA, 365);
        setRetentionDays(connection, "plugin_deviceinfo_settings", "dataPreservePeriod", customerB, 1);
        insertRecord(customerB, deviceB, EXPIRED_TS);
        insertRecord(customerB, deviceB, FRESH_TS);
        insertRecord(customerA, deviceA, EXPIRED_TS);

        purgeAs(customerB);

        assertEquals(0, countAt(customerB, EXPIRED_TS), "customer B's expired record should have been purged");
        assertEquals(1, countAt(customerB, FRESH_TS), "customer B's record is inside B's own retention period and must survive");

        assertEquals(
                1,
                countAt(customerA, EXPIRED_TS),
                "customer A's record was deleted by a purge run as customer B — the purge is not tenant-scoped");
    }
}
