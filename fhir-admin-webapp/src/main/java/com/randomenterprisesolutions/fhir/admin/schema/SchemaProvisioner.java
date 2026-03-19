/*
 * (C) Copyright Random Enterprise Solutions 2026
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package com.randomenterprisesolutions.fhir.admin.schema;

import com.randomenterprisesolutions.fhir.admin.model.TenantConfig;

/**
 * Provisions (or validates) the FHIR persistence schema for a newly created tenant.
 *
 * <p>Implementations are called by
 * {@link com.randomenterprisesolutions.fhir.admin.service.TenantService}
 * immediately after a {@code POST /admin/tenants} request is persisted to the
 * tenant-config store.
 *
 * <p>The default CDI bean in this module is {@link NoOpSchemaProvisioner}, which
 * logs a warning and returns.  Replace it with a real implementation wired to the
 * {@code fhir-persistence-schema} CLI by annotating the new class with
 * {@link javax.enterprise.inject.Alternative} and enabling it in {@code beans.xml}.
 */
public interface SchemaProvisioner {

    /**
     * Provisions the FHIR persistence schema for the given tenant.
     *
     * <p>Implementations may be synchronous (block until schema is ready) or
     * asynchronous (return immediately and signal readiness via a different
     * mechanism).  Callers treat a normal return as success.
     *
     * @param tenant the freshly-created tenant configuration as stored by the
     *               admin API; never null
     * @throws SchemaProvisioningException if provisioning fails and the
     *                                     caller should surface the error
     */
    void provision(TenantConfig tenant) throws SchemaProvisioningException;
}
