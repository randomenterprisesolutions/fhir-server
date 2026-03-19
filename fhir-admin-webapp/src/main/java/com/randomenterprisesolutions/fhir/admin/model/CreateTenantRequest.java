/*
 * (C) Copyright Random Enterprise Solutions 2026
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package com.randomenterprisesolutions.fhir.admin.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

/**
 * Request body for {@code POST /admin/tenants}.
 */
@JsonInclude(Include.NON_NULL)
public class CreateTenantRequest {

    /** Required: unique identifier for the new tenant. */
    private String tenantId;

    /** Optional: human-readable display name; defaults to tenantId. */
    private String displayName;

    /** Required: datastore type (e.g. {@code postgresql}, {@code derby}). */
    private String datastoreType;

    /** Optional: datastore host (may be omitted for embedded stores). */
    private String datastoreHost;

    /** Optional: datastore port; 0 means use datastore default. */
    private int datastorePort;

    /** Optional: database / schema name. */
    private String datastoreDatabase;

    /**
     * Optional: full FHIR server configuration JSON for this tenant.
     * When omitted, a default configuration is generated from the other fields.
     */
    private String fhirServerConfig;

    // ── getters / setters ─────────────────────────────────────────────────────

    public String getTenantId() {
        return tenantId;
    }

    public void setTenantId(String tenantId) {
        this.tenantId = tenantId;
    }

    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    public String getDatastoreType() {
        return datastoreType;
    }

    public void setDatastoreType(String datastoreType) {
        this.datastoreType = datastoreType;
    }

    public String getDatastoreHost() {
        return datastoreHost;
    }

    public void setDatastoreHost(String datastoreHost) {
        this.datastoreHost = datastoreHost;
    }

    public int getDatastorePort() {
        return datastorePort;
    }

    public void setDatastorePort(int datastorePort) {
        this.datastorePort = datastorePort;
    }

    public String getDatastoreDatabase() {
        return datastoreDatabase;
    }

    public void setDatastoreDatabase(String datastoreDatabase) {
        this.datastoreDatabase = datastoreDatabase;
    }

    public String getFhirServerConfig() {
        return fhirServerConfig;
    }

    public void setFhirServerConfig(String fhirServerConfig) {
        this.fhirServerConfig = fhirServerConfig;
    }
}
