/*
 * (C) Copyright IBM Corp. 2017, 2022
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package com.randomenterprisesolutions.fhir.persistence.test;

import com.randomenterprisesolutions.fhir.persistence.FHIRPersistence;
import com.randomenterprisesolutions.fhir.persistence.FHIRPersistenceFactory;
import com.randomenterprisesolutions.fhir.persistence.exception.FHIRPersistenceException;
import com.randomenterprisesolutions.fhir.search.util.SearchHelper;


/**
 * Mock persistence factory for use during testing.
 */
public class MockExceptionPersistenceFactory implements FHIRPersistenceFactory {

    /**
     * Always throws a NullPointerException
     */
    @SuppressWarnings("null")
    public MockExceptionPersistenceFactory() {
        String a = null;
        a.toString();
    }

    @Override
    public FHIRPersistence getInstance(SearchHelper searchHelper) throws FHIRPersistenceException {
        return null;
    }
}
