/*
 * (C) Copyright IBM Corp. 2020, 2021
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package com.randomenterprisesolutions.fhir.validation.test;

import static org.testng.Assert.assertEquals;

import java.util.List;

import org.testng.annotations.Test;

import com.randomenterprisesolutions.fhir.model.annotation.Constraint;
import com.randomenterprisesolutions.fhir.model.resource.StructureDefinition;
import com.randomenterprisesolutions.fhir.profile.ConstraintGenerator;
import com.randomenterprisesolutions.fhir.registry.FHIRRegistry;

public class ReferenceExtensionTest {
    @Test
    public void testReferenceExtension1() throws Exception {
        StructureDefinition extensionDefinition = FHIRRegistry.getInstance().getResource("http://example.com/fhir/StructureDefinition/test-reference-extension", StructureDefinition.class);
        ConstraintGenerator generator = new ConstraintGenerator(extensionDefinition);
        List<Constraint> constraints = generator.generate();
        constraints.forEach(System.out::println);
        assertEquals(constraints.get(1).expression(), "value.asTypeEqual(Reference).exists()");
    }

    @Test
    public void testReferenceExtension2() throws Exception {
        StructureDefinition extensionDefinition = FHIRRegistry.getInstance().getResource("http://example.com/fhir/StructureDefinition/test-reference-extension-with-target-profile", StructureDefinition.class);
        ConstraintGenerator generator = new ConstraintGenerator(extensionDefinition);
        List<Constraint> constraints = generator.generate();
        constraints.forEach(System.out::println);
        assertEquals(constraints.get(1).expression(), "value.asTypeEqual(Reference).exists() and value.asTypeEqual(Reference).all(resolve().conformsTo('http://example.com/fhir/StructureDefinition/test-profile'))");
    }

    @Test
    public void testReferenceExtension3() throws Exception {
        StructureDefinition extensionDefinition = FHIRRegistry.getInstance().getResource("http://example.com/fhir/StructureDefinition/test-reference-extension-with-multiple-target-profiles", StructureDefinition.class);
        ConstraintGenerator generator = new ConstraintGenerator(extensionDefinition);
        List<Constraint> constraints = generator.generate();
        constraints.forEach(System.out::println);
        assertEquals(constraints.get(1).expression(), "value.asTypeEqual(Reference).exists() and value.asTypeEqual(Reference).all((resolve().conformsTo('http://example.com/fhir/StructureDefinition/test-profile') or resolve().is(Patient)))");
    }
}
