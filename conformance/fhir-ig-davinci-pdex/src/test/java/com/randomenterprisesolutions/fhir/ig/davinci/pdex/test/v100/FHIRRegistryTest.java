/*
 * (C) Copyright IBM Corp. 2020, 2022
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package com.randomenterprisesolutions.fhir.ig.davinci.pdex.test.v100;

import org.testng.Assert;
import org.testng.annotations.Test;

import com.randomenterprisesolutions.fhir.model.resource.StructureDefinition;
import com.randomenterprisesolutions.fhir.registry.FHIRRegistry;

public class FHIRRegistryTest {
    @Test
    public void testRegistry() {
        StructureDefinition definition = FHIRRegistry.getInstance()
                .getResource("http://hl7.org/fhir/us/davinci-pdex/StructureDefinition/pdex-medicationdispense", StructureDefinition.class);
        Assert.assertNotNull(definition);
    }
}