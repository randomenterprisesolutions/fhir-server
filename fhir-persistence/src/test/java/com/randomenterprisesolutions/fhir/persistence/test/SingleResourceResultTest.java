/*
 * (C) Copyright IBM Corp. 2022
 *
 * SPDX-License-Identifier: Apache-2.0
 */
 
package com.randomenterprisesolutions.fhir.persistence.test;

import static com.randomenterprisesolutions.fhir.model.type.String.string;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;

import org.testng.annotations.Test;

import com.randomenterprisesolutions.fhir.model.resource.Patient;
import com.randomenterprisesolutions.fhir.model.type.Id;
import com.randomenterprisesolutions.fhir.model.type.Instant;
import com.randomenterprisesolutions.fhir.model.type.Meta;
import com.randomenterprisesolutions.fhir.model.type.Narrative;
import com.randomenterprisesolutions.fhir.model.type.Reference;
import com.randomenterprisesolutions.fhir.model.type.Xhtml;
import com.randomenterprisesolutions.fhir.model.type.code.NarrativeStatus;
import com.randomenterprisesolutions.fhir.persistence.InteractionStatus;
import com.randomenterprisesolutions.fhir.persistence.SingleResourceResult;

/**
 * Unit test for SingleResourceResult
 */
public class SingleResourceResultTest {

    @Test
    public void testReplace() {
        Patient patient = Patient.builder()
                .meta(Meta.builder()
                    .lastUpdated(Instant.now())
                    .versionId(Id.of("1"))
                    .build())
                .generalPractitioner(Reference.builder()
                    .reference(string("Practitioner/1"))
                    .build())
                .text(Narrative.builder()
                    .div(Xhtml.of("<div xmlns=\"http://www.w3.org/1999/xhtml\">Some narrative</div>"))
                    .status(NarrativeStatus.GENERATED)
                    .build())
                .build();
        SingleResourceResult<Patient> srr = new SingleResourceResult.Builder<Patient>()
                .interactionStatus(InteractionStatus.READ)
                .resource(patient)
                .success(true)
                .deleted(false)
                .build();
        assertTrue(srr.getResource() == patient);
        assertTrue(srr.isSuccess());
        assertFalse(srr.isDeleted());
        assertEquals(srr.getStatus(), InteractionStatus.READ);

        // If we replace the resource with the same value, we should get back the same result
        assertTrue(srr == srr.replace(patient));

        // Now check we can actually replace the resource with a new value
        Patient patient2 = patient.toBuilder()
                .id("patient2")
                .build();

        SingleResourceResult<Patient> srr2 = srr.replace(patient2);
        assertFalse(srr2 == srr);
        assertTrue(srr2.getResource() == patient2);
    }
}
