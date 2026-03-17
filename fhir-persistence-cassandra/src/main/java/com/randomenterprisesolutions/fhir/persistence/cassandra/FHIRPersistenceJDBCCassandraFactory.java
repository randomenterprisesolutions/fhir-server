/*
 * (C) Copyright IBM Corp. 2020, 2022
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package com.randomenterprisesolutions.fhir.persistence.cassandra;

import com.randomenterprisesolutions.fhir.persistence.cassandra.cql.DatasourceSessions;
import com.randomenterprisesolutions.fhir.persistence.cassandra.payload.FHIRPayloadPersistenceCassandraImpl;
import com.randomenterprisesolutions.fhir.persistence.exception.FHIRPersistenceException;
import com.randomenterprisesolutions.fhir.persistence.jdbc.FHIRPersistenceJDBCFactory;
import com.randomenterprisesolutions.fhir.persistence.payload.FHIRPayloadPersistence;

/**
 * Factory for creating a hybrid JDBC/Cassandra persistence implementation
 */
public class FHIRPersistenceJDBCCassandraFactory extends FHIRPersistenceJDBCFactory {

    @Override
    public FHIRPayloadPersistence getPayloadPersistence() throws FHIRPersistenceException {
        
        // If payload persistence is configured for this tenant, provide
        // the impl otherwise null
        FHIRPayloadPersistence result = null;
        if (DatasourceSessions.isPayloadPersistenceConfigured()) {
            result = new FHIRPayloadPersistenceCassandraImpl();
        }
        
        return result;
    }
}
