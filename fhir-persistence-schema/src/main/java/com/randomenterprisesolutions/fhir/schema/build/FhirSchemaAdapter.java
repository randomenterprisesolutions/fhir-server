/*
 * (C) Copyright IBM Corp. 2022
 *
 * SPDX-License-Identifier: Apache-2.0
 */
 
package com.randomenterprisesolutions.fhir.schema.build;

import com.randomenterprisesolutions.fhir.database.utils.api.IDatabaseAdapter;
import com.randomenterprisesolutions.fhir.database.utils.common.PlainSchemaAdapter;

/**
 * Represents an adapter used to build the standard FHIR schema
 */
public class FhirSchemaAdapter extends PlainSchemaAdapter {

    /**
     * Public constructor
     * 
     * @param databaseAdapter
     */
    public FhirSchemaAdapter(IDatabaseAdapter databaseAdapter) {
        super(databaseAdapter);
    }
}
