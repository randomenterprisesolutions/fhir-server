/*
 * (C) Copyright IBM Corp. 2020
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package com.randomenterprisesolutions.fhir.model.test;

import static com.randomenterprisesolutions.fhir.model.type.String.string;
import static org.testng.Assert.assertTrue;
import static org.testng.Assert.fail;

import java.io.StringReader;
import java.io.StringWriter;

import org.testng.annotations.Test;

import com.randomenterprisesolutions.fhir.model.format.Format;
import com.randomenterprisesolutions.fhir.model.generator.FHIRGenerator;
import com.randomenterprisesolutions.fhir.model.parser.FHIRParser;
import com.randomenterprisesolutions.fhir.model.parser.exception.FHIRParserException;
import com.randomenterprisesolutions.fhir.model.resource.Observation;
import com.randomenterprisesolutions.fhir.model.type.Age;
import com.randomenterprisesolutions.fhir.model.type.Code;
import com.randomenterprisesolutions.fhir.model.type.CodeableConcept;
import com.randomenterprisesolutions.fhir.model.type.Decimal;
import com.randomenterprisesolutions.fhir.model.type.Extension;
import com.randomenterprisesolutions.fhir.model.type.Uri;
import com.randomenterprisesolutions.fhir.model.type.code.ObservationStatus;

public class ChoiceElementNameTest {
    @Test
    public void testChoiceElement() throws Exception {
        try {
            Observation observation = Observation.builder()
                .extension(Extension.builder()
                    .url("http://example.com/fhir/testExtension")
                    .value(ObservationStatus.FINAL)
                    .build())
                .status(ObservationStatus.FINAL)
                .code(CodeableConcept.builder()
                    .text(string("test"))
                    .build())
                .value(Age.builder()
                    .value(Decimal.of(30))
                    .system(Uri.of("http://unitsofmeasure.org"))
                    .code(Code.of("a"))
                    .build())
                .build();

            StringWriter jsonWriter = new StringWriter();
            FHIRGenerator.generator(Format.JSON, true).generate(observation, jsonWriter);

            String jsonString = jsonWriter.toString();
            System.out.println(jsonString);

            assertTrue(jsonString.contains("valueCode") && jsonString.contains("valueQuantity"));

            FHIRParser.parser(Format.JSON).parse(new StringReader(jsonString));

            StringWriter xmlWriter = new StringWriter();
            FHIRGenerator.generator(Format.XML, true).generate(observation, xmlWriter);

            String xmlString = xmlWriter.toString();
            System.out.println(xmlString);

            assertTrue(xmlString.contains("valueCode") && xmlString.contains("valueQuantity"));

            FHIRParser.parser(Format.XML).parse(new StringReader(xmlString));
        } catch (FHIRParserException e) {
            fail();
        }
    }
}
