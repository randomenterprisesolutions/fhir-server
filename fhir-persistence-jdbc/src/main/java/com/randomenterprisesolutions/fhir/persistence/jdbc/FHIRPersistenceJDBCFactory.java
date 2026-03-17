/*
 * (C) Copyright IBM Corp. 2017, 2022
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package com.randomenterprisesolutions.fhir.persistence.jdbc;


import com.randomenterprisesolutions.fhir.persistence.FHIRPersistence;
import com.randomenterprisesolutions.fhir.persistence.FHIRPersistenceFactory;
import com.randomenterprisesolutions.fhir.persistence.exception.FHIRPersistenceException;
import com.randomenterprisesolutions.fhir.persistence.jdbc.cache.FHIRPersistenceJDBCTenantCache;
import com.randomenterprisesolutions.fhir.persistence.jdbc.impl.FHIRPersistenceJDBCImpl;
import com.randomenterprisesolutions.fhir.search.util.SearchHelper;

/**
 * Factory which serves up instances of the JDBC persistence implementation.
 */
public class FHIRPersistenceJDBCFactory implements FHIRPersistenceFactory {

    // All instances created by this factory share the common cache object (which is tenant-aware)
    private final FHIRPersistenceJDBCTenantCache tenantCache = new FHIRPersistenceJDBCTenantCache();

    @Override
    public FHIRPersistence getInstance(SearchHelper searchHelper) throws FHIRPersistenceException {
        try {
            // each request gets a new instance of the FHIRPersistenceJDBCImpl, sharing
            // the common (tenant-aware) cache object
            FHIRPersistenceJDBCCache cache = tenantCache.getCacheForTenantAndDatasource();
            return new FHIRPersistenceJDBCImpl(cache, getPayloadPersistence(), searchHelper);
        } catch (Exception e) {
            throw new FHIRPersistenceException("Unexpected exception while creating JDBC persistence layer: '" + e.getMessage() + "'", e);
        }
    }
}
