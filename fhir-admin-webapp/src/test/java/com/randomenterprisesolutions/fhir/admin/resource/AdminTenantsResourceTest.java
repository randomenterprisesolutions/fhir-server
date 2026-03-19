/*
 * (C) Copyright Random Enterprise Solutions 2026
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package com.randomenterprisesolutions.fhir.admin.resource;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;

import java.lang.reflect.Field;
import java.net.URI;
import java.util.Arrays;
import java.util.List;

import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.core.UriBuilder;
import javax.ws.rs.core.UriInfo;

import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import com.randomenterprisesolutions.fhir.admin.exception.TenantNotFoundException;
import com.randomenterprisesolutions.fhir.admin.model.CreateTenantRequest;
import com.randomenterprisesolutions.fhir.admin.model.TenantConfig;
import com.randomenterprisesolutions.fhir.admin.model.UpdateTenantRequest;
import com.randomenterprisesolutions.fhir.admin.service.TenantService;

/**
 * Unit tests for {@link AdminTenantsResource}.
 *
 * <p>CDI injection is replaced by direct field injection via reflection so these
 * tests run without a full container.
 */
public class AdminTenantsResourceTest {

    @Mock
    private TenantService tenantService;

    @Mock
    private UriInfo uriInfo;

    @Mock
    private UriBuilder uriBuilder;

    private AdminTenantsResource resource;
    private AutoCloseable mocks;

    @BeforeMethod
    public void setUp() throws Exception {
        mocks = MockitoAnnotations.openMocks(this);
        resource = new AdminTenantsResource();
        inject(resource, "tenantService", tenantService);
        inject(resource, "uriInfo", uriInfo);

        // stub UriInfo to return a predictable URI
        when(uriInfo.getAbsolutePathBuilder()).thenReturn(uriBuilder);
        when(uriBuilder.path(any(String.class))).thenReturn(uriBuilder);
        when(uriBuilder.build()).thenReturn(URI.create("https://localhost:9444/admin/tenants/acme"));
    }

    @AfterMethod
    public void tearDown() throws Exception {
        mocks.close();
    }

    // ── POST /admin/tenants ───────────────────────────────────────────────────

    @Test
    public void createTenant_returns201WithLocation() {
        TenantConfig config = buildConfig("acme");
        when(tenantService.createTenant(any())).thenReturn(config);

        Response response = resource.createTenant(new CreateTenantRequest());

        assertEquals(response.getStatus(), Status.CREATED.getStatusCode());
        assertNotNull(response.getLocation());
        assertEquals(((TenantConfig) response.getEntity()).getTenantId(), "acme");
    }

    // ── GET /admin/tenants ────────────────────────────────────────────────────

    @Test
    public void listTenants_returns200WithList() {
        when(tenantService.listTenants())
                .thenReturn(Arrays.asList(buildConfig("t1"), buildConfig("t2")));

        Response response = resource.listTenants();

        assertEquals(response.getStatus(), Status.OK.getStatusCode());
        @SuppressWarnings("unchecked")
        List<TenantConfig> body = (List<TenantConfig>) response.getEntity();
        assertEquals(body.size(), 2);
    }

    // ── GET /admin/tenants/{tenantId} ─────────────────────────────────────────

    @Test
    public void getTenant_returns200_whenFound() {
        when(tenantService.getTenant("acme")).thenReturn(buildConfig("acme"));

        Response response = resource.getTenant("acme");

        assertEquals(response.getStatus(), Status.OK.getStatusCode());
        assertEquals(((TenantConfig) response.getEntity()).getTenantId(), "acme");
    }

    @Test(expectedExceptions = TenantNotFoundException.class)
    public void getTenant_propagatesNotFoundException_whenMissing() {
        when(tenantService.getTenant("ghost")).thenThrow(new TenantNotFoundException("ghost"));
        resource.getTenant("ghost");
    }

    // ── PUT /admin/tenants/{tenantId} ─────────────────────────────────────────

    @Test
    public void updateTenant_returns200_withUpdatedEntity() {
        TenantConfig updated = buildConfig("acme");
        updated.setDisplayName("Updated");
        when(tenantService.updateTenant(eq("acme"), any())).thenReturn(updated);

        Response response = resource.updateTenant("acme", new UpdateTenantRequest());

        assertEquals(response.getStatus(), Status.OK.getStatusCode());
        assertEquals(((TenantConfig) response.getEntity()).getDisplayName(), "Updated");
    }

    // ── DELETE /admin/tenants/{tenantId} ──────────────────────────────────────

    @Test
    public void deleteTenant_returns204_onSuccess() {
        Response response = resource.deleteTenant("acme");

        verify(tenantService).deleteTenant("acme");
        assertEquals(response.getStatus(), Status.NO_CONTENT.getStatusCode());
    }

    @Test(expectedExceptions = TenantNotFoundException.class)
    public void deleteTenant_propagatesNotFoundException_whenMissing() {
        doThrow(new TenantNotFoundException("ghost")).when(tenantService).deleteTenant("ghost");
        resource.deleteTenant("ghost");
    }

    // ── POST /admin/tenants/{tenantId}/reload ─────────────────────────────────

    @Test
    public void reloadTenant_returns202_onSuccess() {
        when(tenantService.getTenant("acme")).thenReturn(buildConfig("acme"));

        Response response = resource.reloadTenant("acme");

        assertEquals(response.getStatus(), Status.ACCEPTED.getStatusCode());
    }

    @Test(expectedExceptions = TenantNotFoundException.class)
    public void reloadTenant_propagatesNotFoundException_whenMissing() {
        doThrow(new TenantNotFoundException("ghost")).when(tenantService).reloadTenant("ghost");
        resource.reloadTenant("ghost");
    }

    // ── helpers ──────────────────────────────────────────────────────────────

    private TenantConfig buildConfig(String tenantId) {
        TenantConfig cfg = new TenantConfig();
        cfg.setTenantId(tenantId);
        cfg.setDisplayName(tenantId);
        cfg.setDatastoreType("postgresql");
        cfg.setEnabled(true);
        return cfg;
    }

    /** Injects a value into a private field via reflection. */
    private static void inject(Object target, String fieldName, Object value) throws Exception {
        Field field = target.getClass().getDeclaredField(fieldName);
        field.setAccessible(true);
        field.set(target, value);
    }
}
