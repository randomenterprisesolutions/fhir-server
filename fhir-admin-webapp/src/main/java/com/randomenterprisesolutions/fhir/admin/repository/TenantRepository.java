/*
 * (C) Copyright Random Enterprise Solutions 2026
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package com.randomenterprisesolutions.fhir.admin.repository;

import java.util.List;
import java.util.Optional;

import com.randomenterprisesolutions.fhir.admin.model.TenantConfig;

/**
 * Data access interface for tenant configuration records.
 *
 * <p>Implementations must be thread-safe. The in-memory implementation
 * ({@link InMemoryTenantRepository}) is used for development and testing;
 * the JDBC implementation ({@link JdbcTenantRepository}) is used for production.
 */
public interface TenantRepository {

    /**
     * Persist a new tenant record.
     *
     * @param config the tenant configuration to store; must have a non-blank
     *               {@code tenantId}
     * @return the stored record (may be a new object with server-assigned
     *         timestamps)
     * @throws com.randomenterprisesolutions.fhir.admin.exception.TenantAlreadyExistsException
     *         if a tenant with the same ID already exists
     */
    TenantConfig create(TenantConfig config);

    /**
     * Look up a single tenant by ID.
     *
     * @param tenantId the tenant identifier
     * @return an {@link Optional} containing the record, or empty if not found
     */
    Optional<TenantConfig> findById(String tenantId);

    /**
     * Return all registered tenants, regardless of enabled/disabled state.
     *
     * @return an unmodifiable list; never null
     */
    List<TenantConfig> findAll();

    /**
     * Replace the stored tenant record with the supplied value.
     *
     * @param config updated tenant configuration; must carry an existing
     *               {@code tenantId}
     * @return the updated record
     * @throws com.randomenterprisesolutions.fhir.admin.exception.TenantNotFoundException
     *         if no record with the given ID exists
     */
    TenantConfig update(TenantConfig config);

    /**
     * Remove the tenant record identified by {@code tenantId}.
     *
     * @param tenantId the tenant identifier to remove
     * @throws com.randomenterprisesolutions.fhir.admin.exception.TenantNotFoundException
     *         if no record with the given ID exists
     */
    void delete(String tenantId);
}
