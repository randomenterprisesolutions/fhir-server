/*
 * (C) Copyright Random Enterprise Solutions 2026
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package com.randomenterprisesolutions.fhir.admin.model;

import java.time.Instant;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

/**
 * Domain model representing a registered tenant and its configuration.
 * This object is persisted in the TENANT_CONFIG table and returned by the admin API.
 */
@JsonInclude(Include.NON_NULL)
public class TenantConfig {

    /** Unique tenant identifier (alphanumeric, max 64 chars). */
    private String tenantId;

    /** Human-readable display name. */
    private String displayName;

    /** Type of the persistence datastore (e.g. {@code postgresql}, {@code derby}). */
    private String datastoreType;

    /** Datastore connection host. May be null when using embedded/in-memory stores. */
    private String datastoreHost;

    /** Datastore port. 0 means "use default for the selected datastoreType". */
    private int datastorePort;

    /** Name of the database / schema to use on the datastore host. */
    private String datastoreDatabase;

    /**
     * Serialised FHIR server configuration for this tenant (JSON string).
     * This mirrors the content of a classic {@code fhir-server-config.json} file.
     */
    private String fhirServerConfig;

    /** Whether this tenant is currently active and can receive FHIR requests. */
    private boolean enabled;

    /**
     * Monotonically increasing version counter, incremented by
     * {@code POST /admin/tenants/{tenantId}/reload}.
     * FHIR server nodes poll this value to detect configuration changes
     * without direct service-to-service calls.
     * Value is 0 on creation, -1 when backed by InMemoryTenantRepository.
     */
    private long configVersion;

    /** Timestamp when the tenant was first created (epoch millis, UTC). */
    private Instant createdAt;

    /** Timestamp of the most recent update (epoch millis, UTC). */
    private Instant updatedAt;

    // ── constructors ──────────────────────────────────────────────────────────

    public TenantConfig() {
    }

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

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(Instant updatedAt) {
        this.updatedAt = updatedAt;
    }

    public long getConfigVersion() {
        return configVersion;
    }

    public void setConfigVersion(long configVersion) {
        this.configVersion = configVersion;
    }

    @Override
    public String toString() {
        return "TenantConfig{tenantId='" + tenantId + "', displayName='" + displayName
                + "', datastoreType='" + datastoreType + "', enabled=" + enabled + '}';
    }
}
