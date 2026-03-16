/*
 * (C) Copyright IBM Corp. 2019
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package com.randomenterprisesolutions.fhir.validation.test;

import java.io.InputStream;
import java.util.Collection;
import java.util.List;

import com.randomenterprisesolutions.fhir.model.format.Format;
import com.randomenterprisesolutions.fhir.model.generator.FHIRGenerator;
import com.randomenterprisesolutions.fhir.model.parser.FHIRParser;
import com.randomenterprisesolutions.fhir.model.resource.ActivityDefinition;
import com.randomenterprisesolutions.fhir.model.resource.OperationOutcome.Issue;
import com.randomenterprisesolutions.fhir.path.FHIRPathNode;
import com.randomenterprisesolutions.fhir.path.evaluator.FHIRPathEvaluator;
import com.randomenterprisesolutions.fhir.validation.FHIRValidator;

public class ActivityDefinitionTest {
    public static void main(String[] args) throws Exception {
        try (InputStream in = ActivityDefinitionTest.class.getClassLoader().getResourceAsStream("JSON/activitydefinition.json")) {
            ActivityDefinition activityDefinition = FHIRParser.parser(Format.JSON).parse(in);
            List<Issue> issues = FHIRValidator.validator().validate(activityDefinition);
            for (Issue issue : issues) {
                System.out.println("severity: " + issue.getSeverity().getValue() + ", details: " + issue.getDetails().getText().getValue() + ", expression: " + issue.getExpression().get(0).getValue());
            }
            Collection<FHIRPathNode> result = FHIRPathEvaluator.evaluator().evaluate(activityDefinition, "contained.id");
            System.out.println("result: " + result);
            FHIRGenerator.generator(Format.XML).generate(activityDefinition, System.out);
        }
    }
}
