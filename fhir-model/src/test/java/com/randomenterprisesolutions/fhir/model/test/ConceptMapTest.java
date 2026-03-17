/*
 * (C) Copyright IBM Corp. 2019
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package com.randomenterprisesolutions.fhir.model.test;

import java.io.InputStream;

import com.randomenterprisesolutions.fhir.model.format.Format;
import com.randomenterprisesolutions.fhir.model.generator.FHIRGenerator;
import com.randomenterprisesolutions.fhir.model.parser.FHIRParser;
import com.randomenterprisesolutions.fhir.model.resource.ConceptMap;

public class ConceptMapTest {
    public static void main(String[] args) throws Exception {
        try (InputStream in = ConceptMapTest.class.getClassLoader().getResourceAsStream("XML/conceptmap-example.xml")) {
            ConceptMap conceptMap = FHIRParser.parser(Format.XML).parse(in);
            FHIRGenerator.generator(Format.XML, true).generate(conceptMap, System.out);
        }
    }
}
