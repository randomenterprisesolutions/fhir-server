/*
 * (C) Copyright IBM Corp. 2019
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package com.randomenterprisesolutions.fhir.examples.test;

import com.randomenterprisesolutions.fhir.examples.CompleteMockDataCreator;
import com.randomenterprisesolutions.fhir.model.format.Format;
import com.randomenterprisesolutions.fhir.model.generator.FHIRGenerator;
import com.randomenterprisesolutions.fhir.model.resource.Resource;

public class GenerateSingleResource {
    public static void main(String[] args) throws Exception {
        CompleteMockDataCreator creator = new CompleteMockDataCreator();
        
        Resource resource = creator.createResource("Goal", 1);
        FHIRGenerator generator = FHIRGenerator.generator(Format.JSON, true);
        generator.generate(resource, System.out);
    }
}
