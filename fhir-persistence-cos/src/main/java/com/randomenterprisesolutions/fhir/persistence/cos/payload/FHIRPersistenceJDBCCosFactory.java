/*
 * (C) Copyright IBM Corp. 2021
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package com.randomenterprisesolutions.fhir.persistence.cos.payload;

import com.randomenterprisesolutions.fhir.persistence.exception.FHIRPersistenceException;
import com.randomenterprisesolutions.fhir.persistence.jdbc.FHIRPersistenceJDBCFactory;
import com.randomenterprisesolutions.fhir.persistence.payload.FHIRPayloadPersistence;

/**
 *
 */
public class FHIRPersistenceJDBCCosFactory extends FHIRPersistenceJDBCFactory {

    @Override
    public FHIRPayloadPersistence getPayloadPersistence() throws FHIRPersistenceException {
        // Store the payload in Cloud Object Storage (Cos)
        return new FHIRPayloadPersistenceCosImpl();
    };
}