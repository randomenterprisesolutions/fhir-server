/*
 * (C) Copyright Random Enterprise Solutions 2026
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package com.randomenterprisesolutions.fhir.admin.schema;

import java.util.logging.Logger;

import javax.enterprise.context.ApplicationScoped;

import com.randomenterprisesolutions.fhir.admin.model.TenantConfig;

/**
 * Default (no-op) {@link SchemaProvisioner} that logs operator instructions
 * instead of running schema DDL.
 *
 * <p>This implementation decouples the admin webapp from the
 * {@code fhir-persistence-schema} CLI jar.  To automate provisioning, replace
 * this bean with an {@link javax.enterprise.inject.Alternative} that shells out
 * to (or programmatically invokes) {@code FhirSchemaGenerator} via the CLI.
 *
 * <h4>Manual provisioning instructions</h4>
 * After creating a tenant via {@code POST /admin/tenants}, provision the
 * FHIR persistence schema by running:
 * <pre>
 *   java -jar fhir-persistence-schema-cli.jar \
 *        --db-type &lt;postgresql|derby|db2&gt; \
 *        --prop db.host=&lt;host&gt; \
 *        --prop db.port=&lt;port&gt; \
 *        --prop db.database=&lt;dbname&gt; \
 *        --prop db.user=&lt;user&gt; \
 *        --prop db.password=&lt;password&gt; \
 *        --allocate-tenant &lt;tenantId&gt;
 * </pre>
 */
@ApplicationScoped
public class NoOpSchemaProvisioner implements SchemaProvisioner {

    private static final Logger log = Logger.getLogger(NoOpSchemaProvisioner.class.getName());

    @Override
    public void provision(TenantConfig tenant) {
        log.warning("Schema provisioning is not configured (NoOpSchemaProvisioner)."
                + " Tenant '" + tenant.getTenantId() + "' was registered in the admin store"
                + " but the FHIR persistence schema has NOT been created."
                + " Run the fhir-persistence-schema CLI with --allocate-tenant "
                + tenant.getTenantId() + " before sending FHIR API requests for this tenant.");
    }
}
