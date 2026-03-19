/*
 * (C) Copyright Random Enterprise Solutions 2026
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package com.randomenterprisesolutions.fhir.config;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Tenant config cache that prefers the admin database over on-disk JSON files.
 *
 * <p>When {@link FHIRConfiguration} detects the {@code FHIR_ADMIN_DB_URL}
 * environment variable it instantiates this class instead of the plain
 * {@link TenantSpecificPropertyGroupCache}.
 *
 * <h4>Resolution order for {@code getCachedObjectForTenant(tenantId)}</h4>
 * <ol>
 *   <li>Query the admin DB for the tenant's {@code FHIR_SERVER_CONFIG} JSON
 *       and {@code CONFIG_VERSION}.</li>
 *   <li>If the DB version matches the last version seen, return the existing
 *       in-memory {@link PropertyGroup} (cache hit).</li>
 *   <li>If the DB version changed (admin reloaded the tenant), parse the JSON
 *       and update the in-memory cache.</li>
 *   <li>If the DB has no config for this tenant (or is unreachable), fall back
 *       to the file-based superclass logic so that file-only tenants continue
 *       to work transparently (backward-compatible).</li>
 * </ol>
 */
public class DatabaseAwareTenantPropertyGroupCache extends TenantSpecificPropertyGroupCache {

    private static final Logger log =
            Logger.getLogger(DatabaseAwareTenantPropertyGroupCache.class.getName());

    private final DatabaseTenantConfigSource dbSource;

    /** Tracks the {@code CONFIG_VERSION} last successfully loaded per tenant. */
    private final Map<String, Long> lastVersionSeen = new ConcurrentHashMap<>();

    /** In-memory cache for DB-sourced PropertyGroups, separate from the file cache. */
    private final Map<String, PropertyGroup> dbCache = new ConcurrentHashMap<>();

    /**
     * Creates a cache backed by the supplied database source.
     *
     * @param dbSource the admin database config source; never null
     */
    public DatabaseAwareTenantPropertyGroupCache(DatabaseTenantConfigSource dbSource) {
        super();
        this.dbSource = dbSource;
    }

    /**
     * Resolves the tenant configuration, preferring the admin database over
     * on-disk files.
     *
     * @param tenantId the tenant identifier
     * @return the resolved {@link PropertyGroup}, or {@code null} if not found
     *         in either the database or the file system
     * @throws Exception if the file-based superclass throws
     */
    @Override
    public PropertyGroup getCachedObjectForTenant(String tenantId) throws Exception {

        // 1. Try admin DB
        try {
            long dbVersion = dbSource.getConfigVersion(tenantId);
            if (dbVersion >= 0) {
                // Tenant exists in DB; check version against what we cached
                Long cachedVersion = lastVersionSeen.get(tenantId);
                PropertyGroup cached = dbCache.get(tenantId);

                if (cached != null && cachedVersion != null && cachedVersion == dbVersion) {
                    // Config unchanged since last load — return in-memory entry
                    log.finest("DB cache hit for tenant '" + tenantId
                            + "' (config_version=" + dbVersion + ")");
                    return cached;
                }

                // Version changed or first load — reload from DB
                String json = dbSource.getConfigJson(tenantId);
                if (json != null) {
                    PropertyGroup pg = ConfigurationService.loadConfiguration(
                            new ByteArrayInputStream(json.getBytes(StandardCharsets.UTF_8)));
                    dbCache.put(tenantId, pg);
                    lastVersionSeen.put(tenantId, dbVersion);
                    log.info("Loaded config for tenant '" + tenantId
                            + "' from admin DB (config_version=" + dbVersion + ")");
                    return pg;
                }
                // DB row exists but FHIR_SERVER_CONFIG is null — fall through to file
                log.fine("Tenant '" + tenantId
                        + "' found in admin DB but FHIR_SERVER_CONFIG is empty"
                        + " — falling back to file-based config");
            }
        } catch (Exception ex) {
            log.log(Level.WARNING,
                    "Error reading config for tenant '" + tenantId
                    + "' from admin DB — falling back to file-based config", ex);
        }

        // 2. Fall back to file-based config (existing behaviour)
        return super.getCachedObjectForTenant(tenantId);
    }

    /**
     * Clears both the DB-derived in-process cache and the file-based cache.
     */
    @Override
    public void clearCache() {
        dbCache.clear();
        lastVersionSeen.clear();
        super.clearCache();
    }

    /** Exposes the underlying DB source for testing. */
    DatabaseTenantConfigSource getDbSource() {
        return dbSource;
    }

    /**
     * Test helper: returns the last config_version seen for the given tenant,
     * or -1 if the tenant has not been loaded yet.
     */
    long getLastVersionSeen(String tenantId) {
        Long v = lastVersionSeen.get(tenantId);
        return v != null ? v : -1;
    }
}
