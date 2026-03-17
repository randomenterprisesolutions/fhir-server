/*
 * (C) Copyright IBM Corp. 2016, 2022
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package com.randomenterprisesolutions.fhir.persistence;

import com.randomenterprisesolutions.fhir.persistence.exception.FHIRPersistenceException;
import com.randomenterprisesolutions.fhir.persistence.payload.FHIRPayloadPersistence;
import com.randomenterprisesolutions.fhir.search.util.SearchHelper;

/**
 * This interface defines a factory which serves up instances of FHIRPersistence implementations.
 */
public interface FHIRPersistenceFactory {

    /**
     * Returns an instance of a concrete implementation of the FHIRPersistence interface.
     * @param searchHelper a helper for processing search requests
     * @throws FHIRPersistenceException
     */
    FHIRPersistence getInstance(SearchHelper searchHelper) throws FHIRPersistenceException;

    /**
     * Returns an instance of a concrete implementation of the FHIRPayloadPersistence interface
     * which may be used by FHIRPersistence implementations to handle storage and retrieval
     * of FHIR resource payloads.
     * @return the concrete implementation
     * @throws FHIRPersistenceException
     */
    default FHIRPayloadPersistence getPayloadPersistence() throws FHIRPersistenceException {
        return null;
    }
}