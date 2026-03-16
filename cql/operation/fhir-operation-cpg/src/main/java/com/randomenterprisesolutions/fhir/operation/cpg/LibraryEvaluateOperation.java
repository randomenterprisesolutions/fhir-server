/*
 * (C) Copyright IBM Corp. 2021, 2022
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.randomenterprisesolutions.fhir.operation.cpg;

import static com.randomenterprisesolutions.fhir.cql.helpers.ModelHelper.fhirstring;

import java.io.InputStream;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import com.randomenterprisesolutions.fhir.core.ResourceType;
import com.randomenterprisesolutions.fhir.cql.helpers.ParameterMap;
import com.randomenterprisesolutions.fhir.cql.translator.CqlTranslationException;
import com.randomenterprisesolutions.fhir.exception.FHIROperationException;
import com.randomenterprisesolutions.fhir.model.format.Format;
import com.randomenterprisesolutions.fhir.model.parser.FHIRParser;
import com.randomenterprisesolutions.fhir.model.resource.Library;
import com.randomenterprisesolutions.fhir.model.resource.OperationDefinition;
import com.randomenterprisesolutions.fhir.model.resource.OperationOutcome.Issue;
import com.randomenterprisesolutions.fhir.model.resource.Parameters;
import com.randomenterprisesolutions.fhir.model.resource.Parameters.Parameter;
import com.randomenterprisesolutions.fhir.model.resource.Resource;
import com.randomenterprisesolutions.fhir.model.type.CodeableConcept;
import com.randomenterprisesolutions.fhir.model.type.code.IssueSeverity;
import com.randomenterprisesolutions.fhir.model.type.code.IssueType;
import com.randomenterprisesolutions.fhir.persistence.SingleResourceResult;
import com.randomenterprisesolutions.fhir.registry.FHIRRegistry;
import com.randomenterprisesolutions.fhir.search.util.SearchHelper;
import com.randomenterprisesolutions.fhir.server.spi.operation.FHIROperationContext;
import com.randomenterprisesolutions.fhir.server.spi.operation.FHIRResourceHelpers;

public class LibraryEvaluateOperation extends AbstractCqlOperation {

    private static final Logger logger = Logger.getLogger(LibraryEvaluateOperation.class.getName());

    @Override
    protected OperationDefinition buildOperationDefinition() {
        try (InputStream in = getClass().getClassLoader().getResourceAsStream("OperationDefinition-cpg-library-evaluate.json")) {
            return FHIRParser.parser(Format.JSON).parse(in);
        } catch (Exception e) {
            throw new Error(e);
        }
    }

    @Override
    protected Parameters doInvoke(FHIROperationContext operationContext, Class<? extends Resource> resourceType,
            String logicalId, String versionId, Parameters parameters, FHIRResourceHelpers resourceHelper,
            SearchHelper searchHelper) throws FHIROperationException {

        Parameters result = null;

        ParameterMap paramMap = new ParameterMap(parameters);

        checkUnsupportedParameters(paramMap);

        try {
            Library primaryLibrary = null;
            if (operationContext.getType().equals(FHIROperationContext.Type.INSTANCE)) {
                SingleResourceResult<?> readResult = resourceHelper.doRead(ResourceType.LIBRARY.value(), logicalId);
                primaryLibrary = (Library) readResult.getResource();
                if (primaryLibrary == null) {
                    throw new IllegalArgumentException("failed to resolve library with resource id: " + logicalId);
                }
            } else if (operationContext.getType().equals(FHIROperationContext.Type.RESOURCE_TYPE)) {
                Parameter param = paramMap.getSingletonParameter("library");
                String canonicalURL = ((com.randomenterprisesolutions.fhir.model.type.Uri) param.getValue()).getValue();
                primaryLibrary = FHIRRegistry.getInstance().getResource(canonicalURL, Library.class);
                if (primaryLibrary == null) {
                    throw new IllegalArgumentException("failed to resolve library with canonical URL: " + canonicalURL);
                }
            } else {
                throw new UnsupportedOperationException("This operation must be invoked in the context of the Library resource");
            }

            result = doEvaluation(resourceHelper, paramMap, searchHelper, primaryLibrary);

        } catch (FHIROperationException fex) {
            throw fex;
        } catch (IllegalArgumentException | CqlTranslationException iex) {
            logger.log(Level.SEVERE, "Bad Request", iex);
            throw new FHIROperationException(iex.getMessage(), iex)
                    .withIssue(Issue.builder()
                        .severity(IssueSeverity.ERROR)
                        .code(IssueType.INVALID)
                        .details(CodeableConcept.builder().text(fhirstring(iex.getMessage())).build())
                        .build());
        } catch (Exception ex) {
            logger.log(Level.SEVERE, "Evaluation failed", ex);
            throwOperationException(ex);
        }

        return result;
    }

    @Override
    protected Set<String> getCqlExpressionsToEvaluate(ParameterMap paramMap) {
        Set<String> expressions = null;
        if ( paramMap.containsKey(PARAM_IN_EXPRESSION) ) {
            List<Parameter> expressionsParams = paramMap.getParameter(PARAM_IN_EXPRESSION);
            if (expressionsParams != null) {
                expressions = expressionsParams.stream().map(p -> ((com.randomenterprisesolutions.fhir.model.type.String) p.getValue()).getValue()).collect(Collectors.toSet());
            }
        }
        return expressions;
    }

}
