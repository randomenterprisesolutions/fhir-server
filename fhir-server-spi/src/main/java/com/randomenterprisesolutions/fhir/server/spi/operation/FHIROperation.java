/*
 * (C) Copyright IBM Corp. 2016, 2022
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package com.randomenterprisesolutions.fhir.server.spi.operation;

import com.randomenterprisesolutions.fhir.exception.FHIROperationException;
import com.randomenterprisesolutions.fhir.model.resource.OperationDefinition;
import com.randomenterprisesolutions.fhir.model.resource.Parameters;
import com.randomenterprisesolutions.fhir.model.resource.Resource;
import com.randomenterprisesolutions.fhir.search.util.SearchHelper;

public interface FHIROperation {
    String getName();

    /**
     * Invoke the operation.
     *
     * @throws FHIROperationException
     *     if input or output parameters fail validation or an exception occurs
     */
    Parameters invoke(FHIROperationContext operationContext, Class<? extends Resource> resourceType, String logicalId, String versionId, Parameters parameters,
        FHIRResourceHelpers resourceHelpers, SearchHelper searchHelper) throws FHIROperationException;

    OperationDefinition getDefinition();
}
