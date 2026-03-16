/*
 * (C) Copyright IBM Corp. 2021
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package com.randomenterprisesolutions.fhir.term.service.test;

import static com.randomenterprisesolutions.fhir.term.util.ValueSetSupport.getValueSet;

import com.randomenterprisesolutions.fhir.model.resource.ValueSet;
import com.randomenterprisesolutions.fhir.term.service.FHIRTermService;

public class ExpansionPerformanceTest {
    public static final int ITERATIONS = 1000000;

    public static void main(String[] args) {
        long start = System.currentTimeMillis();

        ValueSet valueSet = getValueSet("http://example.com/fhir/ValueSet/vs4|1.0.0");

        for (int i = 0; i < ITERATIONS; i++) {
            FHIRTermService.getInstance().expand(valueSet);
        }

        long end = System.currentTimeMillis();

        System.out.println("Processing time for " + ITERATIONS + " iterations: " + (end - start) + " milliseconds");
    }
}
