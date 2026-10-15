package com.hmdm.plugins;

import static com.hmdm.plugins.TenantIsolationSupport.connect;
import static com.hmdm.plugins.TenantIsolationSupport.exec;
import static com.hmdm.plugins.TenantIsolationSupport.newContainer;
import static com.hmdm.plugins.TenantIsolationSupport.scalarInt;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.sql.Connection;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.xpath.XPathFactory;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.testcontainers.postgresql.PostgreSQLContainer;

class IndexGuardIT {
    private static PostgreSQLContainer pg;

    @BeforeAll
    static void startContainer() {
        pg = newContainer();
        pg.start();
    }

    @AfterAll
    static void stopContainer() {
        if (pg != null) {
            pg.stop();
        }
    }

    @ParameterizedTest
    @CsvSource(delimiter = '|', value = {
            "audit | plugin-audit-2026.06.17-idx-cust-time | plugin_audit_log | customerid,createtime",
            "deviceinfo | plugin-deviceinfo-2026.06.17-idx-device-ts | plugin_deviceinfo_deviceparams | deviceid,ts",
            "deviceinfo | plugin-deviceinfo-2026.06.17-idx-cust-ts | plugin_deviceinfo_deviceparams | customerid,ts",
            "devicelog.postgres | plugin-devicelog-2026.06.17-idx-cust-time | plugin_devicelog_log | customerid,createtime",
            "devicelog.postgres | plugin-devicelog-2026.06.17-idx-device | plugin_devicelog_log | deviceid",
            "messaging | plugin-messaging-2026.06.17-idx-cust-ts | plugin_messaging_messages | customerid,ts",
            "messaging | plugin-messaging-2026.06.17-idx-device | plugin_messaging_messages | deviceid",
            "push | plugin-push-2026.06.17-idx-cust-ts | plugin_push_messages | customerid,ts",
            "push | plugin-push-2026.06.17-idx-device | plugin_push_messages | deviceid"
    })
    void acceptsAbsentOrMatchingIndexAndReportsWrongDefinition(
            String changelog, String changeId, String table, String columns) throws Exception {
        try (var input = getClass().getClassLoader()
                .getResourceAsStream("liquibase/" + changelog + ".changelog.xml");
                Connection connection = connect(pg)) {
            assertNotNull(input);
            var factory = DocumentBuilderFactory.newInstance();
            factory.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true);
            var document = factory.newDocumentBuilder().parse(input);
            var xpath = XPathFactory.newInstance().newXPath();
            String guard = xpath.evaluate("/databaseChangeLog/changeSet[@id='" + changeId
                    + "-guard' and @author='hmdm']/preConditions/sqlCheck", document).trim();
            String createIndex = xpath.evaluate("/databaseChangeLog/changeSet[@id='" + changeId
                    + "' and @author='hmdm']/sql", document).trim();
            String index = table + "_" + columns.replace(',', '_') + "_idx";

            // Each case owns its schema; execute the actual changelog SQL against PostgreSQL.
            exec(connection, "CREATE SCHEMA index_guard_test");
            try {
                exec(connection, "SET search_path TO index_guard_test");
                exec(connection, "CREATE TABLE " + table + " ("
                        + columns.replace(",", " bigint, ") + " bigint, othercolumn bigint)");
                assertEquals(0, scalarInt(connection, guard), "Absent index");
                exec(connection, createIndex);
                assertEquals(0, scalarInt(connection, guard), "Matching index");
                exec(connection, "DROP INDEX " + index);
                exec(connection, "CREATE INDEX " + index + " ON " + table + " (othercolumn)");
                assertEquals(1, scalarInt(connection, guard), "Wrong columns");
                String[] names = columns.split(",");
                if (names.length == 2) {
                    exec(connection, "DROP INDEX " + index);
                    exec(connection, "CREATE INDEX " + index + " ON " + table
                            + " (" + names[1] + ", " + names[0] + ")");
                    assertEquals(1, scalarInt(connection, guard), "Wrong column order");
                }
            } finally {
                exec(connection, "SET search_path TO public");
                exec(connection, "DROP SCHEMA index_guard_test CASCADE");
            }
        }
    }
}
