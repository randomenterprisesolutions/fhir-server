/*
 * (C) Copyright IBM Corp. 2022
 *
 * SPDX-License-Identifier: Apache-2.0
 */
 
package com.randomenterprisesolutions.fhir.persistence.params.api;

import com.randomenterprisesolutions.fhir.persistence.exception.FHIRPersistenceException;
import com.randomenterprisesolutions.fhir.persistence.index.DateParameter;
import com.randomenterprisesolutions.fhir.persistence.index.LocationParameter;
import com.randomenterprisesolutions.fhir.persistence.index.NumberParameter;
import com.randomenterprisesolutions.fhir.persistence.index.ProfileParameter;
import com.randomenterprisesolutions.fhir.persistence.index.QuantityParameter;
import com.randomenterprisesolutions.fhir.persistence.index.ReferenceParameter;
import com.randomenterprisesolutions.fhir.persistence.index.SecurityParameter;
import com.randomenterprisesolutions.fhir.persistence.index.StringParameter;
import com.randomenterprisesolutions.fhir.persistence.index.TagParameter;
import com.randomenterprisesolutions.fhir.persistence.index.TokenParameter;
import com.randomenterprisesolutions.fhir.persistence.params.model.CodeSystemValue;
import com.randomenterprisesolutions.fhir.persistence.params.model.CommonCanonicalValue;
import com.randomenterprisesolutions.fhir.persistence.params.model.CommonTokenValue;
import com.randomenterprisesolutions.fhir.persistence.params.model.LogicalResourceIdentValue;
import com.randomenterprisesolutions.fhir.persistence.params.model.ParameterNameValue;

/**
 * Processes batched parameters
 */
public interface IBatchParameterProcessor {

    /**
     * Compute the shard key value use to distribute resources among nodes
     * of the database
     * @param requestShard
     * @return
     */
    Short encodeShardKey(String requestShard);

    /**
     * @param requestShard
     * @param resourceType
     * @param logicalId
     * @param logicalResourceId
     * @param parameterNameValue
     * @param parameter
     */
    void process(String requestShard, String resourceType, String logicalId, long logicalResourceId, ParameterNameValue parameterNameValue, StringParameter parameter) throws FHIRPersistenceException;

    /**
     * @param requestShard
     * @param resourceType
     * @param logicalId
     * @param logicalResourceId
     * @param parameterNameValue
     * @param parameter
     */
    void process(String requestShard, String resourceType, String logicalId, long logicalResourceId, ParameterNameValue parameterNameValue, NumberParameter parameter) throws FHIRPersistenceException;

    /**
     * @param requestShard
     * @param resourceType
     * @param logicalId
     * @param logicalResourceId
     * @param parameterNameValue
     * @param parameter
     */
    void process(String requestShard, String resourceType, String logicalId, long logicalResourceId, ParameterNameValue parameterNameValue, QuantityParameter parameter, CodeSystemValue codeSystemValue) throws FHIRPersistenceException;

    /**
     * @param requestShard
     * @param resourceType
     * @param logicalId
     * @param logicalResourceId
     * @param parameterNameValue
     * @param parameter
     */
    void process(String requestShard, String resourceType, String logicalId, long logicalResourceId, ParameterNameValue parameterNameValue, LocationParameter parameter) throws FHIRPersistenceException;

    /**
     * @param requestShard
     * @param resourceType
     * @param logicalId
     * @param logicalResourceId
     * @param parameterNameValue
     * @param parameter
     */
    void process(String requestShard, String resourceType, String logicalId, long logicalResourceId, ParameterNameValue parameterNameValue, DateParameter parameter) throws FHIRPersistenceException;

    /**
     * @param requestShard
     * @param resourceType
     * @param logicalId
     * @param logicalResourceId
     * @param parameterNameValue
     * @param parameter
     */
    void process(String requestShard, String resourceType, String logicalId, long logicalResourceId, ParameterNameValue parameterNameValue, TokenParameter parameter, CommonTokenValue commonTokenValue) throws FHIRPersistenceException;

    /**
     * 
     * @param requestShard
     * @param resourceType
     * @param logicalId
     * @param logicalResourceId
     * @param parameterNameValue
     * @param parameter
     * @param commonTokenValue
     * @throws FHIRPersistenceException
     */
    void process(String requestShard, String resourceType, String logicalId, long logicalResourceId, ParameterNameValue parameterNameValue, TagParameter parameter, CommonTokenValue commonTokenValue) throws FHIRPersistenceException;

    /**
     * 
     * @param requestShard
     * @param resourceType
     * @param logicalId
     * @param logicalResourceId
     * @param parameterNameValue
     * @param parameter
     * @param commonCanonicalValue
     * @throws FHIRPersistenceException
     */
    void process(String requestShard, String resourceType, String logicalId, long logicalResourceId, ParameterNameValue parameterNameValue, ProfileParameter parameter, CommonCanonicalValue commonCanonicalValue) throws FHIRPersistenceException;

    /**
     * 
     * @param requestShard
     * @param resourceType
     * @param logicalId
     * @param logicalResourceId
     * @param parameterNameValue
     * @param parameter
     * @param commonCanonicalValue
     * @throws FHIRPersistenceException
     */
    void process(String requestShard, String resourceType, String logicalId, long logicalResourceId, ParameterNameValue parameterNameValue, SecurityParameter parameter, CommonTokenValue commonTokenValue) throws FHIRPersistenceException;

    /**
     * @param requestShard
     * @param resourceType
     * @param logicalId
     * @param logicalResourceId
     * @param parameterNameValue
     * @param parameter
     * @param refLogicalResourceId
     */
    void process(String requestShard, String resourceType, String logicalId, long logicalResourceId, ParameterNameValue parameterNameValue,
        ReferenceParameter parameter, LogicalResourceIdentValue refLogicalResourceId) throws FHIRPersistenceException;
}
