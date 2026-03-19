/*
 * (C) Copyright Random Enterprise Solutions 2026
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package com.randomenterprisesolutions.fhir.admin.db;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.logging.Logger;

/**
 * Manages DDL for the admin database schema.
 *
 * <p>The schema has a single table:
 * <ul>
 *   <li>{@code TENANT_CONFIG} — stores per-tenant configuration including the
 *       FHIR server config JSON and a {@code CONFIG_VERSION} counter used for
 *       cluster-wide cache invalidation.</li>
 * </ul>
 *
 * <p>Call {@link #ensureSchema(Connection)} once at application startup
 * (e.g. inside {@link JdbcTenantRepository}'s constructor) to create tables
 * idempotently.
 */
public final class TenantAdminSchema {

    private static final Logger log = Logger.getLogger(TenantAdminSchema.class.getName());

    static final String CREATE_TENANT_CONFIG =
            "CREATE TABLE TENANT_CONFIG (" +
            "  TENANT_ID          VARCHAR(64)   NOT NULL," +
            "  DISPLAY_NAME       VARCHAR(256)," +
            "  DATASTORE_TYPE     VARCHAR(32)," +
            "  DATASTORE_HOST     VARCHAR(256)," +
            "  DATASTORE_PORT     INTEGER        DEFAULT 0," +
            "  DATASTORE_DATABASE VARCHAR(256)," +
            "  FHIR_SERVER_CONFIG CLOB," +
            "  ENABLED            SMALLINT       NOT NULL DEFAULT 1," +
            "  CONFIG_VERSION     BIGINT         NOT NULL DEFAULT 0," +
            "  CREATED_AT         BIGINT         NOT NULL," +
            "  UPDATED_AT         BIGINT         NOT NULL," +
            "  CONSTRAINT PK_TENANT_CONFIG PRIMARY KEY (TENANT_ID)" +
            ")";

    private TenantAdminSchema() {
        // utility class
    }

    /**
     * Creates admin schema tables if they do not already exist.
     *
     * <p>Safe to call multiple times; existing tables are left unchanged.
     *
     * @param conn an active JDBC connection; autoCommit state is preserved
     * @throws SQLException if a DDL statement fails for any reason other than
     *                      "table already exists"
     */
    public static void ensureSchema(Connection conn) throws SQLException {
        createIfNotExists(conn, "TENANT_CONFIG", CREATE_TENANT_CONFIG);
        log.info("Admin schema ready");
    }

    // ── private helpers ──────────────────────────────────────────────────────

    private static void createIfNotExists(Connection conn, String tableName, String ddl)
            throws SQLException {
        try (Statement stmt = conn.createStatement()) {
            stmt.execute(ddl);
            log.info("Created table: " + tableName);
        } catch (SQLException ex) {
            // Derby:      X0Y32 — table/view already exists
            // PostgreSQL: 42P07 — relation already exists
            // DB2:        -601 (SQLSTATE 42710)
            String state = ex.getSQLState();
            if ("X0Y32".equals(state) || "42P07".equals(state) || "42710".equals(state)) {
                log.fine("Table already exists, skipping: " + tableName);
            } else {
                throw ex;
            }
        }
    }
}
