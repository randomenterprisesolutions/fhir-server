/*
 * (C) Copyright Random Enterprise Solutions 2026
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package com.randomenterprisesolutions.fhir.config;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Reads per-tenant FHIR server configuration JSON and config-version counters
 * from the admin database written by {@code fhir-admin-webapp}.
 *
 * <p>The main FHIR server can optionally delegate tenant config loading to the
 * admin database instead of (or in addition to) on-disk
 * {@code fhir-server-config.json} files.
 *
 * <p>Activation: set the environment variable {@code FHIR_ADMIN_DB_URL} to a
 * JDBC URL that points at the admin database.  {@link FHIRConfiguration}
 * checks this variable at startup and swaps in a
 * {@link DatabaseAwareTenantPropertyGroupCache} when it is present.
 *
 * <p>This class uses only standard {@code java.sql.*} APIs.  No driver
 * dependency is declared here; the JDBC driver (Derby, PostgreSQL, …) must be
 * present on the runtime classpath.
 */
public class DatabaseTenantConfigSource {

    private static final Logger log = Logger.getLogger(DatabaseTenantConfigSource.class.getName());

    private static final String SELECT_CONFIG =
            "SELECT FHIR_SERVER_CONFIG, CONFIG_VERSION"
            + " FROM TENANT_CONFIG WHERE TENANT_ID = ?";

    private final String jdbcUrl;

    /**
     * Creates a source that connects to the admin database at {@code jdbcUrl}.
     *
     * @param jdbcUrl JDBC connection URL, e.g.
     *                {@code jdbc:derby:memory:fhir-admin;create=true} or
     *                {@code jdbc:postgresql://host/fhir_admin}
     */
    public DatabaseTenantConfigSource(String jdbcUrl) {
        this.jdbcUrl = jdbcUrl;
        log.info("DatabaseTenantConfigSource initialised with URL: " + jdbcUrl);
    }

    /**
     * Returns the {@code FHIR_SERVER_CONFIG} JSON string for the given tenant,
     * or {@code null} if the tenant is not found in the admin database or if
     * the stored value is empty.
     *
     * @param tenantId the tenant identifier
     * @return raw JSON string, or {@code null}
     */
    public String getConfigJson(String tenantId) {
        try (Connection conn = DriverManager.getConnection(jdbcUrl);
             PreparedStatement ps = conn.prepareStatement(SELECT_CONFIG)) {
            ps.setString(1, tenantId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    String json = rs.getString(1);
                    return (json != null && !json.isBlank()) ? json : null;
                }
            }
        } catch (SQLException ex) {
            log.log(Level.WARNING,
                    "Could not load config from admin DB for tenant '" + tenantId + "'"
                    + " — falling back to file-based config", ex);
        }
        return null;
    }

    /**
     * Returns the current {@code CONFIG_VERSION} for the given tenant, or
     * {@code -1} if the tenant is not found or if a database error occurs.
     *
     * <p>The FHIR server cache ({@link DatabaseAwareTenantPropertyGroupCache})
     * compares this value with the last version it read.  When the version
     * changes it discards the cached {@link PropertyGroup} and reloads from
     * the database.
     *
     * @param tenantId the tenant identifier
     * @return current version (0-based), or {@code -1}
     */
    public long getConfigVersion(String tenantId) {
        try (Connection conn = DriverManager.getConnection(jdbcUrl);
             PreparedStatement ps = conn.prepareStatement(SELECT_CONFIG)) {
            ps.setString(1, tenantId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    rs.getString(1); // skip config column
                    return rs.getLong(2);
                }
            }
        } catch (SQLException ex) {
            log.log(Level.WARNING,
                    "Could not read config_version for tenant '" + tenantId + "'", ex);
        }
        return -1;
    }
}
