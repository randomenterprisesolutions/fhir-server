/*
 * (C) Copyright IBM Corp. 2019, 2022
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package com.randomenterprisesolutions.fhir.operation.test;

import com.randomenterprisesolutions.fhir.exception.FHIROperationException;
import com.randomenterprisesolutions.fhir.model.resource.OperationDefinition;
import com.randomenterprisesolutions.fhir.model.resource.Parameters;
import com.randomenterprisesolutions.fhir.model.resource.Resource;
import com.randomenterprisesolutions.fhir.search.util.SearchHelper;
import com.randomenterprisesolutions.fhir.server.spi.operation.AbstractOperation;
import com.randomenterprisesolutions.fhir.server.spi.operation.FHIROperationContext;
import com.randomenterprisesolutions.fhir.server.spi.operation.FHIRResourceHelpers;

/**
 * This class will test what happens if there is an Operation that fails to initialize.
 * There is no corresponding testcase as the Java ServiceLoader (SPI) mechanism
 * will automatically load this service if it is configured as a service provider and available on the classpath.
 * The expected result is:
 * 1. to see an error/message explaining why this service was not loaded
 * 2. for other operations to continue working
 */
public class CrashingOperation extends AbstractOperation {
    @Override
    protected OperationDefinition buildOperationDefinition() {
        throw new RuntimeException("Testing an operation that fails to initialize");
    }

    @Override
    protected Parameters doInvoke(FHIROperationContext operationContext, Class<? extends Resource> resourceType, String logicalId, String versionId,
            Parameters parameters, FHIRResourceHelpers resourceHelper, SearchHelper searchHelper) throws FHIROperationException {
        // do nothing
        return null;
    }
}
