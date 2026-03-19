/*
 * (C) Copyright Random Enterprise Solutions 2026
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package com.randomenterprisesolutions.fhir.admin.repository;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertTrue;

import java.util.List;
import java.util.Optional;

import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import com.randomenterprisesolutions.fhir.admin.exception.TenantAlreadyExistsException;
import com.randomenterprisesolutions.fhir.admin.exception.TenantNotFoundException;
import com.randomenterprisesolutions.fhir.admin.model.TenantConfig;

/**
 * Unit tests for {@link InMemoryTenantRepository}.
 *
 * <p>Each test gets a fresh repository instance via {@link BeforeMethod}.
 */
public class InMemoryTenantRepositoryTest {

    private InMemoryTenantRepository repository;

    @BeforeMethod
    public void setUp() {
        repository = new InMemoryTenantRepository();
    }

    // ── create ────────────────────────────────────────────────────────────────

    @Test
    public void create_storesTenantAndSetsTimestamps() {
        TenantConfig cfg = buildConfig("t1");

        TenantConfig saved = repository.create(cfg);

        assertNotNull(saved.getCreatedAt(), "createdAt should be set");
        assertNotNull(saved.getUpdatedAt(), "updatedAt should be set");
        assertTrue(saved.isEnabled(), "new tenants should be enabled by default");
        assertEquals(saved.getTenantId(), "t1");
    }

    @Test(expectedExceptions = TenantAlreadyExistsException.class)
    public void create_duplicateTenantId_throwsConflict() {
        repository.create(buildConfig("dup"));
        // second create with same ID must throw
        repository.create(buildConfig("dup"));
    }

    // ── findById ──────────────────────────────────────────────────────────────

    @Test
    public void findById_returnsStoredTenant() {
        repository.create(buildConfig("findme"));

        Optional<TenantConfig> result = repository.findById("findme");

        assertTrue(result.isPresent());
        assertEquals(result.get().getTenantId(), "findme");
    }

    @Test
    public void findById_unknownId_returnsEmpty() {
        Optional<TenantConfig> result = repository.findById("ghost");
        assertFalse(result.isPresent());
    }

    // ── findAll ───────────────────────────────────────────────────────────────

    @Test
    public void findAll_emptyStore_returnsEmptyList() {
        List<TenantConfig> all = repository.findAll();
        assertNotNull(all);
        assertTrue(all.isEmpty());
    }

    @Test
    public void findAll_returnsAllTenants() {
        repository.create(buildConfig("a1"));
        repository.create(buildConfig("a2"));
        repository.create(buildConfig("a3"));

        List<TenantConfig> all = repository.findAll();
        assertEquals(all.size(), 3);
    }

    // ── update ────────────────────────────────────────────────────────────────

    @Test
    public void update_modifiesExistingTenant() {
        repository.create(buildConfig("upd"));

        TenantConfig modified = buildConfig("upd");
        modified.setDisplayName("Updated Name");

        TenantConfig result = repository.update(modified);

        assertEquals(result.getDisplayName(), "Updated Name");
        assertNotNull(result.getUpdatedAt());
    }

    @Test(expectedExceptions = TenantNotFoundException.class)
    public void update_unknownTenant_throwsNotFound() {
        repository.update(buildConfig("missing"));
    }

    // ── delete ────────────────────────────────────────────────────────────────

    @Test
    public void delete_removesExistingTenant() {
        repository.create(buildConfig("del"));
        repository.delete("del");

        assertFalse(repository.findById("del").isPresent());
    }

    @Test(expectedExceptions = TenantNotFoundException.class)
    public void delete_unknownTenant_throwsNotFound() {
        repository.delete("ghost");
    }

    // ── helper ────────────────────────────────────────────────────────────────

    private TenantConfig buildConfig(String tenantId) {
        TenantConfig cfg = new TenantConfig();
        cfg.setTenantId(tenantId);
        cfg.setDisplayName(tenantId + " display");
        cfg.setDatastoreType("derby");
        return cfg;
    }
}
