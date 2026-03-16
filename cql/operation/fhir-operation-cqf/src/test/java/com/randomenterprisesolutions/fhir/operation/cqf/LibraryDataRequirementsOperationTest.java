/*
 * (C) Copyright IBM Corp. 2021
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.randomenterprisesolutions.fhir.operation.cqf;

import static com.randomenterprisesolutions.fhir.cql.helpers.ModelHelper.fhirstring;

import org.testng.annotations.Test;

import com.randomenterprisesolutions.fhir.model.resource.Library;
import com.randomenterprisesolutions.fhir.model.resource.Parameters;
import com.randomenterprisesolutions.fhir.server.spi.operation.FHIROperationContext;

public class LibraryDataRequirementsOperationTest extends BaseDataRequirementsOperationTest {

    @Test
    public void testInstanceLibrary() throws Exception {
        runTest( FHIROperationContext.createInstanceOperationContext("data-requirements"), Library.class, primaryLibrary -> primaryLibrary.getId(), primaryLibrary -> null );
    }

    @Test
    public void testSystemLibrary() throws Exception {
        runTest( FHIROperationContext.createSystemOperationContext("data-requirements"), null, primaryLibrary -> null, primaryLibrary -> {
            return Parameters.builder().parameter( Parameters.Parameter.builder().name(fhirstring("target")).value(fhirstring(primaryLibrary.getId())).build()).build();
        });
    }

    @Override
    public AbstractDataRequirementsOperation getOperation() {
        return new LibraryDataRequirementsOperation();
    }
}
