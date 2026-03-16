/*
 * (C) Copyright IBM Corp. 2021
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package com.randomenterprisesolutions.fhir.persistence.jdbc.domain;

import com.randomenterprisesolutions.fhir.persistence.exception.FHIRPersistenceException;
import com.randomenterprisesolutions.fhir.search.parameters.QueryParameter;

/**
 * A quantity search parameter
 */
public class QuantitySearchParam extends SearchParam {

    /**
     * Public constructor
     * @param rootResourceType
     * @param name
     * @param queryParameter the search query parameter being wrapped
     */
    public QuantitySearchParam(String rootResourceType, String name, QueryParameter queryParameter) {
        super(rootResourceType, name, queryParameter);
    }

    @Override
    public <T> T visit(T queryData, SearchQueryVisitor<T> visitor) throws FHIRPersistenceException {
        return visitor.addQuantityParam(queryData, getRootResourceType(), getQueryParameter());
    }
}