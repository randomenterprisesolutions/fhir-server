/*
 * (C) Copyright IBM Corp. 2021, 2022
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package com.randomenterprisesolutions.fhir.operation.davinci.hrex;

import com.randomenterprisesolutions.fhir.exception.FHIROperationException;
import com.randomenterprisesolutions.fhir.model.resource.OperationDefinition;
import com.randomenterprisesolutions.fhir.model.resource.Parameters;
import com.randomenterprisesolutions.fhir.model.resource.Resource;
import com.randomenterprisesolutions.fhir.model.type.code.IssueType;
import com.randomenterprisesolutions.fhir.operation.davinci.hrex.configuration.ConfigurationAdapter;
import com.randomenterprisesolutions.fhir.operation.davinci.hrex.configuration.ConfigurationFactory;
import com.randomenterprisesolutions.fhir.operation.davinci.hrex.provider.MemberMatchFactory;
import com.randomenterprisesolutions.fhir.operation.davinci.hrex.provider.strategy.MemberMatchStrategy;
import com.randomenterprisesolutions.fhir.registry.FHIRRegistry;
import com.randomenterprisesolutions.fhir.search.util.SearchHelper;
import com.randomenterprisesolutions.fhir.server.spi.operation.AbstractOperation;
import com.randomenterprisesolutions.fhir.server.spi.operation.FHIROperationContext;
import com.randomenterprisesolutions.fhir.server.spi.operation.FHIROperationUtil;
import com.randomenterprisesolutions.fhir.server.spi.operation.FHIRResourceHelpers;

/**
 * Implements the $MemberMatch Operation
 */
public class MemberMatchOperation extends AbstractOperation {

    public MemberMatchOperation() {
        super();
    }

    @Override
    protected OperationDefinition buildOperationDefinition() {
        return FHIRRegistry.getInstance()
                .getResource("http://hl7.org/fhir/us/davinci-hrex/OperationDefinition/member-match",
                    OperationDefinition.class);
    }

    @Override
    protected Parameters doInvoke(FHIROperationContext operationContext, Class<? extends Resource> resourceType,
            String logicalId, String versionId, Parameters parameters, FHIRResourceHelpers resourceHelper, SearchHelper searchHelper)
            throws FHIROperationException {
        ConfigurationAdapter config = ConfigurationFactory.factory().getConfigurationAdapter();

        if (!config.enabled()) {
            throw FHIROperationUtil.buildExceptionWithIssue("$member-match is not supported", IssueType.NOT_SUPPORTED);
        }

        MemberMatchStrategy strategy = MemberMatchFactory.factory().getStrategy(config);
        return strategy.execute(operationContext, parameters, resourceHelper);
    }
}