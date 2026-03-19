/*
 * (C) Copyright Random Enterprise Solutions 2026
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package com.randomenterprisesolutions.fhir.admin.it;

import java.lang.reflect.Field;
import java.util.Arrays;

import javax.sql.DataSource;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Entity;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.apache.cxf.endpoint.Server;
import org.apache.cxf.jaxrs.JAXRSServerFactoryBean;
import org.apache.derby.jdbc.EmbeddedDataSource;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.fasterxml.jackson.jaxrs.json.JacksonJsonProvider;
import com.randomenterprisesolutions.fhir.admin.exception.AdminExceptionMapper;
import com.randomenterprisesolutions.fhir.admin.model.CreateTenantRequest;
import com.randomenterprisesolutions.fhir.admin.model.TenantConfig;
import com.randomenterprisesolutions.fhir.admin.model.UpdateTenantRequest;
import com.randomenterprisesolutions.fhir.admin.repository.JdbcTenantRepository;
import com.randomenterprisesolutions.fhir.admin.resource.AdminTenantsResource;
import com.randomenterprisesolutions.fhir.admin.schema.NoOpSchemaProvisioner;
import com.randomenterprisesolutions.fhir.admin.schema.SchemaProvisioner;
import com.randomenterprisesolutions.fhir.admin.service.TenantService;
import com.randomenterprisesolutions.fhir.config.DatabaseTenantConfigSource;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertNull;
import static org.testng.Assert.assertTrue;

/**
 * End-to-end integration tests for the tenant lifecycle with a JDBC-backed store.
 *
 * <p>This test class:
 * <ol>
 *   <li>Spins up an embedded CXF JAX-RS server (no Liberty, no containers) wired to a
 *       {@link JdbcTenantRepository} backed by an in-memory Derby database.</li>
 *   <li>Exercises the full tenant lifecycle over real HTTP calls: create, get, list,
 *       update, reload, delete.</li>
 *   <li>Verifies that {@code configVersion} starts at 0 and is incremented by a
 *       {@code POST /reload} request.</li>
 *   <li>Verifies that {@link DatabaseTenantConfigSource} can read the stored
 *       {@code fhirServerConfig} JSON back from the same Derby database.</li>
 * </ol>
 *
 * <p>No external services are required.  The test is named {@code *IT.java} so
 * Maven Failsafe runs it in the {@code integration-test} phase.
 */
public class TenantLifecycleIT {

    private static final String DB_NAME = "memory:fhir-admin-lifecycle-it";
    private static final String DERBY_URL = "jdbc:derby:" + DB_NAME + ";create=true";
    private static final int PORT = 19445;
    private static final String BASE_URL = "http://localhost:" + PORT;
    private static final String TENANTS_URL = BASE_URL + "/admin/tenants";

    private Server server;
    private Client client;
    private JdbcTenantRepository repo;
    private DataSource dataSource;

    // The fhirServerConfig JSON stored for the E2E tenant
    private static final String FHIR_CONFIG_JSON =
            "{\"fhirServer\":{\"core\":{\"defaultTenantId\":\"e2e-tenant\"}}}";

    // Created tenant ID shared across tests in this class
    private static final String E2E_TENANT = "e2e-tenant";

    @BeforeClass
    public void setUp() throws Exception {
        // ── JDBC / Derby ──────────────────────────────────────────────────────
        EmbeddedDataSource ds = new EmbeddedDataSource();
        ds.setDatabaseName(DB_NAME);
        ds.setCreateDatabase("create");
        this.dataSource = ds;
        repo = new JdbcTenantRepository(ds);

        // ── Service + Resource (reflection injection — no CDI in unit tests) ──
        SchemaProvisioner provisioner = new NoOpSchemaProvisioner();
        TenantService service = new TenantService(repo, provisioner);
        AdminTenantsResource resource = new AdminTenantsResource();
        inject(resource, "tenantService", service);

        // ── Jackson ───────────────────────────────────────────────────────────
        ObjectMapper mapper = new ObjectMapper()
                .registerModule(new JavaTimeModule())
                .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        JacksonJsonProvider jacksonProvider = new JacksonJsonProvider(mapper);

        // ── CXF server ────────────────────────────────────────────────────────
        JAXRSServerFactoryBean factory = new JAXRSServerFactoryBean();
        factory.setAddress(BASE_URL);
        factory.setServiceBeans(Arrays.asList(resource));
        factory.setProviders(Arrays.asList(jacksonProvider, new AdminExceptionMapper()));
        server = factory.create();

        // ── JAX-RS client ─────────────────────────────────────────────────────
        client = ClientBuilder.newClient().register(jacksonProvider);
    }

    @AfterClass
    public void tearDown() {
        if (client != null) client.close();
        if (server != null) {
            server.stop();
            server.destroy();
        }

        // Drop the in-memory Derby database
        try {
            EmbeddedDataSource shutdown = new EmbeddedDataSource();
            shutdown.setDatabaseName(DB_NAME);
            shutdown.setConnectionAttributes("drop=true");
            shutdown.getConnection();
        } catch (Exception ignored) {
            // Derby throws on drop — expected
        }
    }

    // ── helper: reflection injection (same pattern as AdminTenantsIT) ──────────

    private static void inject(Object target, String fieldName, Object value)
            throws Exception {
        Field f = target.getClass().getDeclaredField(fieldName);
        f.setAccessible(true);
        f.set(target, value);
    }

    // ── create ────────────────────────────────────────────────────────────────

    @Test
    public void step01_create_tenant_returns_201() {
        CreateTenantRequest req = buildCreateRequest(E2E_TENANT, "postgresql");
        req.setFhirServerConfig(FHIR_CONFIG_JSON);

        try (Response resp = client.target(TENANTS_URL)
                .request(MediaType.APPLICATION_JSON)
                .post(Entity.json(req))) {
            assertEquals(resp.getStatus(), 201);
            TenantConfig body = resp.readEntity(TenantConfig.class);
            assertEquals(body.getTenantId(), E2E_TENANT);
            assertTrue(body.isEnabled());
            assertEquals(body.getConfigVersion(), 0, "New tenant should have configVersion=0");
            assertNotNull(body.getCreatedAt());
        }
    }

    @Test(dependsOnMethods = "step01_create_tenant_returns_201",
          expectedExceptions = {}, description = "Duplicate create returns 409")
    public void step02_duplicate_create_returns_409() {
        try (Response resp = client.target(TENANTS_URL)
                .request(MediaType.APPLICATION_JSON)
                .post(Entity.json(buildCreateRequest(E2E_TENANT, "derby")))) {
            assertEquals(resp.getStatus(), 409);
        }
    }

    // ── get ───────────────────────────────────────────────────────────────────

    @Test(dependsOnMethods = "step01_create_tenant_returns_201")
    public void step03_get_tenant_returns_200() {
        try (Response resp = client.target(TENANTS_URL + "/" + E2E_TENANT)
                .request(MediaType.APPLICATION_JSON)
                .get()) {
            assertEquals(resp.getStatus(), 200);
            TenantConfig body = resp.readEntity(TenantConfig.class);
            assertEquals(body.getTenantId(), E2E_TENANT);
            assertEquals(body.getDatastoreType(), "postgresql");
            assertEquals(body.getFhirServerConfig(), FHIR_CONFIG_JSON);
            assertEquals(body.getConfigVersion(), 0);
        }
    }

    @Test
    public void step03b_get_nonexistent_returns_404() {
        try (Response resp = client.target(TENANTS_URL + "/no-such-tenant-lifecycle")
                .request(MediaType.APPLICATION_JSON)
                .get()) {
            assertEquals(resp.getStatus(), 404);
        }
    }

    // ── list ──────────────────────────────────────────────────────────────────

    @Test(dependsOnMethods = "step01_create_tenant_returns_201")
    public void step04_list_includes_created_tenant() {
        try (Response resp = client.target(TENANTS_URL)
                .request(MediaType.APPLICATION_JSON)
                .get()) {
            assertEquals(resp.getStatus(), 200);
            TenantConfig[] list = resp.readEntity(TenantConfig[].class);
            boolean found = false;
            for (TenantConfig t : list) {
                if (E2E_TENANT.equals(t.getTenantId())) {
                    found = true;
                    break;
                }
            }
            assertTrue(found, "List should include the created e2e tenant");
        }
    }

    // ── update ────────────────────────────────────────────────────────────────

    @Test(dependsOnMethods = "step03_get_tenant_returns_200")
    public void step05_update_tenant_returns_200() {
        UpdateTenantRequest update = new UpdateTenantRequest();
        update.setDisplayName("E2E Updated");
        update.setDatastoreHost("db.e2e.internal");

        try (Response resp = client.target(TENANTS_URL + "/" + E2E_TENANT)
                .request(MediaType.APPLICATION_JSON)
                .put(Entity.json(update))) {
            assertEquals(resp.getStatus(), 200);
            TenantConfig body = resp.readEntity(TenantConfig.class);
            assertEquals(body.getDisplayName(), "E2E Updated");
            assertEquals(body.getDatastoreHost(), "db.e2e.internal");
            // datastoreType should be unchanged
            assertEquals(body.getDatastoreType(), "postgresql");
        }
    }

    // ── reload bumps config_version ───────────────────────────────────────────

    @Test(dependsOnMethods = "step03_get_tenant_returns_200")
    public void step06_reload_bumps_config_version() {
        // Record version before reload
        long versionBefore = repo.getConfigVersion(E2E_TENANT);
        assertEquals(versionBefore, 0, "Before reload config_version should be 0");

        // POST reload
        try (Response resp = client.target(TENANTS_URL + "/" + E2E_TENANT + "/reload")
                .request(MediaType.APPLICATION_JSON)
                .post(Entity.json(""))) {
            assertEquals(resp.getStatus(), 202);
        }

        // Config version must have been incremented
        long versionAfter = repo.getConfigVersion(E2E_TENANT);
        assertEquals(versionAfter, 1, "After reload config_version should be 1");

        // A second reload increments again
        client.target(TENANTS_URL + "/" + E2E_TENANT + "/reload")
              .request(MediaType.APPLICATION_JSON)
              .post(Entity.json("")).close();
        assertEquals(repo.getConfigVersion(E2E_TENANT), 2,
                "Second reload should bring version to 2");
    }

    // ── DatabaseTenantConfigSource reads stored config ─────────────────────────

    @Test(dependsOnMethods = "step01_create_tenant_returns_201")
    public void step07_db_config_source_returns_stored_fhir_config() {
        DatabaseTenantConfigSource source = new DatabaseTenantConfigSource(DERBY_URL);

        String json = source.getConfigJson(E2E_TENANT);
        assertEquals(json, FHIR_CONFIG_JSON,
                "DatabaseTenantConfigSource should return the stored fhirServerConfig");
    }

    @Test(dependsOnMethods = "step01_create_tenant_returns_201")
    public void step08_db_config_source_returns_version() {
        DatabaseTenantConfigSource source = new DatabaseTenantConfigSource(DERBY_URL);
        long version = source.getConfigVersion(E2E_TENANT);
        assertTrue(version >= 0,
                "DatabaseTenantConfigSource should return a non-negative config_version");
    }

    @Test
    public void step08b_db_config_source_returns_null_for_unknown_tenant() {
        DatabaseTenantConfigSource source = new DatabaseTenantConfigSource(DERBY_URL);
        assertNull(source.getConfigJson("ghost-tenant-xyz"),
                "DatabaseTenantConfigSource should return null for unknown tenants");
        assertEquals(source.getConfigVersion("ghost-tenant-xyz"), -1);
    }

    // ── delete ────────────────────────────────────────────────────────────────

    @Test(dependsOnMethods = {
            "step05_update_tenant_returns_200",
            "step06_reload_bumps_config_version",
            "step07_db_config_source_returns_stored_fhir_config",
            "step08_db_config_source_returns_version"})
    public void step09_delete_tenant_returns_204() {
        try (Response resp = client.target(TENANTS_URL + "/" + E2E_TENANT)
                .request()
                .delete()) {
            assertEquals(resp.getStatus(), 204);
        }
        // Subsequent GET should be 404
        try (Response resp = client.target(TENANTS_URL + "/" + E2E_TENANT)
                .request(MediaType.APPLICATION_JSON)
                .get()) {
            assertEquals(resp.getStatus(), 404);
        }
    }

    @Test(dependsOnMethods = "step09_delete_tenant_returns_204")
    public void step10_delete_nonexistent_returns_404() {
        try (Response resp = client.target(TENANTS_URL + "/" + E2E_TENANT)
                .request()
                .delete()) {
            assertEquals(resp.getStatus(), 404);
        }
    }

    // ── validation ────────────────────────────────────────────────────────────

    @Test
    public void invalid_tenant_id_returns_400() {
        CreateTenantRequest req = buildCreateRequest("invalid tenant id!", "derby");
        try (Response resp = client.target(TENANTS_URL)
                .request(MediaType.APPLICATION_JSON)
                .post(Entity.json(req))) {
            assertEquals(resp.getStatus(), 400);
        }
    }

    @Test
    public void missing_datastore_type_returns_400() {
        CreateTenantRequest req = new CreateTenantRequest();
        req.setTenantId("missing-ds-type");
        try (Response resp = client.target(TENANTS_URL)
                .request(MediaType.APPLICATION_JSON)
                .post(Entity.json(req))) {
            assertEquals(resp.getStatus(), 400);
        }
    }

    // ── helper ────────────────────────────────────────────────────────────────

    private CreateTenantRequest buildCreateRequest(String tenantId, String datastoreType) {
        CreateTenantRequest req = new CreateTenantRequest();
        req.setTenantId(tenantId);
        req.setDisplayName(tenantId + " display");
        req.setDatastoreType(datastoreType);
        req.setDatastoreHost("localhost");
        req.setDatastorePort(5432);
        req.setDatastoreDatabase(tenantId + "_db");
        return req;
    }
}
