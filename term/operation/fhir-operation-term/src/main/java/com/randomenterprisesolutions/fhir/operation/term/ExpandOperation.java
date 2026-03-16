/*
 * (C) Copyright IBM Corp. 2020, 2022
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package com.randomenterprisesolutions.fhir.operation.term;

import static com.randomenterprisesolutions.fhir.server.spi.operation.FHIROperationUtil.getOutputParameters;
import static com.randomenterprisesolutions.fhir.term.util.ValueSetSupport.isExpanded;

import com.randomenterprisesolutions.fhir.exception.FHIROperationException;
import com.randomenterprisesolutions.fhir.model.resource.OperationDefinition;
import com.randomenterprisesolutions.fhir.model.resource.Parameters;
import com.randomenterprisesolutions.fhir.model.resource.Resource;
import com.randomenterprisesolutions.fhir.model.resource.ValueSet;
import com.randomenterprisesolutions.fhir.model.type.code.IssueType;
import com.randomenterprisesolutions.fhir.registry.FHIRRegistry;
import com.randomenterprisesolutions.fhir.search.util.SearchHelper;
import com.randomenterprisesolutions.fhir.server.spi.operation.FHIROperationContext;
import com.randomenterprisesolutions.fhir.server.spi.operation.FHIRResourceHelpers;
import com.randomenterprisesolutions.fhir.term.service.ExpansionParameters;
import com.randomenterprisesolutions.fhir.term.service.exception.FHIRTermServiceException;

public class ExpandOperation extends AbstractTermOperation {
    @Override
    protected OperationDefinition buildOperationDefinition() {
        return FHIRRegistry.getInstance().getResource("http://hl7.org/fhir/OperationDefinition/ValueSet-expand", OperationDefinition.class);
    }

    @Override
    protected Parameters doInvoke(
            FHIROperationContext operationContext,
            Class<? extends Resource> resourceType,
            String logicalId,
            String versionId,
            Parameters parameters,
            FHIRResourceHelpers resourceHelper,
            SearchHelper searchHelper) throws FHIROperationException {
        try {
            ValueSet valueSet = getResource(operationContext, logicalId, parameters, resourceHelper, ValueSet.class);
            if (!isExpanded(valueSet) && !service.isExpandable(valueSet)) {
                String url = (valueSet.getUrl() != null) ? valueSet.getUrl().getValue() : null;
                throw buildExceptionWithIssue("ValueSet with url '" + url + "' is not expandable", IssueType.NOT_SUPPORTED);
            }
            ValueSet expanded = service.expand(valueSet, ExpansionParameters.from(parameters));
            return getOutputParameters(expanded);
        } catch (FHIROperationException e) {
            throw e;
        } catch (FHIRTermServiceException e) {
            throw new FHIROperationException(e.getMessage(), e.getCause()).withIssue(e.getIssues());
        } catch (UnsupportedOperationException e) {
            throw buildExceptionWithIssue(e.getMessage(), IssueType.NOT_SUPPORTED, e);
        } catch (Exception e) {
            throw new FHIROperationException("An error occurred during the ValueSet expand operation", e);
        }
    }
}
