/*
 * (C) Copyright IBM Corp. 2022
 *
 * SPDX-License-Identifier: Apache-2.0
 */
 
package com.randomenterprisesolutions.fhir.database.utils.citus;

import com.randomenterprisesolutions.fhir.database.utils.model.DbType;
import com.randomenterprisesolutions.fhir.database.utils.postgres.PostgresTranslator;


/**
 * IDatabaseTranslator implementation supporting Citus
 */
public class CitusTranslator extends PostgresTranslator {
    @Override
    public DbType getType() {
        return DbType.CITUS;
    }
}
