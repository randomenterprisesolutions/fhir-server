/*
 * (C) Copyright IBM Corp. 2019, 2020
 * (C) Copyright Random Enterprise Solutions 2026
 * 
 * SPDX-License-Identifier: Apache-2.0
 */

package com.randomenterprisesolutions.fhir.core.r4.test;

import org.testng.Assert;
import org.testng.annotations.Test;

import com.randomenterprisesolutions.fhir.core.r4.Core401ResourceProvider;
import com.randomenterprisesolutions.fhir.registry.spi.FHIRRegistryResourceProvider;

public class CoreResourceProviderTest {
    @Test
    public void testR4SpecResourceProvider() {
        FHIRRegistryResourceProvider provider = new Core401ResourceProvider();
        Assert.assertEquals(provider.getRegistryResources().size(), 11239);
    }
}
