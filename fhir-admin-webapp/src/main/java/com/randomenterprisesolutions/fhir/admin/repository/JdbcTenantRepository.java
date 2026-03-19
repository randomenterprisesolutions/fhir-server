/*
 * (C) Copyright Random Enterprise Solutions 2026
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package com.randomenterprisesolutions.fhir.admin.repository;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.logging.Logger;

import javax.sql.DataSource;

import com.randomenterprisesolutions.fhir.admin.db.TenantAdminSchema;
import com.randomenterprisesolutions.fhir.admin.exception.TenantAlreadyExistsException;
import com.randomenterprisesolutions.fhir.admin.exception.TenantNotFoundException;
import com.randomenterprisesolutions.fhir.admin.model.TenantConfig;

/**
 * JDBC-backed implementation of {@link TenantRepository}.
 *
 * <p>Persists tenant configuration in the {@code TENANT_CONFIG} table
 * managed by {@link TenantAdminSchema}.  The {@code CONFIG_VERSION} column
 * is a monotonically increasing counter: every call to
 * {@link #bumpConfigVersion(String)} increments it by 1.  The main FHIR
 * server nodes poll this column (via {@code DatabaseAwareTenantPropertyGroupCache}
 * in {@code fhir-config}) to detect configuration changes without direct
 * service-to-service communication.
 *
 * <p>Timestamps are stored as epoch milliseconds ({@code BIGINT}) for
 * portability across Derby, PostgreSQL, and DB2.
 *
 * <p>The admin schema is created idempotently in the constructor so that
 * first-run bootstrapping requires no manual DDL step.
 */
public class JdbcTenantRepository implements TenantRepository {

    private static final Logger log = Logger.getLogger(JdbcTenantRepository.class.getName());

    // ── SQL constants ─────────────────────────────────────────────────────────

    private static final String INSERT =
            "INSERT INTO TENANT_CONFIG "
            + "(TENANT_ID, DISPLAY_NAME, DATASTORE_TYPE, DATASTORE_HOST,"
            + " DATASTORE_PORT, DATASTORE_DATABASE, FHIR_SERVER_CONFIG,"
            + " ENABLED, CONFIG_VERSION, CREATED_AT, UPDATED_AT)"
            + " VALUES (?, ?, ?, ?, ?, ?, ?, 1, 0, ?, ?)";

    private static final String SELECT_COLS =
            "TENANT_ID, DISPLAY_NAME, DATASTORE_TYPE, DATASTORE_HOST,"
            + " DATASTORE_PORT, DATASTORE_DATABASE, FHIR_SERVER_CONFIG,"
            + " ENABLED, CONFIG_VERSION, CREATED_AT, UPDATED_AT";

    private static final String SELECT_BY_ID =
            "SELECT " + SELECT_COLS + " FROM TENANT_CONFIG WHERE TENANT_ID = ?";

    private static final String SELECT_ALL =
            "SELECT " + SELECT_COLS + " FROM TENANT_CONFIG ORDER BY TENANT_ID";

    private static final String UPDATE =
            "UPDATE TENANT_CONFIG"
            + " SET DISPLAY_NAME=?, DATASTORE_TYPE=?, DATASTORE_HOST=?,"
            + " DATASTORE_PORT=?, DATASTORE_DATABASE=?, FHIR_SERVER_CONFIG=?,"
            + " ENABLED=?, UPDATED_AT=?"
            + " WHERE TENANT_ID=?";

    private static final String DELETE =
            "DELETE FROM TENANT_CONFIG WHERE TENANT_ID = ?";

    private static final String BUMP_VERSION =
            "UPDATE TENANT_CONFIG"
            + " SET CONFIG_VERSION = CONFIG_VERSION + 1, UPDATED_AT = ?"
            + " WHERE TENANT_ID = ?";

    private static final String SELECT_VERSION =
            "SELECT CONFIG_VERSION FROM TENANT_CONFIG WHERE TENANT_ID = ?";

    // ── state ─────────────────────────────────────────────────────────────────

    private final DataSource dataSource;

    // ── constructor ───────────────────────────────────────────────────────────

    /**
     * Creates a repository backed by {@code dataSource}.
     *
     * <p>The admin schema ({@code TENANT_CONFIG} table) is created
     * idempotently during construction.
     *
     * @param dataSource JDBC DataSource for the admin database
     * @throws RuntimeException if the schema cannot be initialised
     */
    public JdbcTenantRepository(DataSource dataSource) {
        this.dataSource = dataSource;
        try (Connection conn = dataSource.getConnection()) {
            TenantAdminSchema.ensureSchema(conn);
        } catch (SQLException ex) {
            throw new RuntimeException("Failed to initialise admin database schema", ex);
        }
        log.info("JdbcTenantRepository initialised");
    }

    // ── TenantRepository ──────────────────────────────────────────────────────

    @Override
    public TenantConfig create(TenantConfig config) {
        Instant now = Instant.now();
        config.setCreatedAt(now);
        config.setUpdatedAt(now);
        config.setEnabled(true);
        config.setConfigVersion(0);

        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(INSERT)) {
            ps.setString(1, config.getTenantId());
            ps.setString(2, config.getDisplayName());
            ps.setString(3, config.getDatastoreType());
            ps.setString(4, config.getDatastoreHost());
            ps.setInt(5, config.getDatastorePort());
            ps.setString(6, config.getDatastoreDatabase());
            ps.setString(7, config.getFhirServerConfig());
            ps.setLong(8, now.toEpochMilli());
            ps.setLong(9, now.toEpochMilli());
            ps.executeUpdate();
        } catch (SQLException ex) {
            if (isDuplicateKey(ex)) {
                throw new TenantAlreadyExistsException(config.getTenantId());
            }
            throw new RuntimeException("Failed to create tenant " + config.getTenantId(), ex);
        }
        log.info("Created tenant: " + config.getTenantId());
        return config;
    }

    @Override
    public Optional<TenantConfig> findById(String tenantId) {
        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(SELECT_BY_ID)) {
            ps.setString(1, tenantId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapRow(rs));
                }
                return Optional.empty();
            }
        } catch (SQLException ex) {
            throw new RuntimeException("Failed to find tenant " + tenantId, ex);
        }
    }

    @Override
    public List<TenantConfig> findAll() {
        List<TenantConfig> result = new ArrayList<>();
        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(SELECT_ALL);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                result.add(mapRow(rs));
            }
        } catch (SQLException ex) {
            throw new RuntimeException("Failed to list tenants", ex);
        }
        return Collections.unmodifiableList(result);
    }

    @Override
    public TenantConfig update(TenantConfig config) {
        Instant now = Instant.now();
        config.setUpdatedAt(now);

        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(UPDATE)) {
            ps.setString(1, config.getDisplayName());
            ps.setString(2, config.getDatastoreType());
            ps.setString(3, config.getDatastoreHost());
            ps.setInt(4, config.getDatastorePort());
            ps.setString(5, config.getDatastoreDatabase());
            ps.setString(6, config.getFhirServerConfig());
            ps.setInt(7, config.isEnabled() ? 1 : 0);
            ps.setLong(8, now.toEpochMilli());
            ps.setString(9, config.getTenantId());
            int rows = ps.executeUpdate();
            if (rows == 0) {
                throw new TenantNotFoundException(config.getTenantId());
            }
        } catch (TenantNotFoundException ex) {
            throw ex;
        } catch (SQLException ex) {
            throw new RuntimeException("Failed to update tenant " + config.getTenantId(), ex);
        }
        log.info("Updated tenant: " + config.getTenantId());
        return config;
    }

    @Override
    public void delete(String tenantId) {
        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(DELETE)) {
            ps.setString(1, tenantId);
            int rows = ps.executeUpdate();
            if (rows == 0) {
                throw new TenantNotFoundException(tenantId);
            }
        } catch (TenantNotFoundException ex) {
            throw ex;
        } catch (SQLException ex) {
            throw new RuntimeException("Failed to delete tenant " + tenantId, ex);
        }
        log.info("Deleted tenant: " + tenantId);
    }

    // ── Config-version support ─────────────────────────────────────────────────

    /**
     * Increments {@code CONFIG_VERSION} for the given tenant by 1 and returns
     * the new value.
     *
     * <p>Called by {@link com.randomenterprisesolutions.fhir.admin.service.TenantService#reloadTenant(String)}
     * to signal that FHIR server nodes should discard their cached configuration
     * for this tenant and reload from the database on the next request.
     *
     * @param tenantId the tenant to bump
     * @return the new version value after the increment
     * @throws TenantNotFoundException if the tenant does not exist
     */
    public long bumpConfigVersion(String tenantId) {
        Instant now = Instant.now();
        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(BUMP_VERSION)) {
            ps.setLong(1, now.toEpochMilli());
            ps.setString(2, tenantId);
            int rows = ps.executeUpdate();
            if (rows == 0) {
                throw new TenantNotFoundException(tenantId);
            }
        } catch (TenantNotFoundException ex) {
            throw ex;
        } catch (SQLException ex) {
            throw new RuntimeException("Failed to bump config_version for tenant " + tenantId, ex);
        }
        return getConfigVersion(tenantId);
    }

    /**
     * Returns the current {@code CONFIG_VERSION} for the given tenant.
     *
     * @param tenantId the tenant identifier
     * @return the current version, or {@code -1} if the tenant does not exist
     */
    public long getConfigVersion(String tenantId) {
        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(SELECT_VERSION)) {
            ps.setString(1, tenantId);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? rs.getLong(1) : -1;
            }
        } catch (SQLException ex) {
            throw new RuntimeException(
                    "Failed to retrieve config_version for tenant " + tenantId, ex);
        }
    }

    // ── private helpers ───────────────────────────────────────────────────────

    private TenantConfig mapRow(ResultSet rs) throws SQLException {
        TenantConfig c = new TenantConfig();
        c.setTenantId(rs.getString("TENANT_ID"));
        c.setDisplayName(rs.getString("DISPLAY_NAME"));
        c.setDatastoreType(rs.getString("DATASTORE_TYPE"));
        c.setDatastoreHost(rs.getString("DATASTORE_HOST"));
        c.setDatastorePort(rs.getInt("DATASTORE_PORT"));
        c.setDatastoreDatabase(rs.getString("DATASTORE_DATABASE"));
        c.setFhirServerConfig(rs.getString("FHIR_SERVER_CONFIG"));
        c.setEnabled(rs.getInt("ENABLED") == 1);
        c.setConfigVersion(rs.getLong("CONFIG_VERSION"));
        c.setCreatedAt(Instant.ofEpochMilli(rs.getLong("CREATED_AT")));
        c.setUpdatedAt(Instant.ofEpochMilli(rs.getLong("UPDATED_AT")));
        return c;
    }

    /** Derby: 23505, PostgreSQL: 23505, DB2: 23505 */
    private boolean isDuplicateKey(SQLException ex) {
        return "23505".equals(ex.getSQLState());
    }
}
