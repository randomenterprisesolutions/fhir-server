/*
 * (C) Copyright IBM Corp. 2021
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package com.randomenterprisesolutions.fhir.ig.us.spl;

import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertTrue;

import java.util.Collection;

import org.testng.annotations.Test;

import com.randomenterprisesolutions.fhir.registry.resource.FHIRRegistryResource;
import com.randomenterprisesolutions.fhir.registry.spi.FHIRRegistryResourceProvider;

public class ResourceProviderTest {
    @Test
    public void testResourceProvider() {
        FHIRRegistryResourceProvider provider = new ResourceProvider();
        Collection<FHIRRegistryResource> registryResources = provider.getRegistryResources();
        assertNotNull(registryResources);
        assertTrue(!registryResources.isEmpty());
        for (FHIRRegistryResource fhirRegistryResource : registryResources) {
            assertNotNull(fhirRegistryResource.getResource());
        }
    }
}
