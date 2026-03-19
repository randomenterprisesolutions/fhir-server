/*
 * (C) Copyright Random Enterprise Solutions 2026
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package com.randomenterprisesolutions.fhir.admin.service;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import com.randomenterprisesolutions.fhir.admin.exception.InvalidTenantRequestException;
import com.randomenterprisesolutions.fhir.admin.exception.TenantNotFoundException;
import com.randomenterprisesolutions.fhir.admin.model.CreateTenantRequest;
import com.randomenterprisesolutions.fhir.admin.model.TenantConfig;
import com.randomenterprisesolutions.fhir.admin.model.UpdateTenantRequest;
import com.randomenterprisesolutions.fhir.admin.repository.TenantRepository;

/**
 * Unit tests for {@link TenantService}.
 *
 * <p>The repository is mocked via Mockito so these tests run without any
 * persistence infrastructure.
 */
public class TenantServiceTest {

    @Mock
    private TenantRepository repository;

    private TenantService service;
    private AutoCloseable mocks;

    @BeforeMethod
    public void setUp() {
        mocks = MockitoAnnotations.openMocks(this);
        service = new TenantService(repository);
    }

    @AfterMethod
    public void tearDown() throws Exception {
        mocks.close();
    }

    // ── createTenant ─────────────────────────────────────────────────────────

    @Test
    public void createTenant_success() {
        CreateTenantRequest req = buildCreateRequest("acme", "postgresql");

        TenantConfig stored = new TenantConfig();
        stored.setTenantId("acme");
        stored.setEnabled(true);
        when(repository.create(any())).thenReturn(stored);

        TenantConfig result = service.createTenant(req);

        assertNotNull(result);
        assertEquals(result.getTenantId(), "acme");
        verify(repository).create(any());
    }

    @Test(expectedExceptions = InvalidTenantRequestException.class,
            expectedExceptionsMessageRegExp = ".*tenantId is required.*")
    public void createTenant_nullTenantId_throwsBadRequest() {
        CreateTenantRequest req = buildCreateRequest(null, "postgresql");
        service.createTenant(req);
    }

    @Test(expectedExceptions = InvalidTenantRequestException.class,
            expectedExceptionsMessageRegExp = ".*tenantId is required.*")
    public void createTenant_blankTenantId_throwsBadRequest() {
        CreateTenantRequest req = buildCreateRequest("   ", "postgresql");
        service.createTenant(req);
    }

    @Test(expectedExceptions = InvalidTenantRequestException.class,
            expectedExceptionsMessageRegExp = ".*tenantId must match pattern.*")
    public void createTenant_invalidTenantIdChars_throwsBadRequest() {
        CreateTenantRequest req = buildCreateRequest("acme corp!", "postgresql");
        service.createTenant(req);
    }

    @Test(expectedExceptions = InvalidTenantRequestException.class,
            expectedExceptionsMessageRegExp = ".*datastoreType is required.*")
    public void createTenant_nullDatastoreType_throwsBadRequest() {
        CreateTenantRequest req = buildCreateRequest("acme", null);
        service.createTenant(req);
    }

    @Test
    public void createTenant_defaultsDisplayNameToTenantId_whenNotProvided() {
        CreateTenantRequest req = buildCreateRequest("acme", "derby");
        req.setDisplayName(null);

        TenantConfig stored = new TenantConfig();
        stored.setTenantId("acme");
        stored.setDisplayName("acme"); // service sets display name = tenantId
        when(repository.create(any())).thenAnswer(inv -> inv.getArgument(0));

        TenantConfig result = service.createTenant(req);
        assertEquals(result.getDisplayName(), "acme");
    }

    @Test
    public void createTenant_preservesExplicitDisplayName() {
        CreateTenantRequest req = buildCreateRequest("acme", "postgresql");
        req.setDisplayName("Acme Corp");

        when(repository.create(any())).thenAnswer(inv -> inv.getArgument(0));

        TenantConfig result = service.createTenant(req);
        assertEquals(result.getDisplayName(), "Acme Corp");
    }

    // ── getTenant ─────────────────────────────────────────────────────────────

    @Test
    public void getTenant_success() {
        TenantConfig cfg = buildTenantConfig("acme");
        when(repository.findById("acme")).thenReturn(Optional.of(cfg));

        TenantConfig result = service.getTenant("acme");
        assertNotNull(result);
        assertEquals(result.getTenantId(), "acme");
    }

    @Test(expectedExceptions = TenantNotFoundException.class)
    public void getTenant_notFound_throwsException() {
        when(repository.findById("unknown")).thenReturn(Optional.empty());
        service.getTenant("unknown");
    }

    // ── listTenants ───────────────────────────────────────────────────────────

    @Test
    public void listTenants_returnsAllTenants() {
        List<TenantConfig> all = Arrays.asList(
                buildTenantConfig("t1"), buildTenantConfig("t2"));
        when(repository.findAll()).thenReturn(all);

        List<TenantConfig> result = service.listTenants();
        assertEquals(result.size(), 2);
    }

    // ── updateTenant ──────────────────────────────────────────────────────────

    @Test
    public void updateTenant_updatesOnlySuppliedFields() {
        TenantConfig existing = buildTenantConfig("acme");
        existing.setDisplayName("Old Name");
        existing.setEnabled(true);
        when(repository.findById("acme")).thenReturn(Optional.of(existing));
        when(repository.update(any())).thenAnswer(inv -> inv.getArgument(0));

        UpdateTenantRequest req = new UpdateTenantRequest();
        req.setDisplayName("New Name");

        TenantConfig result = service.updateTenant("acme", req);
        assertEquals(result.getDisplayName(), "New Name");
        assertEquals(result.isEnabled(), true); // unchanged
    }

    @Test
    public void updateTenant_canDisableTenant() {
        TenantConfig existing = buildTenantConfig("acme");
        existing.setEnabled(true);
        when(repository.findById("acme")).thenReturn(Optional.of(existing));
        when(repository.update(any())).thenAnswer(inv -> inv.getArgument(0));

        UpdateTenantRequest req = new UpdateTenantRequest();
        req.setEnabled(false);

        TenantConfig result = service.updateTenant("acme", req);
        assertEquals(result.isEnabled(), false);
    }

    @Test(expectedExceptions = TenantNotFoundException.class)
    public void updateTenant_notFound_throwsException() {
        when(repository.findById("missing")).thenReturn(Optional.empty());
        service.updateTenant("missing", new UpdateTenantRequest());
    }

    // ── deleteTenant ──────────────────────────────────────────────────────────

    @Test
    public void deleteTenant_success() {
        when(repository.findById("acme")).thenReturn(Optional.of(buildTenantConfig("acme")));

        service.deleteTenant("acme");

        verify(repository).delete("acme");
    }

    @Test(expectedExceptions = TenantNotFoundException.class)
    public void deleteTenant_notFound_throwsException() {
        when(repository.findById("ghost")).thenReturn(Optional.empty());
        service.deleteTenant("ghost");
    }

    // ── reloadTenant ──────────────────────────────────────────────────────────

    @Test
    public void reloadTenant_success_noOp() {
        when(repository.findById("acme")).thenReturn(Optional.of(buildTenantConfig("acme")));
        // No exception expected; implementation is currently a no-op stub
        service.reloadTenant("acme");
    }

    @Test(expectedExceptions = TenantNotFoundException.class)
    public void reloadTenant_notFound_throwsException() {
        when(repository.findById("ghost")).thenReturn(Optional.empty());
        service.reloadTenant("ghost");
    }

    // ── helpers ──────────────────────────────────────────────────────────────

    private CreateTenantRequest buildCreateRequest(String tenantId, String datastoreType) {
        CreateTenantRequest req = new CreateTenantRequest();
        req.setTenantId(tenantId);
        req.setDatastoreType(datastoreType);
        return req;
    }

    private TenantConfig buildTenantConfig(String tenantId) {
        TenantConfig cfg = new TenantConfig();
        cfg.setTenantId(tenantId);
        cfg.setDisplayName(tenantId);
        cfg.setDatastoreType("postgresql");
        cfg.setEnabled(true);
        return cfg;
    }
}
