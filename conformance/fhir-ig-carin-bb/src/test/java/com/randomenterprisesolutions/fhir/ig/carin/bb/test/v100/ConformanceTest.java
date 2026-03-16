/*
 * (C) Copyright IBM Corp. 2021
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package com.randomenterprisesolutions.fhir.ig.carin.bb.test.v100;

import java.util.ArrayList;
import java.util.List;

import org.testng.Assert;
import org.testng.annotations.Test;

import com.randomenterprisesolutions.fhir.ig.carin.bb.test.tool.C4BBExamplesUtil;
import com.randomenterprisesolutions.fhir.model.resource.ExplanationOfBenefit;
import com.randomenterprisesolutions.fhir.model.resource.ExplanationOfBenefit.Item;
import com.randomenterprisesolutions.fhir.model.resource.ExplanationOfBenefit.Item.Adjudication;
import com.randomenterprisesolutions.fhir.model.resource.OperationOutcome.Issue;
import com.randomenterprisesolutions.fhir.model.type.Code;
import com.randomenterprisesolutions.fhir.model.type.CodeableConcept;
import com.randomenterprisesolutions.fhir.model.type.Coding;
import com.randomenterprisesolutions.fhir.model.type.Decimal;
import com.randomenterprisesolutions.fhir.model.type.Uri;
import com.randomenterprisesolutions.fhir.validation.FHIRValidator;
import com.randomenterprisesolutions.fhir.validation.util.FHIRValidationUtil;

/**
 * Specific Conformance Tests
 */
public class ConformanceTest {
    @Test
    public void testExplanationOfBenefit() throws Exception {
        ExplanationOfBenefit explanationOfBenefit = C4BBExamplesUtil.readLocalJSONResource("100", "ExplanationOfBenefit-InpatientEOBExample1.json");
        List<Issue> issues = FHIRValidator.validator().validate(explanationOfBenefit);
        Assert.assertFalse(FHIRValidationUtil.hasErrors(issues));

        // add an 'allowedunits' adjudication slice to the first item
        List<Item> items = new ArrayList<>(explanationOfBenefit.getItem());
        Item item = items.get(0);
        item = item.toBuilder()
                .adjudication(Adjudication.builder()
                    .category(CodeableConcept.builder()
                        .coding(Coding.builder()
                            .system(Uri.of("http://hl7.org/fhir/us/carin-bb/CodeSystem/C4BBAdjudicationDiscriminator"))
                            .code(Code.of("allowedunits"))
                            .build())
                        .build())
                    .value(Decimal.of(1))
                    .build())
                .build();
        items.set(0, item);
        explanationOfBenefit = explanationOfBenefit
            .toBuilder()
            .item(items)
            .build();
        issues = FHIRValidator.validator().validate(explanationOfBenefit);
        Assert.assertFalse(FHIRValidationUtil.hasErrors(issues));
    }
}