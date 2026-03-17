/*
 * (C) Copyright IBM Corp. 2019, 2020
 * (C) Copyright Random Enterprise Solutions 2026
 * SPDX-License-Identifier: Apache-2.0
 */

package com.randomenterprisesolutions.fhir.core.r4.test;

import java.util.Collection;

import com.randomenterprisesolutions.fhir.model.resource.CodeSystem;
import com.randomenterprisesolutions.fhir.model.resource.Resource;
import com.randomenterprisesolutions.fhir.model.resource.SearchParameter;
import com.randomenterprisesolutions.fhir.model.resource.StructureDefinition;
import com.randomenterprisesolutions.fhir.model.type.Canonical;
import com.randomenterprisesolutions.fhir.model.util.ModelSupport;
import com.randomenterprisesolutions.fhir.registry.FHIRRegistry;
import com.randomenterprisesolutions.fhir.registry.util.FHIRRegistryUtil;

import org.testng.Assert;
import org.testng.annotations.Test;

public class FHIRRegistryTest {
    @Test
    public void testRegistry() {
        StructureDefinition structureDefinition = FHIRRegistry.getInstance().getResource("http://hl7.org/fhir/StructureDefinition/Account", StructureDefinition.class);
        Assert.assertNotNull(structureDefinition);
    }

    @Test
    public void testVersionedResource() {
        CodeSystem codeSystem = FHIRRegistry.getInstance().getResource("http://terminology.hl7.org/CodeSystem/v2-0391|2.4", CodeSystem.class);
        Assert.assertNotNull(codeSystem);

        codeSystem = FHIRRegistry.getInstance().getResource("http://terminology.hl7.org/CodeSystem/v2-0391|2.6", CodeSystem.class);
        Assert.assertNotNull(codeSystem);

        codeSystem = FHIRRegistry.getInstance().getResource("http://terminology.hl7.org/CodeSystem/v2-0391", CodeSystem.class);
        Assert.assertNotNull(codeSystem);
        Assert.assertTrue(codeSystem.getUrl().getValue().endsWith("2.6"));
    }

    @Test
    public void testGetResourcesByResourceType() {
        Collection<SearchParameter> searchParameters = FHIRRegistry.getInstance().getResources(SearchParameter.class);
        Assert.assertEquals(searchParameters.size(), 1375);
    }

    @Test
    public void testGetProfilesByType() {
        Collection<Canonical> observationProfiles = FHIRRegistry.getInstance().getProfiles("Observation");
        Assert.assertEquals(observationProfiles.size(), 17);
    }

    @Test
    public void testGetSearchParametersByType() {
        Collection<SearchParameter> tokenSearchParameters = FHIRRegistry.getInstance().getSearchParameters("token");
        Assert.assertEquals(tokenSearchParameters.size(), 536);
    }

    @Test
    public void testLoadAllResources() {
        // FHIRRegistryUtil has a private set of all definitional resources,
        // so an alternative would be to mark that public and iterate through that instead
        for (Class<? extends Resource> resourceType : ModelSupport.getResourceTypes()) {
            if (FHIRRegistryUtil.isDefinitionalResourceType(resourceType)) {
                FHIRRegistry.getInstance().getResources(resourceType);
            }
        }
    }
}
