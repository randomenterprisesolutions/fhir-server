/*
 * (C) Copyright IBM Corp. 2021
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package com.randomenterprisesolutions.fhir.server.rest;

import com.randomenterprisesolutions.fhir.model.resource.Bundle.Entry;
import com.randomenterprisesolutions.fhir.model.resource.Resource;
import com.randomenterprisesolutions.fhir.persistence.context.FHIRPersistenceEvent;
import com.randomenterprisesolutions.fhir.server.spi.operation.FHIRRestOperationResponse;
import com.randomenterprisesolutions.fhir.server.util.FHIRUrlParser;

/**
 * Represents a FHIR REST CREATE interaction
 */
public class FHIRRestInteractionCreate extends FHIRRestInteractionResource {

    private final String type;
    private final String ifNoneExist;
    private final String localIdentifier;

    /**
     * Public constructor
     *
     * @param entryIndex
     * @param validationResponseEntry
     * @param requestDescription
     * @param requestURL
     * @param type
     * @param resource
     * @param ifNoneExist
     * @param localIdentifier
     */
    public FHIRRestInteractionCreate(int entryIndex, FHIRPersistenceEvent event, Entry validationResponseEntry,
            String requestDescription, FHIRUrlParser requestURL, String type, Resource resource,
            String ifNoneExist, String localIdentifier) {
        super(entryIndex, event, resource, validationResponseEntry, requestDescription, requestURL);
        this.type = type;
        this.ifNoneExist = ifNoneExist;
        this.localIdentifier = localIdentifier;
    }

    @Override
    public void process(FHIRRestInteractionVisitor visitor) throws Exception {
        FHIRRestOperationResponse result = visitor.doCreate(getEntryIndex(), getEvent(), getWarnings(),
                getValidationResponseEntry(), getRequestDescription(), getRequestURL(), getAccumulatedTime(), type,
                getNewResource(), ifNoneExist, localIdentifier, getOffloadResponse());

        // update the resource so we can use it when called in the next processing phase
        if (result != null) {
            if (result.getResource() != null) {
                setNewResource(result.getResource());
            }
            
            if (result.getStorePayloadResponse() != null) {
                setOffloadResponse(result.getStorePayloadResponse());
            }
        }
    }
}