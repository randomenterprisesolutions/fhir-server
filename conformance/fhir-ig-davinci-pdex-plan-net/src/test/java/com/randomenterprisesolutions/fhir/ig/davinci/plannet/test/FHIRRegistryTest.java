/*
 * (C) Copyright IBM Corp. 2020
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package com.randomenterprisesolutions.fhir.ig.davinci.plannet.test;

import org.testng.Assert;
import org.testng.annotations.Test;

import com.randomenterprisesolutions.fhir.model.format.Format;
import com.randomenterprisesolutions.fhir.model.generator.FHIRGenerator;
import com.randomenterprisesolutions.fhir.model.generator.exception.FHIRGeneratorException;
import com.randomenterprisesolutions.fhir.model.resource.CapabilityStatement;
import com.randomenterprisesolutions.fhir.registry.FHIRRegistry;

public class FHIRRegistryTest {
    @Test
    public void testRegistry() throws FHIRGeneratorException {
        CapabilityStatement definition = FHIRRegistry.getInstance()
                .getResource("http://hl7.org/fhir/us/davinci-pdex-plan-net/CapabilityStatement/plan-net", CapabilityStatement.class);
        FHIRGenerator.generator(Format.XML).generate(definition, System.out);
        Assert.assertNotNull(definition);
    }
}
