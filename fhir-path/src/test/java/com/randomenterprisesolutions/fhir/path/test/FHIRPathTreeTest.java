/*
 * (C) Copyright IBM Corp. 2019
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package com.randomenterprisesolutions.fhir.path.test;

import java.io.FilterOutputStream;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.UUID;

import com.randomenterprisesolutions.fhir.model.format.Format;
import com.randomenterprisesolutions.fhir.model.generator.FHIRGenerator;
import com.randomenterprisesolutions.fhir.model.resource.Patient;
import com.randomenterprisesolutions.fhir.model.type.Boolean;
import com.randomenterprisesolutions.fhir.model.type.Date;
import com.randomenterprisesolutions.fhir.model.type.Extension;
import com.randomenterprisesolutions.fhir.model.type.HumanName;
import com.randomenterprisesolutions.fhir.model.type.Id;
import com.randomenterprisesolutions.fhir.model.type.Instant;
import com.randomenterprisesolutions.fhir.model.type.Integer;
import com.randomenterprisesolutions.fhir.model.type.Meta;
import com.randomenterprisesolutions.fhir.model.type.String;
import com.randomenterprisesolutions.fhir.path.FHIRPathNode;
import com.randomenterprisesolutions.fhir.path.FHIRPathTree;

public class FHIRPathTreeTest {
    public static void main(java.lang.String[] args) throws Exception {
        java.lang.String id = UUID.randomUUID().toString();

        Meta meta = Meta.builder().versionId(Id.of("1"))
                .lastUpdated(Instant.now(ZoneOffset.UTC))
                .build();

        String given = String.builder().value("John")
                .extension(Extension.builder()
                    .url("http://example.com/someExtension")
                    .value(String.of("value and extension"))
                    .build())
                .build();

        String otherGiven = String.builder()
                .extension(Extension.builder()
                    .url("http://example.com/someExtension")
                    .value(String.of("extension only"))
                    .build())
                .build();

        HumanName name = HumanName.builder()
                .id("someId")
                .given(given)
                .given(otherGiven)
                .given(String.of("value no extension"))
                .family(String.of("Doe"))
                .build();

        Patient patient = Patient.builder()
                .id(id)
                .active(Boolean.TRUE)
                .multipleBirth(Integer.of(2))
                .meta(meta)
                .name(name)
                .birthDate(Date.of(LocalDate.now()))
                .build();

        FilterOutputStream out = new FilterOutputStream(System.out) {
            @Override
            public void close() {
                // do nothing
            }
        };

        FHIRGenerator.generator(Format.JSON, true).generate(patient, out);

        System.out.println("");

        FHIRPathTree tree = FHIRPathTree.tree(patient);
        tree.getRoot().stream().forEach(FHIRPathTreeTest::print);

        FHIRPathNode node = tree.getNode("Patient.id.extension[0]");
        print(node);
    }

    public static void print(FHIRPathNode node) {
        System.out.println("name: " + node.name() + ", type: " + node.type() + ", class: " + node.getClass().getSimpleName());
    }
}
