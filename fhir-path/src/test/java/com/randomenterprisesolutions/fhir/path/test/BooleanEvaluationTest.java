/*
 * (C) Copyright IBM Corp. 2020
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package com.randomenterprisesolutions.fhir.path.test;

import static com.randomenterprisesolutions.fhir.path.evaluator.FHIRPathEvaluator.SINGLETON_TRUE;
import static org.testng.Assert.assertEquals;

import java.util.Collection;

import org.testng.annotations.Test;

import com.randomenterprisesolutions.fhir.path.FHIRPathNode;
import com.randomenterprisesolutions.fhir.path.evaluator.FHIRPathEvaluator;

public class BooleanEvaluationTest {
    @Test
    public void testBooleanEvaluation1() throws Exception {
        FHIRPathEvaluator evaluator = FHIRPathEvaluator.evaluator();
        Collection<FHIRPathNode> result = evaluator.evaluate("true and 'foo'");
        assertEquals(result, SINGLETON_TRUE);
    }
}
