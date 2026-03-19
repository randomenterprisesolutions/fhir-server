/*
 * (C) Copyright Random Enterprise Solutions 2026
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package com.randomenterprisesolutions.fhir.admin.it;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertTrue;

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.List;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Entity;
import javax.ws.rs.core.GenericType;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import org.apache.cxf.endpoint.Server;
import org.apache.cxf.jaxrs.JAXRSServerFactoryBean;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.fasterxml.jackson.jaxrs.json.JacksonJsonProvider;
import com.randomenterprisesolutions.fhir.admin.exception.AdminExceptionMapper;
import com.randomenterprisesolutions.fhir.admin.model.ApiError;
import com.randomenterprisesolutions.fhir.admin.model.CreateTenantRequest;
import com.randomenterprisesolutions.fhir.admin.model.TenantConfig;
import com.randomenterprisesolutions.fhir.admin.model.UpdateTenantRequest;
import com.randomenterprisesolutions.fhir.admin.repository.InMemoryTenantRepository;
import com.randomenterprisesolutions.fhir.admin.resource.AdminTenantsResource;
import com.randomenterprisesolutions.fhir.admin.service.TenantService;

/**
 * End-to-end integration tests for the Admin Tenant API.
 *
 * <p>An Apache CXF embedded Jetty server is started in-process so the full
 * HTTP request/response cycle is exercised — including JSON serialisation,
 * routing and exception mapping — without needing a deployed application server.
 *
 * <p>These tests are intentionally excluded from the {@code test} lifecycle phase
 * (see {@code maven-surefire-plugin} config in {@code pom.xml}) and run only
 * during {@code integration-test} via Maven Failsafe. You can run them locally
 * with:
 * <pre>
 *   mvn verify -pl fhir-admin-webapp
 * </pre>
 */
public class AdminTenantsIT {

    private static final String BASE_URL = "http://localhost:19444";
    private static final String TENANTS_URL = BASE_URL + "/admin/tenants";

    private Server cxfServer;
    private Client client;

    // ── lifecycle ─────────────────────────────────────────────────────────────

    @BeforeClass
    public void startServer() throws Exception {
        // shared ObjectMapper with java.time support
        ObjectMapper mapper = new ObjectMapper()
                .registerModule(new JavaTimeModule())
                .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        JacksonJsonProvider jacksonProvider = new JacksonJsonProvider(mapper);

        // Wire the resource manually (no CDI in this embedded test context)
        InMemoryTenantRepository repo = new InMemoryTenantRepository();
        TenantService service = new TenantService(repo);
        AdminTenantsResource resource = new AdminTenantsResource();
        inject(resource, "tenantService", service);

        JAXRSServerFactoryBean factory = new JAXRSServerFactoryBean();
        factory.setAddress(BASE_URL);
        factory.setServiceBeans(Arrays.asList(resource));
        factory.setProviders(Arrays.asList(jacksonProvider, new AdminExceptionMapper()));
        cxfServer = factory.create();

        client = ClientBuilder.newClient()
                .register(jacksonProvider);
    }

    @AfterClass(alwaysRun = true)
    public void stopServer() {
        if (client != null) {
            client.close();
        }
        if (cxfServer != null) {
            cxfServer.stop();
            cxfServer.destroy();
        }
    }

    // ── POST /admin/tenants ───────────────────────────────────────────────────

    @Test
    public void createTenant_returns201_andTenantIsRetrievable() {
        CreateTenantRequest req = createRequest("it-acme", "postgresql");

        Response response = client.target(TENANTS_URL)
                .request(MediaType.APPLICATION_JSON)
                .post(Entity.json(req));

        assertEquals(response.getStatus(), Status.CREATED.getStatusCode());
        assertNotNull(response.getLocation(), "Location header must be set");

        TenantConfig body = response.readEntity(TenantConfig.class);
        assertEquals(body.getTenantId(), "it-acme");
        assertTrue(body.isEnabled());
    }

    @Test(dependsOnMethods = "createTenant_returns201_andTenantIsRetrievable")
    public void createTenant_duplicateId_returns409() {
        CreateTenantRequest req = createRequest("it-acme", "derby");

        Response response = client.target(TENANTS_URL)
                .request(MediaType.APPLICATION_JSON)
                .post(Entity.json(req));

        assertEquals(response.getStatus(), Status.CONFLICT.getStatusCode());
        ApiError error = response.readEntity(ApiError.class);
        assertEquals(error.getStatus(), 409);
    }

    @Test
    public void createTenant_missingTenantId_returns400() {
        CreateTenantRequest req = new CreateTenantRequest();
        req.setDatastoreType("postgresql");
        // tenantId intentionally omitted

        Response response = client.target(TENANTS_URL)
                .request(MediaType.APPLICATION_JSON)
                .post(Entity.json(req));

        assertEquals(response.getStatus(), Status.BAD_REQUEST.getStatusCode());
        ApiError error = response.readEntity(ApiError.class);
        assertEquals(error.getStatus(), 400);
    }

    // ── GET /admin/tenants ────────────────────────────────────────────────────

    @Test(dependsOnMethods = "createTenant_returns201_andTenantIsRetrievable")
    public void listTenants_returns200_withAtLeastOneTenant() {
        Response response = client.target(TENANTS_URL)
                .request(MediaType.APPLICATION_JSON)
                .get();

        assertEquals(response.getStatus(), Status.OK.getStatusCode());
        List<TenantConfig> tenants = response.readEntity(new GenericType<List<TenantConfig>>() {});
        assertFalse(tenants.isEmpty());
    }

    // ── GET /admin/tenants/{tenantId} ─────────────────────────────────────────

    @Test(dependsOnMethods = "createTenant_returns201_andTenantIsRetrievable")
    public void getTenant_returns200_forExistingTenant() {
        Response response = client.target(TENANTS_URL).path("it-acme")
                .request(MediaType.APPLICATION_JSON)
                .get();

        assertEquals(response.getStatus(), Status.OK.getStatusCode());
        TenantConfig config = response.readEntity(TenantConfig.class);
        assertEquals(config.getTenantId(), "it-acme");
    }

    @Test
    public void getTenant_returns404_forUnknownTenant() {
        Response response = client.target(TENANTS_URL).path("does-not-exist")
                .request(MediaType.APPLICATION_JSON)
                .get();

        assertEquals(response.getStatus(), Status.NOT_FOUND.getStatusCode());
        ApiError error = response.readEntity(ApiError.class);
        assertEquals(error.getStatus(), 404);
    }

    // ── PUT /admin/tenants/{tenantId} ─────────────────────────────────────────

    @Test(dependsOnMethods = "createTenant_returns201_andTenantIsRetrievable")
    public void updateTenant_returns200_withChangedDisplayName() {
        UpdateTenantRequest req = new UpdateTenantRequest();
        req.setDisplayName("Acme Corp (updated)");

        Response response = client.target(TENANTS_URL).path("it-acme")
                .request(MediaType.APPLICATION_JSON)
                .put(Entity.json(req));

        assertEquals(response.getStatus(), Status.OK.getStatusCode());
        TenantConfig updated = response.readEntity(TenantConfig.class);
        assertEquals(updated.getDisplayName(), "Acme Corp (updated)");
    }

    @Test
    public void updateTenant_returns404_forUnknownTenant() {
        Response response = client.target(TENANTS_URL).path("unknown-tenant")
                .request(MediaType.APPLICATION_JSON)
                .put(Entity.json(new UpdateTenantRequest()));

        assertEquals(response.getStatus(), Status.NOT_FOUND.getStatusCode());
    }

    // ── POST /admin/tenants/{tenantId}/reload ─────────────────────────────────

    @Test(dependsOnMethods = "createTenant_returns201_andTenantIsRetrievable")
    public void reloadTenant_returns202_forExistingTenant() {
        Response response = client.target(TENANTS_URL).path("it-acme").path("reload")
                .request(MediaType.APPLICATION_JSON)
                .post(Entity.json(""));

        assertEquals(response.getStatus(), Status.ACCEPTED.getStatusCode());
    }

    @Test
    public void reloadTenant_returns404_forUnknownTenant() {
        Response response = client.target(TENANTS_URL).path("ghost").path("reload")
                .request(MediaType.APPLICATION_JSON)
                .post(Entity.json(""));

        assertEquals(response.getStatus(), Status.NOT_FOUND.getStatusCode());
    }

    // ── DELETE /admin/tenants/{tenantId} ──────────────────────────────────────

    @Test(dependsOnMethods = {
            "createTenant_returns201_andTenantIsRetrievable",
            "updateTenant_returns200_withChangedDisplayName",
            "reloadTenant_returns202_forExistingTenant",
            "getTenant_returns200_forExistingTenant"
    })
    public void deleteTenant_returns204_andTenantDisappears() {
        Response deleteResponse = client.target(TENANTS_URL).path("it-acme")
                .request(MediaType.APPLICATION_JSON)
                .delete();

        assertEquals(deleteResponse.getStatus(), Status.NO_CONTENT.getStatusCode());

        // Verify it is really gone
        Response getResponse = client.target(TENANTS_URL).path("it-acme")
                .request(MediaType.APPLICATION_JSON)
                .get();
        assertEquals(getResponse.getStatus(), Status.NOT_FOUND.getStatusCode());
    }

    @Test
    public void deleteTenant_returns404_forUnknownTenant() {
        Response response = client.target(TENANTS_URL).path("ghost-tenant")
                .request(MediaType.APPLICATION_JSON)
                .delete();

        assertEquals(response.getStatus(), Status.NOT_FOUND.getStatusCode());
    }

    // ── helpers ──────────────────────────────────────────────────────────────

    private CreateTenantRequest createRequest(String tenantId, String datastoreType) {
        CreateTenantRequest req = new CreateTenantRequest();
        req.setTenantId(tenantId);
        req.setDatastoreType(datastoreType);
        req.setDisplayName(tenantId);
        return req;
    }

    private static void inject(Object target, String fieldName, Object value) throws Exception {
        Field field = target.getClass().getDeclaredField(fieldName);
        field.setAccessible(true);
        field.set(target, value);
    }
}
