/*
 * (C) Copyright Random Enterprise Solutions 2026
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package com.randomenterprisesolutions.fhir.admin.repository;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Logger;

import com.randomenterprisesolutions.fhir.admin.exception.TenantAlreadyExistsException;
import com.randomenterprisesolutions.fhir.admin.exception.TenantNotFoundException;
import com.randomenterprisesolutions.fhir.admin.model.TenantConfig;

/**
 * Thread-safe, in-memory implementation of {@link TenantRepository}.
 *
 * <p>All data is stored in a {@link ConcurrentHashMap} and is lost when the JVM
 * restarts. This implementation is suitable for:
 * <ul>
 *   <li>local development and smoke-testing</li>
 *   <li>unit and integration tests that do not require persistence</li>
 * </ul>
 *
 * <p>The CDI lifecycle of this class is managed by {@link TenantRepositoryProducer};
 * it is therefore <em>not</em> annotated with {@code @ApplicationScoped} directly.
 * Set {@code FHIR_ADMIN_DB_URL} to switch to {@link JdbcTenantRepository}.
 */
public class InMemoryTenantRepository implements TenantRepository {

    private static final Logger log = Logger.getLogger(InMemoryTenantRepository.class.getName());

    /** Backing store: tenantId → TenantConfig. */
    private final ConcurrentHashMap<String, TenantConfig> store = new ConcurrentHashMap<>();

    @Override
    public TenantConfig create(TenantConfig config) {
        Instant now = Instant.now();
        config.setCreatedAt(now);
        config.setUpdatedAt(now);
        config.setEnabled(true);

        TenantConfig previous = store.putIfAbsent(config.getTenantId(), config);
        if (previous != null) {
            throw new TenantAlreadyExistsException(config.getTenantId());
        }
        log.info("Created tenant: " + config.getTenantId());
        return config;
    }

    @Override
    public Optional<TenantConfig> findById(String tenantId) {
        return Optional.ofNullable(store.get(tenantId));
    }

    @Override
    public List<TenantConfig> findAll() {
        return Collections.unmodifiableList(new ArrayList<>(store.values()));
    }

    @Override
    public TenantConfig update(TenantConfig config) {
        String tenantId = config.getTenantId();
        if (!store.containsKey(tenantId)) {
            throw new TenantNotFoundException(tenantId);
        }
        config.setUpdatedAt(Instant.now());
        store.put(tenantId, config);
        log.info("Updated tenant: " + tenantId);
        return config;
    }

    @Override
    public void delete(String tenantId) {
        TenantConfig removed = store.remove(tenantId);
        if (removed == null) {
            throw new TenantNotFoundException(tenantId);
        }
        log.info("Deleted tenant: " + tenantId);
    }
}
