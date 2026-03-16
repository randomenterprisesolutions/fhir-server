/*
 * (C) Copyright IBM Corp. 2022
 *
 * SPDX-License-Identifier: Apache-2.0
 */
 
package com.randomenterprisesolutions.fhir.persistence.params.batch;

import com.randomenterprisesolutions.fhir.persistence.exception.FHIRPersistenceException;
import com.randomenterprisesolutions.fhir.persistence.index.SecurityParameter;
import com.randomenterprisesolutions.fhir.persistence.params.api.IBatchParameterProcessor;
import com.randomenterprisesolutions.fhir.persistence.params.api.BatchParameterValue;
import com.randomenterprisesolutions.fhir.persistence.params.model.CommonTokenValue;
import com.randomenterprisesolutions.fhir.persistence.params.model.ParameterNameValue;

/**
 * A security parameter we are collecting to batch
 */
public class BatchSecurityParameter extends BatchParameterValue {
    private final SecurityParameter parameter;
    private final CommonTokenValue commonTokenValue;
    
    /**
     * Canonical constructor
     * 
     * @param requestShard
     * @param resourceType
     * @param logicalId
     * @param logicalResourceId
     * @param parameterNameValue
     * @param parameter
     * @param commonTokenValue
     */
    public BatchSecurityParameter(String requestShard, String resourceType, String logicalId, long logicalResourceId, ParameterNameValue parameterNameValue, SecurityParameter parameter, CommonTokenValue commonTokenValue) {
        super(requestShard, resourceType, logicalId, logicalResourceId, parameterNameValue);
        this.parameter = parameter;
        this.commonTokenValue = commonTokenValue;
    }

    @Override
    public void apply(IBatchParameterProcessor processor) throws FHIRPersistenceException {
        processor.process(requestShard, resourceType, logicalId, logicalResourceId, parameterNameValue, parameter, commonTokenValue);
    }
}
