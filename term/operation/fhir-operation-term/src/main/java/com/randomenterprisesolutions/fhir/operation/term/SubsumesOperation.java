/*
 * (C) Copyright IBM Corp. 2020, 2022
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package com.randomenterprisesolutions.fhir.operation.term;

import static com.randomenterprisesolutions.fhir.model.type.String.string;

import com.randomenterprisesolutions.fhir.exception.FHIROperationException;
import com.randomenterprisesolutions.fhir.model.resource.CodeSystem;
import com.randomenterprisesolutions.fhir.model.resource.OperationDefinition;
import com.randomenterprisesolutions.fhir.model.resource.Parameters;
import com.randomenterprisesolutions.fhir.model.resource.Parameters.Parameter;
import com.randomenterprisesolutions.fhir.model.resource.Resource;
import com.randomenterprisesolutions.fhir.model.type.Coding;
import com.randomenterprisesolutions.fhir.model.type.code.ConceptSubsumptionOutcome;
import com.randomenterprisesolutions.fhir.model.type.code.IssueType;
import com.randomenterprisesolutions.fhir.registry.FHIRRegistry;
import com.randomenterprisesolutions.fhir.search.util.SearchHelper;
import com.randomenterprisesolutions.fhir.server.spi.operation.FHIROperationContext;
import com.randomenterprisesolutions.fhir.server.spi.operation.FHIRResourceHelpers;
import com.randomenterprisesolutions.fhir.term.service.exception.FHIRTermServiceException;

public class SubsumesOperation extends AbstractTermOperation {
    @Override
    protected OperationDefinition buildOperationDefinition() {
        return FHIRRegistry.getInstance().getResource("http://hl7.org/fhir/OperationDefinition/CodeSystem-subsumes", OperationDefinition.class);
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
            CodeSystem codeSystem = FHIROperationContext.Type.INSTANCE.equals(operationContext.getType()) ?
                    getResource(operationContext, logicalId, parameters, resourceHelper, CodeSystem.class) : null;
            Coding codingA = getCoding(parameters, "codingA", "codeA", (codeSystem == null));
            Coding codingB = getCoding(parameters, "codingB", "codeB", (codeSystem == null));
            if (codeSystem != null) {
                codingA = codingA.toBuilder().system(codeSystem.getUrl()).build();
                codingB = codingB.toBuilder().system(codeSystem.getUrl()).build();
            }
            ConceptSubsumptionOutcome outcome = service.subsumes(codingA, codingB);
            if (outcome == null) {
                throw buildExceptionWithIssue("Subsumption cannot be tested", IssueType.NOT_SUPPORTED);
            }
            return Parameters.builder()
                    .parameter(Parameter.builder()
                        .name(string("outcome"))
                        .value(outcome)
                        .build())
                    .build();
        } catch (FHIROperationException e) {
            throw e;
        } catch (FHIRTermServiceException e) {
            throw new FHIROperationException(e.getMessage(), e.getCause()).withIssue(e.getIssues());
        } catch (UnsupportedOperationException e) {
            throw buildExceptionWithIssue(e.getMessage(), IssueType.NOT_SUPPORTED, e);
        } catch (Exception e) {
            throw new FHIROperationException("An error occurred during the CodeSystem subsumes operation", e);
        }
    }
}