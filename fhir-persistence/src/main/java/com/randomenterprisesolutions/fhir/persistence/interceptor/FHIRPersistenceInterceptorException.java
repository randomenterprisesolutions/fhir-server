/*
 * (C) Copyright IBM Corp. 2016,2019
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package com.randomenterprisesolutions.fhir.persistence.interceptor;

import java.util.Collection;

import com.randomenterprisesolutions.fhir.model.resource.OperationOutcome;
import com.randomenterprisesolutions.fhir.persistence.exception.FHIRPersistenceException;

/**
 * @deprecated moved to com.randomenterprisesolutions.fhir.server.interceptor in fhir-server
 */
@Deprecated
public class FHIRPersistenceInterceptorException extends FHIRPersistenceException {
    private static final long serialVersionUID = 1L;

    public FHIRPersistenceInterceptorException(String message) {
        super(message);
    }

    public FHIRPersistenceInterceptorException(String message, Throwable cause) {
        super(message, cause);
    }

    @Override
    public FHIRPersistenceInterceptorException withIssue(OperationOutcome.Issue... issues) {
        super.withIssue(issues);
        return this;
    }

    @Override
    public FHIRPersistenceInterceptorException withIssue(Collection<OperationOutcome.Issue> issues) {
        super.withIssue(issues);
        return this;
    }

}
