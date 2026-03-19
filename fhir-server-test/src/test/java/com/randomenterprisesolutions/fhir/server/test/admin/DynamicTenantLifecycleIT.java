/*
 * (C) Copyright Random Enterprise Solutions 2026
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package com.randomenterprisesolutions.fhir.server.test.admin;

import static com.randomenterprisesolutions.fhir.model.type.String.string;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertTrue;

import java.io.InputStream;
import java.util.Base64;
import java.util.Properties;
import java.util.UUID;
import java.util.logging.Logger;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.ClientRequestContext;
import javax.ws.rs.client.ClientRequestFilter;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.testng.SkipException;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import com.randomenterprisesolutions.fhir.core.FHIRMediaType;
import com.randomenterprisesolutions.fhir.model.resource.Bundle;
import com.randomenterprisesolutions.fhir.model.resource.Patient;
import com.randomenterprisesolutions.fhir.model.type.Boolean;
import com.randomenterprisesolutions.fhir.model.type.HumanName;
import com.randomenterprisesolutions.fhir.server.test.FHIRServerTestBase;

/**
 * End-to-end integration test for the dynamic tenant lifecycle.
 *
 * <p>The test exercises the full round-trip:
 * <ol>
 *   <li>Create a tenant via the Admin REST API ({@code POST /admin/tenants}).</li>
 *   <li>Confirm the FHIR server serves metadata for the new tenant.</li>
 *   <li>POST a FHIR {@code Patient} resource to the FHIR server under the new tenant.</li>
 *   <li>Read the {@code Patient} back by logical ID.</li>
 *   <li>Search {@code Patient}s by family name in the new tenant.</li>
 *   <li>Verify the resource is isolated — it is invisible to the {@code default} tenant.</li>
 *   <li>Trigger a config-cache reload via the Admin API and confirm that
 *       {@code configVersion} is incremented.</li>
 *   <li>Confirm FHIR access continues to work after the reload.</li>
 *   <li>Clean up: DELETE the FHIR resource, then DELETE the tenant via the Admin API.</li>
 *   <li>Confirm the tenant is gone from the Admin API.</li>
 * </ol>
 *
 * <h4>Enabling this test</h4>
 * The test is <em>skipped</em> unless {@code test.admin.base.url} is set (as a JVM
 * system property or in {@code test.properties}).  The property must point to the
 * context root of the admin webapp, e.g.:
 * <pre>
 *   test.admin.base.url = http://localhost:9080/fhir-admin
 * </pre>
 *
 * <h4>Infrastructure prerequisites</h4>
 * Both the FHIR server and the admin webapp must be running and share the same
 * admin database ({@code FHIR_ADMIN_DB_URL}).  The FHIR server must load tenant
 * configuration from that database ({@code DatabaseAwareTenantPropertyGroupCache}
 * is activated automatically when {@code FHIR_ADMIN_DB_URL} is set).
 *
 * <p>The dynamically created tenant's FHIR persistence schema must also be
 * provisioned before resource operations can succeed.  Either wire a real
 * {@code SchemaProvisioner} into the admin webapp, or run the
 * {@code fhir-persistence-schema} CLI with {@code --allocate-tenant <tenantId>}
 * against the target datastore after {@code POST /admin/tenants}.
 *
 * <h4>Credentials</h4>
 * Override the Admin API credentials via:
 * <ul>
 *   <li>{@code test.admin.username} (default {@code fhiradmin})</li>
 *   <li>{@code test.admin.password} (default {@code change-password})</li>
 * </ul>
 */
public class DynamicTenantLifecycleIT extends FHIRServerTestBase {

    private static final Logger LOG = Logger.getLogger(DynamicTenantLifecycleIT.class.getName());

    // ── property names ────────────────────────────────────────────────────────

    /** Base URL of the admin webapp, e.g. {@code http://localhost:9080/fhir-admin}. */
    public static final String PROP_ADMIN_BASE_URL = "test.admin.base.url";
    /** Admin API username (default {@code fhiradmin}). */
    public static final String PROP_ADMIN_USERNAME  = "test.admin.username";
    /** Admin API password (default {@code change-password}). */
    public static final String PROP_ADMIN_PASSWORD  = "test.admin.password";

    private static final String DEFAULT_ADMIN_USERNAME = "fhiradmin";
    private static final String DEFAULT_ADMIN_PASSWORD = "change-password";

    // ── test state ────────────────────────────────────────────────────────────

    /**
     * Unique tenant ID for this test run.  The short UUID suffix prevents
     * collisions with pre-existing tenant data when tests run in parallel or
     * are re-run without cleaning up.
     */
    private final String tenantId = "it-dyn-" + UUID.randomUUID().toString().substring(0, 8);

    /** Datastore ID used with {@code X-FHIR-DSID}.  Matches what the admin stores. */
    private static final String DSID = "default";

    /** Logical ID of the Patient created in step 4, used by subsequent steps. */
    private String patientId;

    /** JAX-RS client wired to the admin API base URL with Basic auth. */
    private Client adminClient;

    /** {@code /admin/tenants} WebTarget. */
    private WebTarget adminTenants;

    /** Set to {@code true} when the admin URL is not configured. */
    private boolean skip = false;

    // ── lifecycle ─────────────────────────────────────────────────────────────

    @BeforeClass
    @Override
    public void setUp() throws Exception {
        super.setUp();

        // Check for admin URL — first as a system property, then in test.properties
        String adminBaseUrl = System.getProperty(PROP_ADMIN_BASE_URL);
        if (adminBaseUrl == null) {
            adminBaseUrl = loadTestProperty(PROP_ADMIN_BASE_URL, null);
        }
        if (adminBaseUrl == null || adminBaseUrl.isBlank()) {
            LOG.warning(PROP_ADMIN_BASE_URL + " not set — DynamicTenantLifecycleIT will be skipped. "
                    + "Set this property to the admin webapp base URL to run the test.");
            skip = true;
            return;
        }

        String adminUser = System.getProperty(PROP_ADMIN_USERNAME,
                loadTestProperty(PROP_ADMIN_USERNAME, DEFAULT_ADMIN_USERNAME));
        String adminPass = System.getProperty(PROP_ADMIN_PASSWORD,
                loadTestProperty(PROP_ADMIN_PASSWORD, DEFAULT_ADMIN_PASSWORD));

        String basicToken = Base64.getEncoder()
                .encodeToString((adminUser + ":" + adminPass).getBytes());

        adminClient = ClientBuilder.newClient()
                .register((ClientRequestFilter) (ClientRequestContext ctx) ->
                        ctx.getHeaders().putSingle("Authorization", "Basic " + basicToken));

        adminTenants = adminClient.target(adminBaseUrl.stripTrailing("/") + "/admin/tenants");

        LOG.info("Admin API: " + adminBaseUrl + " | dynamic tenant ID: " + tenantId);
    }

    @AfterClass(alwaysRun = true)
    public void tearDown() {
        if (adminClient != null) {
            adminClient.close();
        }
    }

    // ── step 1: admin — create tenant ─────────────────────────────────────────

    @Test(groups = { "dynamic-tenant" })
    public void step01_create_tenant_via_admin_api() {
        if (skip) throw new SkipException(PROP_ADMIN_BASE_URL + " not configured");

        String reqBody = "{"
                + "\"tenantId\":\"" + tenantId + "\","
                + "\"displayName\":\"Dynamic IT Tenant\","
                + "\"datastoreType\":\"postgresql\","
                + "\"datastoreHost\":\"postgres\","
                + "\"datastorePort\":5432,"
                + "\"datastoreDatabase\":\"fhirdb\""
                + "}";

        try (Response r = adminTenants.request(MediaType.APPLICATION_JSON)
                .post(Entity.json(reqBody))) {
            assertEquals(r.getStatus(), 201,
                    "Admin API POST /tenants should return 201 Created");
            LOG.info("Tenant created: " + tenantId);
        }
    }

    // ── step 2: admin — round-trip GET shows configVersion=0 ─────────────────

    @Test(groups = { "dynamic-tenant" },
          dependsOnMethods = "step01_create_tenant_via_admin_api")
    public void step02_admin_get_shows_config_version_zero() {
        try (Response r = adminTenants.path(tenantId)
                .request(MediaType.APPLICATION_JSON).get()) {
            assertEquals(r.getStatus(), 200);
            String body = r.readEntity(String.class);
            assertTrue(body.contains("\"" + tenantId + "\""),
                    "Response should include the tenant ID");
            assertTrue(body.contains("\"configVersion\":0"),
                    "Newly created tenant should have configVersion=0, got: " + body);
        }
    }

    // ── step 3: FHIR — metadata is served for the new tenant ─────────────────

    @Test(groups = { "dynamic-tenant" },
          dependsOnMethods = "step01_create_tenant_via_admin_api")
    public void step03_fhir_metadata_accessible_for_new_tenant() {
        Response r = getWebTarget().path("metadata")
                .request(FHIRMediaType.APPLICATION_FHIR_JSON)
                .header("X-FHIR-TENANT-ID", tenantId)
                .get();
        assertEquals(r.getStatus(), 200,
                "FHIR /metadata should return 200 for the newly created tenant");
    }

    // ── step 4: FHIR — create a Patient ──────────────────────────────────────

    @Test(groups = { "dynamic-tenant" },
          dependsOnMethods = "step03_fhir_metadata_accessible_for_new_tenant")
    public void step04_create_patient_in_dynamic_tenant() {
        Patient patient = Patient.builder()
                .active(Boolean.of(true))
                .name(HumanName.builder()
                        .family(string("DynamicTenantIT"))
                        .given(string("Integration"))
                        .build())
                .build();

        Response r = getWebTarget().path("Patient")
                .request()
                .header("X-FHIR-TENANT-ID", tenantId)
                .header("X-FHIR-DSID", DSID)
                .post(Entity.entity(patient, FHIRMediaType.APPLICATION_FHIR_JSON));

        assertEquals(r.getStatus(), 201,
                "POST Patient should succeed in the dynamically created tenant");
        patientId = getLocationLogicalId(r);
        assertNotNull(patientId, "Location header must carry the Patient logical ID");
        LOG.info("Patient " + patientId + " created in tenant " + tenantId);
    }

    // ── step 5: FHIR — read by logical ID ────────────────────────────────────

    @Test(groups = { "dynamic-tenant" },
          dependsOnMethods = "step04_create_patient_in_dynamic_tenant")
    public void step05_read_patient_by_logical_id() {
        Response r = getWebTarget().path("Patient/" + patientId)
                .request(FHIRMediaType.APPLICATION_FHIR_JSON)
                .header("X-FHIR-TENANT-ID", tenantId)
                .header("X-FHIR-DSID", DSID)
                .get();
        assertResponse(r, 200);
        Patient patient = r.readEntity(Patient.class);
        assertEquals(patient.getId(), patientId,
                "Read response should carry the Patient we just created");
    }

    // ── step 6: FHIR — search by family name ─────────────────────────────────

    @Test(groups = { "dynamic-tenant" },
          dependsOnMethods = "step04_create_patient_in_dynamic_tenant")
    public void step06_search_patients_in_dynamic_tenant() {
        Response r = getWebTarget().path("Patient")
                .queryParam("family", "DynamicTenantIT")
                .request(FHIRMediaType.APPLICATION_FHIR_JSON)
                .header("X-FHIR-TENANT-ID", tenantId)
                .header("X-FHIR-DSID", DSID)
                .get();
        assertResponse(r, 200);
        Bundle bundle = r.readEntity(Bundle.class);
        assertTrue(bundle.getEntry().size() >= 1,
                "Search should find at least the Patient created in this test run");
    }

    // ── step 7: FHIR — tenant isolation ──────────────────────────────────────

    @Test(groups = { "dynamic-tenant" },
          dependsOnMethods = "step04_create_patient_in_dynamic_tenant")
    public void step07_patient_not_visible_in_default_tenant() {
        Response r = getWebTarget().path("Patient/" + patientId)
                .request(FHIRMediaType.APPLICATION_FHIR_JSON)
                .header("X-FHIR-TENANT-ID", "default")
                .get();
        assertEquals(r.getStatus(), 404,
                "Patient created in the dynamic tenant must not be accessible in 'default'");
    }

    // ── step 8: admin — reload bumps configVersion ───────────────────────────

    @Test(groups = { "dynamic-tenant" },
          dependsOnMethods = "step04_create_patient_in_dynamic_tenant")
    public void step08_admin_reload_increments_config_version() {
        // Trigger reload
        try (Response r = adminTenants.path(tenantId).path("reload")
                .request(MediaType.APPLICATION_JSON)
                .post(Entity.json(""))) {
            assertEquals(r.getStatus(), 202,
                    "POST /reload should return 202 Accepted");
        }

        // Verify configVersion is now 1
        try (Response r = adminTenants.path(tenantId)
                .request(MediaType.APPLICATION_JSON).get()) {
            assertEquals(r.getStatus(), 200);
            String body = r.readEntity(String.class);
            assertTrue(body.contains("\"configVersion\":1"),
                    "configVersion should be 1 after first reload, got: " + body);
        }

        // A second reload brings it to 2
        try (Response r = adminTenants.path(tenantId).path("reload")
                .request(MediaType.APPLICATION_JSON)
                .post(Entity.json(""))) {
            assertEquals(r.getStatus(), 202);
        }
        try (Response r = adminTenants.path(tenantId)
                .request(MediaType.APPLICATION_JSON).get()) {
            String body = r.readEntity(String.class);
            assertTrue(body.contains("\"configVersion\":2"),
                    "configVersion should be 2 after second reload, got: " + body);
        }
    }

    // ── step 9: FHIR — still works after config reload ───────────────────────

    @Test(groups = { "dynamic-tenant" },
          dependsOnMethods = { "step05_read_patient_by_logical_id",
                               "step08_admin_reload_increments_config_version" })
    public void step09_fhir_access_works_after_config_reload() {
        Response r = getWebTarget().path("Patient/" + patientId)
                .request(FHIRMediaType.APPLICATION_FHIR_JSON)
                .header("X-FHIR-TENANT-ID", tenantId)
                .header("X-FHIR-DSID", DSID)
                .get();
        assertResponse(r, 200);
        Patient patient = r.readEntity(Patient.class);
        assertEquals(patient.getId(), patientId,
                "Patient must still be readable after config-cache reload");
    }

    // ── step 10: FHIR — delete the Patient ───────────────────────────────────

    @Test(groups = { "dynamic-tenant" },
          dependsOnMethods = { "step09_fhir_access_works_after_config_reload",
                               "step07_patient_not_visible_in_default_tenant",
                               "step06_search_patients_in_dynamic_tenant" },
          alwaysRun = true)
    public void step10_delete_patient() {
        if (patientId == null) {
            LOG.warning("No patient ID recorded — skipping Patient DELETE");
            return;
        }
        Response r = getWebTarget().path("Patient/" + patientId)
                .request()
                .header("X-FHIR-TENANT-ID", tenantId)
                .header("X-FHIR-DSID", DSID)
                .delete();
        assertTrue(r.getStatus() == 200 || r.getStatus() == 204,
                "DELETE Patient should succeed, got: " + r.getStatus());
        LOG.info("Patient " + patientId + " deleted from tenant " + tenantId);
    }

    // ── step 11: admin — delete tenant ───────────────────────────────────────

    @Test(groups = { "dynamic-tenant" },
          dependsOnMethods = "step10_delete_patient",
          alwaysRun = true)
    public void step11_delete_tenant_via_admin_api() {
        if (skip || adminTenants == null) return;
        try (Response r = adminTenants.path(tenantId).request().delete()) {
            assertEquals(r.getStatus(), 204,
                    "Admin DELETE /tenants/{id} should return 204 No Content");
            LOG.info("Tenant " + tenantId + " deleted via admin API");
        }
    }

    // ── step 12: admin — confirm tenant gone ─────────────────────────────────

    @Test(groups = { "dynamic-tenant" },
          dependsOnMethods = "step11_delete_tenant_via_admin_api")
    public void step12_deleted_tenant_not_found_in_admin_api() {
        try (Response r = adminTenants.path(tenantId)
                .request(MediaType.APPLICATION_JSON).get()) {
            assertEquals(r.getStatus(), 404,
                    "Admin GET /tenants/{id} should return 404 after deletion");
        }
    }

    // ── helper ────────────────────────────────────────────────────────────────

    /**
     * Reads a single property from {@code test.properties} on the classpath,
     * returning {@code defaultValue} if the file is absent or the key is missing.
     */
    private static String loadTestProperty(String key, String defaultValue) {
        try {
            Properties p = new Properties();
            InputStream is = Thread.currentThread().getContextClassLoader()
                    .getResourceAsStream("test.properties");
            if (is != null) {
                try { p.load(is); } finally { is.close(); }
            }
            return p.getProperty(key, defaultValue);
        } catch (Exception e) {
            return defaultValue;
        }
    }
}
