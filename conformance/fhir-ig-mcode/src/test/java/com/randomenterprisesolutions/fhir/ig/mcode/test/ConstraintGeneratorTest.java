/*
 * (C) Copyright IBM Corp. 2020
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package com.randomenterprisesolutions.fhir.ig.mcode.test;

import static com.randomenterprisesolutions.fhir.path.util.FHIRPathUtil.compile;

import org.testng.annotations.Test;

import com.randomenterprisesolutions.fhir.ig.mcode.MCODEResourceProvider;
import com.randomenterprisesolutions.fhir.model.annotation.Constraint;
import com.randomenterprisesolutions.fhir.model.resource.StructureDefinition;
import com.randomenterprisesolutions.fhir.model.type.Extension;
import com.randomenterprisesolutions.fhir.model.util.ModelSupport;
import com.randomenterprisesolutions.fhir.profile.ProfileSupport;
import com.randomenterprisesolutions.fhir.registry.resource.FHIRRegistryResource;
import com.randomenterprisesolutions.fhir.registry.spi.FHIRRegistryResourceProvider;

public class ConstraintGeneratorTest {
    @Test
    public static void testConstraintGenerator() throws Exception {
        FHIRRegistryResourceProvider provider = new MCODEResourceProvider();
        for (FHIRRegistryResource registryResource : provider.getRegistryResources()) {
            if (StructureDefinition.class.equals(registryResource.getResourceType())) {
                String url = registryResource.getUrl();
                System.out.println(url);
                Class<?> type = ModelSupport.isResourceType(registryResource.getType()) ? ModelSupport.getResourceType(registryResource.getType()) : Extension.class;
                for (Constraint constraint : ProfileSupport.getConstraints(url, type)) {
                    System.out.println("    " + constraint);
                    if (!Constraint.LOCATION_BASE.equals(constraint.location())) {
                        compile(constraint.location());
                    }
                    compile(constraint.expression());
                }
            }
        }
    }
}
