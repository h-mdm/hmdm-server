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

package com.hmdm.plugins.devicelog;

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

import com.hmdm.plugins.devicelog.persistence.postgres.dao.mapper.PostgresDeviceLogMapper;
import com.hmdm.plugins.devicelog.rest.json.DeviceLogFilter;
import java.sql.Connection;
import java.util.List;
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
 * <p>Proves that the Device Log purge deletes only the calling customer's records, and that the log search returns only the calling customer's
 * rows.</p>
 *
 * <p>Two distinct defects, hence two groups of tests. The purge had no customer predicate at all. The search filtered on {@code devices.customerId} —
 * the <em>device's</em> owner — rather than on the log row's own denormalized {@code
 * data.customerId}, so a row whose two differ was visible to the wrong tenant.</p>
 *
 * <p><b>Every test here fails pre-fix on its own isolation assertion.</b> The purge tests are mirrors — each purges as one customer and asserts the
 * other's rows survive — so neither depends on the other having run. Controls that cannot fail pre-fix ("the caller's own expired row is deleted",
 * "the caller's in-retention row is kept", "the divergent row exists") are folded into those tests and into the fixture rather than standing as tests
 * of their own.</p>
 *
 * <p>Log rows are recreated per test. Sharing one {@code @BeforeAll} fixture across purge tests makes the pre-fix result order-dependent: whichever
 * purge runs first deletes every customer's expired rows, and a later test then sees an empty table and fails for a reason unrelated to what it
 * asserts.</p>
 *
 * <p>Manual container lifecycle rather than {@code @Testcontainers}/{@code @Container}: this branch is on JUnit 6, and that extension targets the
 * JUnit 5 extension API.</p>
 */
class TenantIsolationIT {

    /** Older than either customer's retention period in either purge test. */
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
    private static int userA;
    private static int applicationId;

    @BeforeAll
    static void startContainer() throws Exception {
        pg = newContainer();
        pg.start();
        connection = connect(pg);
        bootstrapSchema(pg);
        sessionFactory = buildSessionFactory();
        seedFixture();
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

    private static SqlSessionFactory buildSessionFactory() {
        PGSimpleDataSource dataSource = new PGSimpleDataSource();
        dataSource.setUrl(pg.getJdbcUrl());
        dataSource.setUser(pg.getUsername());
        dataSource.setPassword(pg.getPassword());

        Configuration configuration =
                new Configuration(new Environment("it", new JdbcTransactionFactory(), dataSource));
        // Aliases BEFORE the mapper. PostgresDeviceLogMapper.xml refers to types by simple name
        // (resultType="PostgresDeviceLogRecord", type="PostgresDeviceLogPluginSettings"). Production's
        // AbstractPersistenceModule adds mappers first and aliases second, which works only because the
        // mybatis-guice DSL defers resolution until the factory is built; a programmatic Configuration
        // resolves at parse time and would fail.
        configuration
                .getTypeAliasRegistry()
                .registerAliases("com.hmdm.plugins.devicelog.persistence.postgres.dao.domain");
        configuration.addMapper(PostgresDeviceLogMapper.class);
        return new SqlSessionFactoryBuilder().build(configuration);
    }

    private static void seedFixture() throws Exception {
        customerA = createCustomer(connection, "tenantA");
        customerB = createCustomer(connection, "tenantB");

        deviceA = createDevice(connection, customerA, "IT-A-0001");
        deviceB = createDevice(connection, customerB, "IT-B-0001");

        // The search joins users and requires the caller to be able to see the rows at all. Without
        // allDevicesAvailable = TRUE (or explicit group access) the access predicate filters everything
        // out on its own and masks whether data.customerId did anything. It defaults to TRUE; set
        // explicitly so the premise is visible here rather than inherited from a schema default.
        userA = scalarInt(
                connection,
                "INSERT INTO users (login, email, name, password, customerId, allDevicesAvailable) "
                        + "VALUES ('it-user-a', 'a@example.com', 'IT user A', 'x', " + customerA + ", TRUE) RETURNING id");

        // plugin_devicelog_log.applicationId is NOT NULL REFERENCES applications(id).
        applicationId = scalarInt(
                connection,
                "INSERT INTO applications (pkg, name, version) VALUES ('com.example.it', 'IT app', '1.0') RETURNING id");
    }

    /**
     * Customers, devices, the user and the application are immutable and created once; the log rows every test mutates are not.
     *
     * <p>Retention periods are set per test rather than here, because the two purge tests need them swapped.</p>
     */
    @BeforeEach
    void clearRecords() throws Exception {
        clearLogs();
    }

    private static void insertLog(int dataCustomerId, int deviceId, long createTime) throws Exception {
        exec(
                connection,
                "INSERT INTO plugin_devicelog_log (createTime, customerId, deviceId, applicationId, severity, severityOrder, message) "
                        + "VALUES (" + createTime + ", " + dataCustomerId + ", " + deviceId + ", " + applicationId
                        + ", 'INFO', 3, 'tenant isolation IT')");
    }

    private static void clearLogs() throws Exception {
        exec(connection, "DELETE FROM plugin_devicelog_log");
    }

    private static int countFor(int customerId) throws Exception {
        return scalarInt(connection, "SELECT count(*) FROM plugin_devicelog_log WHERE customerId = " + customerId);
    }

    private static int countAt(int customerId, long createTime) throws Exception {
        return scalarInt(
                connection,
                "SELECT count(*) FROM plugin_devicelog_log WHERE customerId = " + customerId + " AND createTime = " + createTime);
    }

    private static void setRetention(int customerId, int days) throws Exception {
        setRetentionDays(connection, "plugin_devicelog_settings", "logsPreservePeriod", customerId, days);
    }

    private static void purgeAs(int customerId) {
        try (SqlSession session = sessionFactory.openSession(true)) {
            session.getMapper(PostgresDeviceLogMapper.class).purgeLogRecords(customerId);
        }
    }

    private static DeviceLogFilter filterAs(int customerId, int userId) {
        DeviceLogFilter filter = new DeviceLogFilter();
        filter.setCustomerId(customerId);
        filter.setUserId(userId);
        filter.setPageNum(1);
        filter.setPageSize(50);
        filter.setSortValue("createTime");
        return filter;
    }

    // ---------------------------------------------------------------- purge

    /**
     * A keeps logs for 1 day, B for 365. Both hold a 30-day-old row, so A's cutoff catches B's row as well — pre-fix the DELETE carries no customer
     * predicate and removes it.
     */
    @Test
    void purgeAsCustomerAKeepsCustomerBsRecords() throws Exception {
        setRetention(customerA, 1);
        setRetention(customerB, 365);
        insertLog(customerA, deviceA, EXPIRED_TS);
        insertLog(customerA, deviceA, FRESH_TS);
        insertLog(customerB, deviceB, EXPIRED_TS);

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
        setRetention(customerA, 365);
        setRetention(customerB, 1);
        insertLog(customerB, deviceB, EXPIRED_TS);
        insertLog(customerB, deviceB, FRESH_TS);
        insertLog(customerA, deviceA, EXPIRED_TS);

        purgeAs(customerB);

        assertEquals(0, countAt(customerB, EXPIRED_TS), "customer B's expired record should have been purged");
        assertEquals(1, countAt(customerB, FRESH_TS), "customer B's record is inside B's own retention period and must survive");

        assertEquals(
                1,
                countAt(customerA, EXPIRED_TS),
                "customer A's record was deleted by a purge run as customer B — the purge is not tenant-scoped");
    }

    // --------------------------------------------------------------- search

    /**
     * The divergent row is what makes the search tests meaningful: its {@code data.customerId} is B while the device it references belongs to A. The
     * pre-existing predicate is on {@code devices.customerId}, so querying as A returns this row before the fix and excludes it after.
     *
     * <p>The fixture asserts the row is really there and really B's. Without that check, a future change that merely deleted the row would make every
     * search assertion below pass for the wrong reason — the count would be right because nothing existed, not because the predicate scoped it. This
     * is a fixture precondition rather than a test of its own: on its own it can never fail pre-fix, so as a {@code @Test} it would be an
     * always-green case.</p>
     */
    private static void seedDivergentRow() throws Exception {
        long recent = daysAgoMillis(0);
        insertLog(customerA, deviceA, recent); // legitimately A's
        insertLog(customerB, deviceA, recent); // B's log row against A's device — the divergent one

        assertEquals(1, countFor(customerB), "fixture: the divergent row should exist and belong to customer B");
        assertEquals(1, countFor(customerA), "fixture: customer A should own exactly its own row");
    }

    @Test
    void findAllReturnsOnlyTheCallersOwnLogRows() throws Exception {
        seedDivergentRow();

        List<?> rows;
        try (SqlSession session = sessionFactory.openSession(true)) {
            rows = session.getMapper(PostgresDeviceLogMapper.class)
                    .findAllLogRecordsByCustomerId(filterAs(customerA, userA));
        }

        assertEquals(
                1,
                rows.size(),
                "findAll returned a log row belonging to customer B because it filters on the device's owner "
                        + "rather than on the log row's own customerId");
    }

    @Test
    void countAllAgreesWithFindAll() throws Exception {
        seedDivergentRow();

        long total;
        List<?> rows;
        try (SqlSession session = sessionFactory.openSession(true)) {
            PostgresDeviceLogMapper mapper = session.getMapper(PostgresDeviceLogMapper.class);
            total = mapper.countAll(filterAs(customerA, userA));
            rows = mapper.findAllLogRecordsByCustomerId(filterAs(customerA, userA));
        }

        assertEquals(1L, total, "countAll counted customer B's log row for customer A");
        assertEquals(
                rows.size(),
                (int) total,
                "countAll and findAll disagree — the paged list and its total would be inconsistent");
    }
}
