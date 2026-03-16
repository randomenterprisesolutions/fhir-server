/*
 * (C) Copyright IBM Corp. 2020, 2021
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package com.randomenterprisesolutions.fhir.operation.bulkdata.tool.helpers.dynamic;

import static com.randomenterprisesolutions.fhir.model.type.String.string;
import static com.randomenterprisesolutions.fhir.model.type.Xhtml.xhtml;

import java.time.LocalDate;
import java.time.ZoneOffset;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Collection;
import java.util.UUID;

import com.randomenterprisesolutions.fhir.model.resource.Bundle;
import com.randomenterprisesolutions.fhir.model.resource.Bundle.Entry.Request;
import com.randomenterprisesolutions.fhir.model.resource.Group;
import com.randomenterprisesolutions.fhir.model.resource.Group.Characteristic;
import com.randomenterprisesolutions.fhir.model.resource.Patient;
import com.randomenterprisesolutions.fhir.model.type.Age;
import com.randomenterprisesolutions.fhir.model.type.Code;
import com.randomenterprisesolutions.fhir.model.type.CodeableConcept;
import com.randomenterprisesolutions.fhir.model.type.Coding;
import com.randomenterprisesolutions.fhir.model.type.Date;
import com.randomenterprisesolutions.fhir.model.type.Decimal;
import com.randomenterprisesolutions.fhir.model.type.HumanName;
import com.randomenterprisesolutions.fhir.model.type.Id;
import com.randomenterprisesolutions.fhir.model.type.Instant;
import com.randomenterprisesolutions.fhir.model.type.Meta;
import com.randomenterprisesolutions.fhir.model.type.Narrative;
import com.randomenterprisesolutions.fhir.model.type.Reference;
import com.randomenterprisesolutions.fhir.model.type.Uri;
import com.randomenterprisesolutions.fhir.model.type.Xhtml;
import com.randomenterprisesolutions.fhir.model.type.code.AdministrativeGender;
import com.randomenterprisesolutions.fhir.model.type.code.BundleType;
import com.randomenterprisesolutions.fhir.model.type.code.GroupType;
import com.randomenterprisesolutions.fhir.model.type.code.HTTPVerb;
import com.randomenterprisesolutions.fhir.model.type.code.NarrativeStatus;
import com.randomenterprisesolutions.fhir.model.type.code.QuantityComparator;

/**
 * Shows an age range with gender included in a Dynamic Group.
 */
public class AgeRangeWithGenderGroup extends GroupExample {
    @Override
    public String filename() {
        return "age-range-with-gender";
    }

    @Override
    public Group group() {
        java.lang.String div = "<div xmlns=\"http://www.w3.org/1999/xhtml\"><p><b>Generated Narrative</b></p></div>";

        java.lang.String id = UUID.randomUUID().toString();

        Meta meta = Meta.builder()
                .versionId(Id.of("1"))
                .lastUpdated(Instant.now(ZoneOffset.UTC))
                .build();

        Narrative text = Narrative.builder()
                .status(NarrativeStatus.GENERATED)
                .div(xhtml(div))
                .build();

        com.randomenterprisesolutions.fhir.model.type.Boolean active = com.randomenterprisesolutions.fhir.model.type.Boolean.of(true);

        com.randomenterprisesolutions.fhir.model.type.Boolean actual = com.randomenterprisesolutions.fhir.model.type.Boolean.of(false);

        Collection<Characteristic> characteristics = new ArrayList<>();
        characteristics.add(generateLowerBoundBirthdateCharacteristic());
        characteristics.add(generateUpperBoundBirthdateCharacteristic());
        characteristics.add(generateGenderCharacteristic());

        Group group = Group.builder()
                .id(id)
                .meta(meta)
                .text(text)
                .active(active)
                .type(GroupType.PERSON)
                .actual(actual)
                .name(string(filename()))
                .characteristic(characteristics)
                .build();
        return group;
    }

    private Characteristic generateGenderCharacteristic() {
        CodeableConcept code = CodeableConcept.builder()
                .coding(Coding.builder().code(Code.of("AdministrativeGender"))
                    .system(Uri.of("http://hl7.org/fhir/administrative-gender"))
                    .build())
            .text(string("Gender"))
            .build();

        CodeableConcept value = CodeableConcept.builder().coding(Coding.builder().code(Code.of("Female"))
            .system(Uri.of("http://hl7.org/fhir/administrative-gender")).build())
            .text(string("female"))
            .build();

        Characteristic characteristic = Characteristic.builder()
            .code(code)
            .value(value)
            .exclude(com.randomenterprisesolutions.fhir.model.type.Boolean.FALSE)
            .build();
        return characteristic;
    }

    private Characteristic generateLowerBoundBirthdateCharacteristic() {
        CodeableConcept code = CodeableConcept.builder().coding(Coding.builder().code(Code.of("29553-5"))
            .system(Uri.of("http://loinc.org")).build())
            .text(string("Age calculated"))
            .build();

        Age value = Age.builder()
                .system(Uri.of("http://unitsofmeasure.org"))
                .code(Code.of("a"))
                .unit(string("years"))
                .value(Decimal.of("13"))
                .comparator(QuantityComparator.GREATER_OR_EQUALS)
                .build();

        Characteristic characteristic = Characteristic.builder()
            .code(code)
            .value(value)
            .exclude(com.randomenterprisesolutions.fhir.model.type.Boolean.FALSE)
            .build();
        return characteristic;
    }

    private Characteristic generateUpperBoundBirthdateCharacteristic() {
        CodeableConcept code = CodeableConcept.builder().coding(Coding.builder().code(Code.of("29553-5"))
            .system(Uri.of("http://loinc.org")).build())
            .text(string("Age calculated"))
            .build();

        Age value = Age.builder()
                .system(Uri.of("http://unitsofmeasure.org"))
                .code(Code.of("a"))
                .unit(string("years"))
                .value(Decimal.of("56"))
                .comparator(QuantityComparator.LESS_OR_EQUALS)
                .build();

        Characteristic characteristic = Characteristic.builder()
            .code(code)
            .value(value)
            .exclude(com.randomenterprisesolutions.fhir.model.type.Boolean.FALSE)
            .build();
        return characteristic;
    }

    @Override
    public Bundle sampleData() {
        Bundle.Entry entry = Bundle.Entry.builder()
                .resource(buildTestPatient())
                .request(Request.builder().method(HTTPVerb.POST)
                    .url(Uri.of("Patient")).build())
                .build();
        return Bundle.builder().type(BundleType.TRANSACTION).entry(entry).build();
    }

    private Patient buildTestPatient() {
        String id = UUID.randomUUID().toString();

        Meta meta =
                Meta.builder()
                    .versionId(Id.of("1"))
                    .lastUpdated(Instant.now(ZoneOffset.UTC))
                    .build();

        com.randomenterprisesolutions.fhir.model.type.String given =
                com.randomenterprisesolutions.fhir.model.type.String.builder()
                .value("John")
                .build();

        HumanName name =
                HumanName.builder()
                    .id("someId")
                    .given(given)
                    .family(string("Doe")).build();

        java.lang.String uUID = UUID.randomUUID().toString();

        Reference providerRef =
                Reference.builder().reference(string("urn:uuid:" + uUID)).build();

        return Patient.builder().id(id)
                .active(com.randomenterprisesolutions.fhir.model.type.Boolean.TRUE)
                .multipleBirth(com.randomenterprisesolutions.fhir.model.type.Integer.of(2))
                .meta(meta).name(name).birthDate(Date.of(LocalDate.now().minus(30,ChronoUnit.YEARS)))
                .gender(AdministrativeGender.FEMALE)
                .generalPractitioner(providerRef).text(
                    Narrative.builder()
                        .div(Xhtml.of("<div xmlns=\"http://www.w3.org/1999/xhtml\">loaded from the datastore</div>"))
                        .status(NarrativeStatus.GENERATED).build())
                .build();
    }
}