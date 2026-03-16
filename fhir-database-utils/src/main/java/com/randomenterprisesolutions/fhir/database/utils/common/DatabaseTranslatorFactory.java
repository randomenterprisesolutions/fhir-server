/*
 * (C) Copyright IBM Corp. 2021
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package com.randomenterprisesolutions.fhir.database.utils.common;

import com.randomenterprisesolutions.fhir.database.utils.api.IDatabaseTranslator;
import com.randomenterprisesolutions.fhir.database.utils.citus.CitusTranslator;
import com.randomenterprisesolutions.fhir.database.utils.derby.DerbyTranslator;
import com.randomenterprisesolutions.fhir.database.utils.model.DbType;
import com.randomenterprisesolutions.fhir.database.utils.postgres.PostgresTranslator;

/**
 * Factory class for creating instances of the {@link IDatabaseTranslator}
 * interface
 */
public class DatabaseTranslatorFactory {
    /**
     * Get the translator appropriate for the given database type
     * @return
     */
    public static IDatabaseTranslator getTranslator(DbType type) {
        final IDatabaseTranslator result;

        switch (type) {
        case DERBY:
            result = new DerbyTranslator();
            break;
        case POSTGRESQL:
            result = new PostgresTranslator();
            break;
        case CITUS:
            result = new CitusTranslator();
            break;
        default:
            throw new IllegalStateException("DbType not supported: " + type);
        }

        return result;
    }

}
