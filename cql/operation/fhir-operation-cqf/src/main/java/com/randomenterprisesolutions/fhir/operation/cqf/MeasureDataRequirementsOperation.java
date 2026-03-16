/*
 * (C) Copyright IBM Corp. 2021, 2022
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.randomenterprisesolutions.fhir.operation.cqf;

import java.util.Arrays;
import java.util.List;

import com.randomenterprisesolutions.fhir.core.ResourceType;
import com.randomenterprisesolutions.fhir.cql.helpers.LibraryHelper;
import com.randomenterprisesolutions.fhir.ecqm.r4.MeasureHelper;
import com.randomenterprisesolutions.fhir.exception.FHIROperationException;
import com.randomenterprisesolutions.fhir.model.resource.Library;
import com.randomenterprisesolutions.fhir.model.resource.Measure;
import com.randomenterprisesolutions.fhir.model.resource.OperationDefinition;
import com.randomenterprisesolutions.fhir.model.resource.Parameters;
import com.randomenterprisesolutions.fhir.model.resource.Resource;
import com.randomenterprisesolutions.fhir.model.type.RelatedArtifact;
import com.randomenterprisesolutions.fhir.model.type.code.RelatedArtifactType;
import com.randomenterprisesolutions.fhir.persistence.SingleResourceResult;
import com.randomenterprisesolutions.fhir.registry.FHIRRegistry;
import com.randomenterprisesolutions.fhir.search.util.SearchHelper;
import com.randomenterprisesolutions.fhir.server.spi.operation.FHIROperationContext;
import com.randomenterprisesolutions.fhir.server.spi.operation.FHIRResourceHelpers;

public class MeasureDataRequirementsOperation extends AbstractDataRequirementsOperation {

    @Override
    protected OperationDefinition buildOperationDefinition() {
        return FHIRRegistry.getInstance().getResource("http://hl7.org/fhir/OperationDefinition/Measure-data-requirements", OperationDefinition.class);
    }

    @Override
    public Parameters doInvoke(FHIROperationContext operationContext, Class<? extends Resource> resourceType, String logicalId, String versionId,
            Parameters parameters, FHIRResourceHelpers resourceHelper, SearchHelper searchHelper) throws FHIROperationException {

        Measure measure = null;
        try {
            SingleResourceResult<?> readResult = resourceHelper.doRead(ResourceType.MEASURE.value(), logicalId);
            measure = (Measure) readResult.getResource();
            if (measure == null) {
                throw new FHIROperationException("Failed to resolve measure with resource id: " + logicalId);
            }
        } catch (FHIROperationException fex) {
            throw fex;
        } catch (Exception ex) {
            throw new FHIROperationException("Failed to read resource", ex);
        }

        int numLibraries = (measure.getLibrary() != null) ? measure.getLibrary().size() : 0;
        if (numLibraries != 1) {
            throw new IllegalArgumentException(String.format("Unexpected number of libraries '%d' referenced by measure '%s'", numLibraries, measure.getId()));
        }

        String primaryLibraryId = MeasureHelper.getPrimaryLibraryId(measure);
        Library primaryLibrary = OperationHelper.loadLibraryByReference(resourceHelper, primaryLibraryId);
        List<Library> fhirLibraries = LibraryHelper.loadLibraries(primaryLibrary);

        RelatedArtifact related = RelatedArtifact.builder().type(RelatedArtifactType.DEPENDS_ON).resource(measure.getLibrary().get(0)).build();
        return doDataRequirements(fhirLibraries, () -> Arrays.asList(related) );
    }
}
