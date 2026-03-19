/*
 * (C) Copyright Random Enterprise Solutions 2026
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package com.randomenterprisesolutions.fhir.admin.service;

import java.util.List;
import java.util.logging.Logger;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import com.randomenterprisesolutions.fhir.admin.exception.InvalidTenantRequestException;
import com.randomenterprisesolutions.fhir.admin.exception.TenantNotFoundException;
import com.randomenterprisesolutions.fhir.admin.model.CreateTenantRequest;
import com.randomenterprisesolutions.fhir.admin.model.TenantConfig;
import com.randomenterprisesolutions.fhir.admin.model.UpdateTenantRequest;
import com.randomenterprisesolutions.fhir.admin.repository.JdbcTenantRepository;
import com.randomenterprisesolutions.fhir.admin.repository.TenantRepository;
import com.randomenterprisesolutions.fhir.admin.schema.SchemaProvisioner;

/**
 * Business-logic layer for tenant lifecycle management.
 *
 * <p>This service is the single authoritative entry-point for all tenant
 * create / read / update / delete operations. It performs input validation,
 * maps DTOs to domain objects, delegates persistence to {@link TenantRepository},
 * and (via {@link SchemaProvisioner}) triggers FHIR persistence schema
 * provisioning on tenant creation.
 */
@ApplicationScoped
public class TenantService {

    private static final Logger log = Logger.getLogger(TenantService.class.getName());

    /** Pattern: alphanumeric plus hyphens and underscores, 1-64 chars. */
    private static final java.util.regex.Pattern TENANT_ID_PATTERN =
            java.util.regex.Pattern.compile("^[a-zA-Z0-9_-]{1,64}$");

    @Inject
    private TenantRepository repository;

    @Inject
    private SchemaProvisioner schemaProvisioner;

    // ── constructors for direct instantiation (tests, non-CDI) ───────────────

    /**
     * Creates a {@code TenantService} with the given repository and a
     * {@link com.randomenterprisesolutions.fhir.admin.schema.NoOpSchemaProvisioner}.
     * Intended for non-CDI contexts such as unit and integration tests.
     *
     * @param repository repository implementation to use
     */
    public TenantService(TenantRepository repository) {
        this(repository,
             new com.randomenterprisesolutions.fhir.admin.schema.NoOpSchemaProvisioner());
    }

    /**
     * Creates a {@code TenantService} with explicit repository and provisioner.
     * Intended for non-CDI contexts such as integration tests.
     *
     * @param repository  repository implementation to use
     * @param provisioner schema provisioner to call after tenant creation
     */
    public TenantService(TenantRepository repository, SchemaProvisioner provisioner) {
        this.repository = repository;
        this.schemaProvisioner = provisioner;
    }

    /** CDI no-arg constructor. */
    public TenantService() {
    }

    // ── public API ───────────────────────────────────────────────────────────

    /**
     * Create a new tenant from the supplied request.
     *
     * @param request create request (tenantId and datastoreType are required)
     * @return the persisted {@link TenantConfig}
     * @throws InvalidTenantRequestException if required fields are missing or
     *                                        the tenantId violates the naming rules
     * @throws com.randomenterprisesolutions.fhir.admin.exception.TenantAlreadyExistsException
     *                                        if the tenantId is already taken
     */
    public TenantConfig createTenant(CreateTenantRequest request) {
        validateCreateRequest(request);

        TenantConfig config = new TenantConfig();
        config.setTenantId(request.getTenantId().trim());
        config.setDisplayName(
                request.getDisplayName() != null ? request.getDisplayName() : request.getTenantId());
        config.setDatastoreType(request.getDatastoreType());
        config.setDatastoreHost(request.getDatastoreHost());
        config.setDatastorePort(request.getDatastorePort());
        config.setDatastoreDatabase(request.getDatastoreDatabase());
        config.setFhirServerConfig(request.getFhirServerConfig());

        TenantConfig saved = repository.create(config);
        log.info("Tenant created: " + saved.getTenantId());

        // Invoke schema provisioner so the FHIR persistence schema is set up
        // for the new tenant.  The default is NoOpSchemaProvisioner which logs
        // manual instructions; replace with a real implementation to automate DDL.
        schemaProvisioner.provision(saved);

        return saved;
    }

    /**
     * Return all registered tenants.
     *
     * @return list of all tenants; never null
     */
    public List<TenantConfig> listTenants() {
        return repository.findAll();
    }

    /**
     * Retrieve a single tenant by ID.
     *
     * @param tenantId the tenant identifier
     * @return the matching {@link TenantConfig}
     * @throws TenantNotFoundException if no tenant with the given ID exists
     */
    public TenantConfig getTenant(String tenantId) {
        return repository.findById(tenantId)
                .orElseThrow(() -> new TenantNotFoundException(tenantId));
    }

    /**
     * Apply a partial update to an existing tenant.
     *
     * <p>Only non-null fields in {@code request} are written; existing values are
     * preserved for fields that are absent from the request.
     *
     * @param tenantId the tenant identifier
     * @param request  fields to update
     * @return the updated {@link TenantConfig}
     * @throws TenantNotFoundException if no tenant with the given ID exists
     */
    public TenantConfig updateTenant(String tenantId, UpdateTenantRequest request) {
        TenantConfig existing = repository.findById(tenantId)
                .orElseThrow(() -> new TenantNotFoundException(tenantId));

        if (request.getDisplayName() != null) {
            existing.setDisplayName(request.getDisplayName());
        }
        if (request.getDatastoreType() != null) {
            existing.setDatastoreType(request.getDatastoreType());
        }
        if (request.getDatastoreHost() != null) {
            existing.setDatastoreHost(request.getDatastoreHost());
        }
        if (request.getDatastorePort() != null) {
            existing.setDatastorePort(request.getDatastorePort());
        }
        if (request.getDatastoreDatabase() != null) {
            existing.setDatastoreDatabase(request.getDatastoreDatabase());
        }
        if (request.getFhirServerConfig() != null) {
            existing.setFhirServerConfig(request.getFhirServerConfig());
        }
        if (request.getEnabled() != null) {
            existing.setEnabled(request.getEnabled());
        }

        return repository.update(existing);
    }

    /**
     * Delete (or disable) a tenant.
     *
     * @param tenantId the tenant identifier
     * @throws TenantNotFoundException if no tenant with the given ID exists
     */
    public void deleteTenant(String tenantId) {
        // Existence check gives a cleaner exception before the repository call
        repository.findById(tenantId)
                .orElseThrow(() -> new TenantNotFoundException(tenantId));
        repository.delete(tenantId);
        log.info("Tenant deleted: " + tenantId);
    }

    /**
     * Force a config-cache reload for the given tenant across all nodes.
     *
     * <p>Bumps the {@code CONFIG_VERSION} counter when backed by
     * {@link JdbcTenantRepository} so that FHIR server nodes detect the change.
     *
     * @param tenantId the tenant identifier
     * @throws TenantNotFoundException if no tenant with the given ID exists
     */
    public void reloadTenant(String tenantId) {
        // Assert the tenant exists
        getTenant(tenantId);

        // If using JDBC persistence, bump the config_version counter so that
        // FHIR server nodes detect the change via
        // DatabaseAwareTenantPropertyGroupCache on their next config access.
        if (repository instanceof JdbcTenantRepository) {
            long newVersion = ((JdbcTenantRepository) repository).bumpConfigVersion(tenantId);
            log.info("Cache reload requested for tenant '" + tenantId
                    + "' — config_version bumped to " + newVersion);
        } else {
            log.info("Cache reload requested for tenant '" + tenantId
                    + "' (in-memory store — config_version not tracked)");
        }
    }

    // ── private helpers ──────────────────────────────────────────────────────

    private void validateCreateRequest(CreateTenantRequest request) {
        if (request == null) {
            throw new InvalidTenantRequestException("Request body must not be null");
        }
        if (request.getTenantId() == null || request.getTenantId().isBlank()) {
            throw new InvalidTenantRequestException("tenantId is required");
        }
        if (!TENANT_ID_PATTERN.matcher(request.getTenantId().trim()).matches()) {
            throw new InvalidTenantRequestException(
                    "tenantId must match pattern ^[a-zA-Z0-9_-]{1,64}$; got: '"
                            + request.getTenantId() + "'");
        }
        if (request.getDatastoreType() == null || request.getDatastoreType().isBlank()) {
            throw new InvalidTenantRequestException("datastoreType is required");
        }
    }
}
