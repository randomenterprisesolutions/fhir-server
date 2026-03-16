/*
 * (C) Copyright IBM Corp. 2020, 2021
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package com.randomenterprisesolutions.fhir.validation.test;

import static com.randomenterprisesolutions.fhir.model.type.String.string;
import static com.randomenterprisesolutions.fhir.validation.util.FHIRValidationUtil.countErrors;
import static com.randomenterprisesolutions.fhir.validation.util.FHIRValidationUtil.countInformation;
import static com.randomenterprisesolutions.fhir.validation.util.FHIRValidationUtil.countWarnings;
import static org.testng.Assert.assertEquals;

import java.util.List;

import org.testng.annotations.Test;

import com.randomenterprisesolutions.fhir.model.annotation.Constraint;
import com.randomenterprisesolutions.fhir.model.resource.Coverage;
import com.randomenterprisesolutions.fhir.model.resource.OperationOutcome.Issue;
import com.randomenterprisesolutions.fhir.model.resource.StructureDefinition;
import com.randomenterprisesolutions.fhir.model.type.Canonical;
import com.randomenterprisesolutions.fhir.model.type.DateTime;
import com.randomenterprisesolutions.fhir.model.type.Meta;
import com.randomenterprisesolutions.fhir.model.type.Period;
import com.randomenterprisesolutions.fhir.model.type.Reference;
import com.randomenterprisesolutions.fhir.model.type.code.CoverageStatus;
import com.randomenterprisesolutions.fhir.profile.ConstraintGenerator;
import com.randomenterprisesolutions.fhir.registry.FHIRRegistry;
import com.randomenterprisesolutions.fhir.validation.FHIRValidator;

public class ProfileConstraintTest {
    @Test
    public void testProfileConstraint() throws Exception {
        StructureDefinition structureDefinition = FHIRRegistry.getInstance().getResource("http://myurl.org/my-coverage", StructureDefinition.class);
        ConstraintGenerator generator = new ConstraintGenerator(structureDefinition);
        List<Constraint> constraints = generator.generate();
        assertEquals(constraints.size(), 1);

        structureDefinition = FHIRRegistry.getInstance().getResource("http://myurl.org/my-extension", StructureDefinition.class);
        generator = new ConstraintGenerator(structureDefinition);
        constraints = generator.generate();
        assertEquals(constraints.size(), 2);

        structureDefinition = FHIRRegistry.getInstance().getResource("http://myurl.org/period-with-my-extension", StructureDefinition.class);
        generator = new ConstraintGenerator(structureDefinition);
        constraints = generator.generate();
        assertEquals(constraints.size(), 1);

        Coverage coverage = Coverage.builder()
                .status(CoverageStatus.ACTIVE)
                .beneficiary(Reference.builder()
                    .display(string("none"))
                    .build())
                .payor(Reference.builder()
                    .display(string("none"))
                    .build())
                .period(Period.builder()
                    .start(DateTime.of("2020-01-01"))
                    .end(DateTime.of("2020-01-31"))
                    .build())
                .meta(Meta.builder()
                    .profile(Canonical.of("http://myurl.org/my-coverage"))
                    .build())
                .build();

        List<Issue> issues = FHIRValidator.validator().validate(coverage);
        assertEquals(countErrors(issues), 1);
        assertEquals(countWarnings(issues), 1);
        assertEquals(countInformation(issues), 1);
    }
}