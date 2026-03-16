package com.randomenterprisesolutions.fhir.operation.convert;
import static org.testng.Assert.assertEquals;

import java.io.StringReader;
import java.io.StringWriter;

/*
 * (C) Copyright Merative 2020, 2022
 *
 * SPDX-License-Identifier: Apache-2.0
 */
import org.testng.annotations.Test;

import com.randomenterprisesolutions.fhir.model.format.Format;
import com.randomenterprisesolutions.fhir.model.generator.FHIRGenerator;
import com.randomenterprisesolutions.fhir.model.parser.FHIRParser;
import com.randomenterprisesolutions.fhir.model.resource.Patient;
import com.randomenterprisesolutions.fhir.model.resource.Resource;
import com.randomenterprisesolutions.fhir.model.type.Extension;

public class ConversionTest {
    // https://github.com/randomenterprisesolutions/FHIR/issues/3577
    @Test(enabled = false)
    void testStringCompare() throws Exception {
        Resource original = Patient.builder()
                .extension(Extension.builder()
                        .url("test")
                        .value("test\none")
                        .build())
                .build();

        StringWriter sw = new StringWriter();
        FHIRGenerator.generator(Format.XML).generate(original, sw);

        StringReader sr = new StringReader(sw.toString());
        Resource roundtripped = FHIRParser.parser(Format.XML).parse(sr);

        assertEquals(roundtripped, original);
    }
}
