/*
 * (C) Copyright IBM Corp. 2020, 2022
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package com.randomenterprisesolutions.fhir.persistence.jdbc.search.test;

import com.randomenterprisesolutions.fhir.config.FHIRConfigProvider;
import com.randomenterprisesolutions.fhir.persistence.FHIRPersistence;
import com.randomenterprisesolutions.fhir.persistence.jdbc.test.util.PersistenceTestSupport;
import com.randomenterprisesolutions.fhir.persistence.search.test.AbstractSearchIdAndLastUpdatedTest;
import com.randomenterprisesolutions.fhir.search.util.SearchHelper;

public class JDBCSearchIdLastUpdatedTest extends AbstractSearchIdAndLastUpdatedTest {
    // Container to hide the instantiation of the persistence impl used for tests
    private PersistenceTestSupport testSupport;

    @Override
    public void bootstrapDatabase() throws Exception {
        testSupport = new PersistenceTestSupport();
    }

    @Override
    public FHIRPersistence getPersistenceImpl(FHIRConfigProvider configProvider, SearchHelper searchHelper) throws Exception {
        return testSupport.getPersistenceImpl(configProvider, searchHelper);
    }

    @Override
    protected void shutdownPools() throws Exception {
        if (testSupport != null) {
            testSupport.shutdown();
        }
    }
}
