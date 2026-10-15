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

package com.hmdm.plugins;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.regex.Pattern;
import liquibase.Contexts;
import liquibase.LabelExpression;
import liquibase.Liquibase;
import liquibase.database.DatabaseFactory;
import liquibase.database.jvm.JdbcConnection;
import liquibase.resource.ClassLoaderResourceAccessor;
import org.testcontainers.postgresql.PostgreSQLContainer;
import org.testcontainers.utility.DockerImageName;

/**
 * <p>Shared harness for the tenant-isolation integration tests: one PostgreSQL container, the Liquibase bootstrap, and the small SQL helpers the
 * fixtures need.</p>
 *
 * <p>Declared once and referenced by both suites. If the two suites named the image separately they could drift apart, and would then not be testing
 * the same database.</p>
 */
public final class TenantIsolationSupport {

    /**
     * The exact image these suites run against, pinned by digest.
     *
     * <p>A floating tag is mutable — {@code postgres:16-alpine} re-points on every patch and Alpine refresh, so it pins neither the PostgreSQL patch
     * level nor the image contents. Runtime overrides must also include a full digest and must be recorded with gate evidence.</p>
     *
     * <p>Resolved from Docker Hub's library/postgres manifest for 16.4-alpine3.20 on 2026-09-16.
     * The OCI image index digest was verified against SHA-256 of the response body. This index
     * includes linux/amd64 and linux/arm64, so both architectures use the same pinned reference.</p>
     */
    public static final String PINNED_IMAGE = "postgres:16.4-alpine3.20@sha256:5660c2cbfea50c7a9127d17dc4e48543eedd3d7a41a595a2dfa572471e37e64c";

    /** The one shape {@link #PINNED_IMAGE} is allowed to take: this exact repository and tag, followed by a full 64-hex digest. */
    private static final Pattern PINNED_IMAGE_PATTERN =
            Pattern.compile("^postgres:16\\.4-alpine3\\.20@sha256:[0-9a-f]{64}$");

    /**
     * The root changelog uses three contexts: {@code common}, {@code shared} and {@code private}. {@code shared} and {@code private} are mutually
     * exclusive multi-tenancy scenarios, and the {@code shared} changeset is the one that creates the DEFAULT and ADMIN customers. Tenant-isolation
     * tests need more than one customer to exist, so the bootstrap runs {@code common,shared} — running {@code common} alone would leave a
     * single-tenant database in which these tests cannot express their premise.
     */
    private static final String CONTEXTS = "common,shared";

    /** Applied in order. Each is resolvable from the server module's test classpath. */
    private static final String[] CHANGELOGS = {
                    "liquibase/db.changelog.xml", // server
                    "liquibase/deviceinfo.changelog.xml", // deviceinfo plugin
                    "liquibase/devicelog.postgres.changelog.xml", // devicelog-postgres plugin
    };

    private TenantIsolationSupport() {}

    /**
     * Returns the runtime override or committed image, requiring the expected tag and a full digest.
     *
     * @throws IllegalStateException if the image does not specify the expected tag and a full digest
     */
    public static DockerImageName resolveImage() {
        return resolveImage(System.getProperty("hmdm.it.pgImage", PINNED_IMAGE));
    }

    static DockerImageName resolveImage(String image) {
        if (!PINNED_IMAGE_PATTERN.matcher(image).matches()) {
            throw new IllegalStateException("Unresolved PostgreSQL image: \"" + image
                    + "\". Resolve it with: docker buildx imagetools inspect postgres:16.4-alpine3.20. "
                    + "Supply -Dhmdm.it.pgImage=postgres:16.4-alpine3.20@sha256:<digest> or update PINNED_IMAGE. "
                    + "Record the resolved digest with gate evidence.");
        }
        // asCompatibleSubstituteFor: a digest-pinned reference is not recognised as the postgres image by name alone.
        return DockerImageName.parse(image).asCompatibleSubstituteFor("postgres");
    }

    /** Note: PostgreSQLContainer is NOT generic in Testcontainers 2.x, unlike the 1.x {@code PostgreSQLContainer<?>}. */
    public static PostgreSQLContainer newContainer() {
        return new PostgreSQLContainer(resolveImage());
    }

    public static Connection connect(PostgreSQLContainer pg) throws SQLException {
        return DriverManager.getConnection(pg.getJdbcUrl(), pg.getUsername(), pg.getPassword());
    }

    /**
     * Applies the root changelog and both plugin changelogs, each on its own JDBC connection.
     *
     * <p><b>The per-changelog connection is required, not tidiness.</b> {@code Liquibase.close()} cascades: {@code Liquibase.close()} →
     * {@code Database.close()} → {@code JdbcConnection.close()} → {@code java.sql.Connection.close()} (verified against the pinned liquibase-core
     * 4.33.0 bytecode). Handing the suite's shared connection to each {@link Liquibase} would therefore leave that connection closed after the first
     * changelog, and the remaining two would run against a dead one. Each instance owns a connection it is free to close.</p>
     *
     * <p>The caller's own connection is never passed in, so it survives this call and stays usable for the fixtures.</p>
     */
    public static void bootstrapSchema(PostgreSQLContainer pg) throws Exception {
        for (String changelog : CHANGELOGS) {
            // Both are closed on exit: Liquibase closes the connection via the cascade above, and the
            // explicit resource covers the case where the Liquibase constructor itself throws. A second
            // close on an already-closed JDBC connection is a no-op per the JDBC spec.
            try (Connection connection = connect(pg);
                    Liquibase liquibase = new Liquibase(
                            changelog,
                            new ClassLoaderResourceAccessor(),
                            DatabaseFactory.getInstance()
                                    .findCorrectDatabaseImplementation(new JdbcConnection(connection)))) {
                liquibase.update(new Contexts(CONTEXTS), new LabelExpression());
            }
        }
    }

    public static void exec(Connection connection, String sql) throws SQLException {
        try (Statement statement = connection.createStatement()) {
            statement.execute(sql);
        }
    }

    /** Runs a single-value query and returns the first column of the first row as an int. */
    public static int scalarInt(Connection connection, String sql) throws SQLException {
        try (Statement statement = connection.createStatement();
                ResultSet rs = statement.executeQuery(sql)) {
            if (!rs.next()) {
                throw new SQLException("query returned no rows: " + sql);
            }
            return rs.getInt(1);
        }
    }

    /**
     * Creates a customer and returns its id. {@code name} and {@code filesDir} both carry UNIQUE constraints, so they are derived from the caller's
     * label rather than fixed.
     */
    public static int createCustomer(Connection connection, String label) throws SQLException {
        return scalarInt(
                connection,
                "INSERT INTO customers (name, description, master, filesDir) VALUES ("
                        + quote(label) + ", " + quote("tenant isolation IT " + label) + ", FALSE, " + quote(label)
                        + ") RETURNING id");
    }

    /**
     * Creates a device owned by {@code customerId} and returns its id.
     *
     * <p>Device ids are globally unique primary keys and {@code devices.number} is UNIQUE, so callers must give each customer distinct device numbers
     * — overlapping ids or numbers would make the isolation assertions meaningless.</p>
     */
    public static int createDevice(Connection connection, int customerId, String number) throws SQLException {
        return scalarInt(
                connection,
                "INSERT INTO devices (number, description, lastUpdate, customerId) VALUES ("
                        + quote(number) + ", " + quote("tenant isolation IT") + ", 0, " + customerId + ") RETURNING id");
    }

    /**
     * Ensures exactly one settings row for this customer, then asserts it.
     *
     * <p>The two settings tables are asymmetric, which is why a blind insert is wrong: {@code
     * plugin_deviceinfo_settings} has UNIQUE(customerId) and is never populated by its changelog, while {@code
     * plugin_devicelog_settings} has no unique constraint and IS partly populated by one. A second insert into the latter creates a silent duplicate,
     * and the purge statements' scalar subquery then raises "more than one row returned by a subquery used as an expression" — failing identically
     * before and after the fix.</p>
     *
     * <p>Both purge queries derive their cutoff from that scalar subquery, so with no settings row at all the subquery yields NULL, {@code ts < NULL}
     * is NULL, and nothing is deleted — by the old mapper and the new one alike, so the isolation assertion would pass vacuously.</p>
     */
    public static void setRetentionDays(Connection connection, String table, String periodColumn, int customerId, int days)
            throws SQLException {
        exec(connection, "DELETE FROM " + table + " WHERE customerId = " + customerId);
        exec(
                connection,
                "INSERT INTO " + table + " (customerId, " + periodColumn + ") VALUES (" + customerId + ", " + days + ")");
        int rows = scalarInt(connection, "SELECT count(*) FROM " + table + " WHERE customerId = " + customerId);
        if (rows != 1) {
            throw new IllegalStateException(
                    "expected exactly one " + table + " row for customer " + customerId + ", found " + rows);
        }
    }

    /** Epoch milliseconds for "n days ago", matching the millisecond timestamps both plugins store. */
    public static long daysAgoMillis(int days) {
        return System.currentTimeMillis() - (days * 86_400_000L);
    }

    private static String quote(String s) {
        return "'" + s.replace("'", "''") + "'";
    }
}
