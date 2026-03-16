/*
 * (C) Copyright IBM Corp. 2022
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package com.randomenterprisesolutions.fhir.ig.davinci.pdex.test.v200;

import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertTrue;

import java.util.Collection;

import org.testng.annotations.Test;

import com.randomenterprisesolutions.fhir.ig.davinci.pdex.PDEX200ResourceProvider;
import com.randomenterprisesolutions.fhir.registry.resource.FHIRRegistryResource;
import com.randomenterprisesolutions.fhir.registry.spi.FHIRRegistryResourceProvider;

public class PDEXResourceProviderTest {
    @Test
    public void testPDEXResourceProvider() {
        FHIRRegistryResourceProvider provider = new PDEX200ResourceProvider();
        Collection<FHIRRegistryResource> registryResources = provider.getRegistryResources();
        assertNotNull(registryResources);
        assertTrue(!registryResources.isEmpty());
        for (FHIRRegistryResource fhirRegistryResource : registryResources) {
            assertNotNull(fhirRegistryResource.getResource());
        }
    }
}