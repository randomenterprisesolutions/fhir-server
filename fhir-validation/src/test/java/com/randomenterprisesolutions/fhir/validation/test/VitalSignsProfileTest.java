/*
 * (C) Copyright IBM Corp. 2019
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package com.randomenterprisesolutions.fhir.validation.test;

import com.randomenterprisesolutions.fhir.model.resource.StructureDefinition;
import com.randomenterprisesolutions.fhir.profile.ConstraintGenerator;
import com.randomenterprisesolutions.fhir.profile.ProfileSupport;

public class VitalSignsProfileTest {
    private static final String VITAL_SIGNS_PROFILE_URL = "http://hl7.org/fhir/StructureDefinition/vitalsigns";
    public static void main(String[] args) {
        StructureDefinition vitalSignsProfile = ProfileSupport.getProfile(VITAL_SIGNS_PROFILE_URL);
        ConstraintGenerator generator = new ConstraintGenerator(vitalSignsProfile);
        generator.generate().stream().forEach(System.out::println);
    }
}