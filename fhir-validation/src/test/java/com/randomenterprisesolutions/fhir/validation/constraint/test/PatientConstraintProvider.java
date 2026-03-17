/*
 * (C) Copyright IBM Corp. 2021
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package com.randomenterprisesolutions.fhir.validation.constraint.test;

import java.util.List;

import com.randomenterprisesolutions.fhir.model.annotation.Constraint;
import com.randomenterprisesolutions.fhir.model.constraint.spi.AbstractModelConstraintProvider;
import com.randomenterprisesolutions.fhir.model.resource.Patient;

public class PatientConstraintProvider extends AbstractModelConstraintProvider {
    @Override
    public boolean appliesTo(Class<?> modelClass) {
        return Patient.class.equals(modelClass);
    }

    @Override
    protected void addConstraints(List<Constraint> constraints) {
        constraints.add(constraint(
            "patient-name-1",
            Constraint.LEVEL_WARNING,
            Constraint.LOCATION_BASE,
            "If Patient.name exists, then Patient.name.family SHOULD exist",
            PatientConstraintValidator.class));
    }
}
