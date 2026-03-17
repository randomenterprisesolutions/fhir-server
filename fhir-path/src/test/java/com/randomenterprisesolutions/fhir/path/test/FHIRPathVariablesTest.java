/*
 * (C) Copyright IBM Corp. 2022
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package com.randomenterprisesolutions.fhir.path.test;

import static org.testng.Assert.assertEquals;

import java.util.Collection;

import com.randomenterprisesolutions.fhir.model.resource.Patient;
import com.randomenterprisesolutions.fhir.model.type.HumanName;
import com.randomenterprisesolutions.fhir.path.FHIRPathNode;
import com.randomenterprisesolutions.fhir.path.FHIRPathStringValue;
import com.randomenterprisesolutions.fhir.path.evaluator.FHIRPathEvaluator;
import com.randomenterprisesolutions.fhir.path.evaluator.FHIRPathEvaluator.EvaluationContext;
import com.randomenterprisesolutions.fhir.path.util.FHIRPathUtil;
import org.testng.annotations.Test;

/**
 * Test FHIRPath expressions that use variables / externalConstants like %resource
 */
public class FHIRPathVariablesTest {
    Patient patient = Patient.builder()
            .id("test")
            .name(HumanName.builder()
                    .given("Lee")
                    .build())
            .build();

    @Test
    public void testRepeatFunction_CodeSystem() throws Exception {
        FHIRPathEvaluator evaluator = FHIRPathEvaluator.evaluator();

        Collection<FHIRPathNode> initialContext = evaluator.evaluate(patient, "Patient.name");

        EvaluationContext evaluationContext = new EvaluationContext(patient);
        Collection<FHIRPathNode> result = evaluator.evaluate(evaluationContext, "%resource.id", initialContext);

        assertEquals("test", FHIRPathUtil.getSingleton(result, FHIRPathStringValue.class).string());
    }
}
