/*
 * (C) Copyright Random Enterprise Solutions 2026
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package com.randomenterprisesolutions.fhir.admin.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

/**
 * Request body for {@code PUT /admin/tenants/{tenantId}}.
 * All fields are optional; only non-null fields are applied to the stored config.
 */
@JsonInclude(Include.NON_NULL)
public class UpdateTenantRequest {

    /** Optional: new human-readable display name. */
    private String displayName;

    /** Optional: updated datastore type. */
    private String datastoreType;

    /** Optional: updated datastore host. */
    private String datastoreHost;

    /** Optional: updated datastore port. Null leaves the existing value unchanged. */
    private Integer datastorePort;

    /** Optional: updated database / schema name. */
    private String datastoreDatabase;

    /** Optional: full replacement of the FHIR server config JSON. */
    private String fhirServerConfig;

    /** Optional: enable or disable the tenant. */
    private Boolean enabled;

    // ── getters / setters ─────────────────────────────────────────────────────

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

    public Integer getDatastorePort() {
        return datastorePort;
    }

    public void setDatastorePort(Integer datastorePort) {
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

    public Boolean getEnabled() {
        return enabled;
    }

    public void setEnabled(Boolean enabled) {
        this.enabled = enabled;
    }
}
