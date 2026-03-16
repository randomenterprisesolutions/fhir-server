/*
 * (C) Copyright IBM Corp. 2021, 2022
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package com.randomenterprisesolutions.fhir.term.graph.test;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.apache.commons.configuration2.MapConfiguration;

import com.randomenterprisesolutions.fhir.model.resource.CodeSystem;
import com.randomenterprisesolutions.fhir.model.resource.CodeSystem.Concept;
import com.randomenterprisesolutions.fhir.model.type.Code;
import com.randomenterprisesolutions.fhir.model.type.Uri;
import com.randomenterprisesolutions.fhir.model.type.code.CodeSystemContentMode;
import com.randomenterprisesolutions.fhir.model.type.code.PublicationStatus;
import com.randomenterprisesolutions.fhir.term.graph.provider.GraphTermServiceProvider;

/**
 * Tests for Snomed and Graph
 */
public class SnomedGraphTermServiceProviderTest {
    public static void main(String[] args) throws Exception {
        CodeSystem codeSystem = CodeSystem.builder()
                .url(Uri.of("http://snomed.info/sct"))
                .status(PublicationStatus.ACTIVE)
                .content(CodeSystemContentMode.NOT_PRESENT)
                .build();

        System.out.println(codeSystem);

        Map<String, Object> map = new HashMap<>();
        map.put("storage.backend", "cql");
        map.put("storage.hostname", "127.0.0.1");
        map.put("index.search.backend", "elasticsearch");
        map.put("index.search.hostname", "127.0.0.1:9200");
        map.put("query.batch", true);
        map.put("query.batch-property-prefetch", true);
        map.put("query.fast-property", true);
        map.put("storage.read-only", true);

        GraphTermServiceProvider provider = new GraphTermServiceProvider(new MapConfiguration(map));

        Set<Concept> concepts = provider.closure(codeSystem, Code.of("195967001"));
        concepts.stream().forEach(System.out::println);

        System.out.println(provider.subsumes(codeSystem, Code.of("195967001"), Code.of("31387002")));
        System.out.println(provider.subsumes(codeSystem, Code.of("195967001"), Code.of("195967001")));

        System.out.println(provider.getConcept(codeSystem, Code.of("195967001")));

        provider.getGraph().close();
    }
}
