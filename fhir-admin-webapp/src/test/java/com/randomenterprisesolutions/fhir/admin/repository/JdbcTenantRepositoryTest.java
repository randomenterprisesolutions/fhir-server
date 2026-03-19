/*
 * (C) Copyright Random Enterprise Solutions 2026
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package com.randomenterprisesolutions.fhir.admin.repository;

import java.util.List;
import java.util.Optional;

import javax.sql.DataSource;

import org.apache.derby.jdbc.EmbeddedDataSource;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import com.randomenterprisesolutions.fhir.admin.exception.TenantAlreadyExistsException;
import com.randomenterprisesolutions.fhir.admin.exception.TenantNotFoundException;
import com.randomenterprisesolutions.fhir.admin.model.TenantConfig;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertTrue;

/**
 * Unit tests for {@link JdbcTenantRepository} backed by an in-memory Derby database.
 */
public class JdbcTenantRepositoryTest {

    private static final String DB_NAME = "memory:fhir-admin-repo-unit";

    private JdbcTenantRepository repo;

    @BeforeClass
    public void setUp() {
        EmbeddedDataSource ds = new EmbeddedDataSource();
        ds.setDatabaseName(DB_NAME);
        ds.setCreateDatabase("create");
        repo = new JdbcTenantRepository(ds);
    }

    @AfterClass
    public void tearDown() {
        // Shut down the in-memory Derby instance cleanly
        try {
            EmbeddedDataSource shutdown = new EmbeddedDataSource();
            shutdown.setDatabaseName(DB_NAME);
            shutdown.setConnectionAttributes("drop=true");
            shutdown.getConnection();
        } catch (Exception ignored) {
            // Derby always throws an exception on drop — that is expected
        }
    }

    // ── create ────────────────────────────────────────────────────────────────

    @Test
    public void create_persists_and_returns_tenant() {
        TenantConfig cfg = buildConfig("repo-create", "postgresql");
        TenantConfig saved = repo.create(cfg);

        assertNotNull(saved, "create should return the stored config");
        assertEquals(saved.getTenantId(), "repo-create");
        assertTrue(saved.isEnabled(), "new tenant should be enabled");
        assertEquals(saved.getConfigVersion(), 0, "new tenant should start at version 0");
        assertNotNull(saved.getCreatedAt());
        assertNotNull(saved.getUpdatedAt());
    }

    @Test(dependsOnMethods = "create_persists_and_returns_tenant",
          expectedExceptions = TenantAlreadyExistsException.class)
    public void create_duplicate_throws_TenantAlreadyExistsException() {
        repo.create(buildConfig("repo-create", "derby"));
    }

    // ── findById ─────────────────────────────────────────────────────────────

    @Test
    public void findById_returns_created_tenant() {
        repo.create(buildConfig("repo-find", "derby"));

        Optional<TenantConfig> found = repo.findById("repo-find");
        assertTrue(found.isPresent());
        assertEquals(found.get().getDatastoreType(), "derby");
    }

    @Test
    public void findById_returns_empty_for_unknown_id() {
        Optional<TenantConfig> found = repo.findById("no-such-tenant-xyz");
        assertFalse(found.isPresent());
    }

    // ── findAll ───────────────────────────────────────────────────────────────

    @Test
    public void findAll_includes_created_tenants() {
        repo.create(buildConfig("repo-list-a", "db2"));
        repo.create(buildConfig("repo-list-b", "mssql"));

        List<TenantConfig> all = repo.findAll();
        assertTrue(all.stream().anyMatch(t -> "repo-list-a".equals(t.getTenantId())));
        assertTrue(all.stream().anyMatch(t -> "repo-list-b".equals(t.getTenantId())));
    }

    // ── update ────────────────────────────────────────────────────────────────

    @Test
    public void update_persists_changes() {
        repo.create(buildConfig("repo-update", "derby"));
        TenantConfig toUpdate = repo.findById("repo-update").orElseThrow();
        toUpdate.setDisplayName("Updated Name");
        toUpdate.setEnabled(false);

        TenantConfig updated = repo.update(toUpdate);
        assertEquals(updated.getDisplayName(), "Updated Name");
        assertFalse(updated.isEnabled());

        // Re-read to confirm durability
        TenantConfig reloaded = repo.findById("repo-update").orElseThrow();
        assertEquals(reloaded.getDisplayName(), "Updated Name");
    }

    @Test(expectedExceptions = TenantNotFoundException.class)
    public void update_nonexistent_throws_TenantNotFoundException() {
        TenantConfig ghost = buildConfig("ghost-update", "derby");
        repo.update(ghost);
    }

    // ── delete ────────────────────────────────────────────────────────────────

    @Test
    public void delete_removes_tenant() {
        repo.create(buildConfig("repo-delete", "postgresql"));
        repo.delete("repo-delete");
        assertFalse(repo.findById("repo-delete").isPresent());
    }

    @Test(expectedExceptions = TenantNotFoundException.class)
    public void delete_nonexistent_throws_TenantNotFoundException() {
        repo.delete("no-such-tenant-delete");
    }

    // ── config_version ────────────────────────────────────────────────────────

    @Test
    public void initial_config_version_is_zero() {
        repo.create(buildConfig("repo-version", "derby"));
        assertEquals(repo.getConfigVersion("repo-version"), 0);
    }

    @Test(dependsOnMethods = "initial_config_version_is_zero")
    public void bumpConfigVersion_increments_by_one() {
        long v1 = repo.bumpConfigVersion("repo-version");
        assertEquals(v1, 1);

        long v2 = repo.bumpConfigVersion("repo-version");
        assertEquals(v2, 2);
    }

    @Test
    public void getConfigVersion_returns_minus_one_for_unknown_tenant() {
        assertEquals(repo.getConfigVersion("never-created"), -1);
    }

    @Test(expectedExceptions = TenantNotFoundException.class)
    public void bumpConfigVersion_nonexistent_throws_TenantNotFoundException() {
        repo.bumpConfigVersion("ghost-version");
    }

    // ── fhirServerConfig round-trip ───────────────────────────────────────────

    @Test
    public void fhirServerConfig_roundtrip_preserves_json() {
        String sampleJson = "{\"fhirServer\":{\"core\":{\"defaultTenantId\":\"sample\"}}}";
        TenantConfig cfg = buildConfig("repo-config-json", "derby");
        cfg.setFhirServerConfig(sampleJson);
        repo.create(cfg);

        String loaded = repo.findById("repo-config-json").orElseThrow().getFhirServerConfig();
        assertEquals(loaded, sampleJson);
    }

    // ── helper ────────────────────────────────────────────────────────────────

    private TenantConfig buildConfig(String tenantId, String datastoreType) {
        TenantConfig c = new TenantConfig();
        c.setTenantId(tenantId);
        c.setDisplayName(tenantId + " display");
        c.setDatastoreType(datastoreType);
        c.setDatastoreHost("localhost");
        c.setDatastorePort(5432);
        c.setDatastoreDatabase(tenantId + "_db");
        return c;
    }
}
