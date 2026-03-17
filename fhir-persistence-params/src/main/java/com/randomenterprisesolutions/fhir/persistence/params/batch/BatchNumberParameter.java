/*
 * (C) Copyright IBM Corp. 2022
 *
 * SPDX-License-Identifier: Apache-2.0
 */
 
package com.randomenterprisesolutions.fhir.persistence.params.batch;

import com.randomenterprisesolutions.fhir.persistence.exception.FHIRPersistenceException;
import com.randomenterprisesolutions.fhir.persistence.index.NumberParameter;
import com.randomenterprisesolutions.fhir.persistence.params.api.IBatchParameterProcessor;
import com.randomenterprisesolutions.fhir.persistence.params.api.BatchParameterValue;
import com.randomenterprisesolutions.fhir.persistence.params.model.ParameterNameValue;

/**
 * A number parameter we are collecting to batch
 */
public class BatchNumberParameter extends BatchParameterValue {
    private final NumberParameter parameter;
    
    /**
     * Canonical constructor
     * 
     * @param requestShard
     * @param resourceType
     * @param logicalId
     * @param logicalResourceId
     * @param parameterNameValue
     * @param parameter
     */
    public BatchNumberParameter(String requestShard, String resourceType, String logicalId, long logicalResourceId, ParameterNameValue parameterNameValue, NumberParameter parameter) {
        super(requestShard, resourceType, logicalId, logicalResourceId, parameterNameValue);
        this.parameter = parameter;
    }

    @Override
    public void apply(IBatchParameterProcessor processor) throws FHIRPersistenceException {
        processor.process(requestShard, resourceType, logicalId, logicalResourceId, parameterNameValue, parameter);
    }
}
