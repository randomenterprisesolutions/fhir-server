/*
 * (C) Copyright IBM Corp. 2021
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package com.randomenterprisesolutions.fhir.term.graph.test;

import org.apache.commons.configuration2.PropertiesConfiguration;
import org.apache.commons.configuration2.builder.FileBasedConfigurationBuilder;
import org.apache.commons.configuration2.builder.fluent.Parameters;
import org.testng.annotations.AfterClass;

import com.randomenterprisesolutions.fhir.term.graph.FHIRTermGraph;
import com.randomenterprisesolutions.fhir.term.graph.factory.FHIRTermGraphFactory;
import com.randomenterprisesolutions.fhir.term.graph.loader.FHIRTermGraphLoader;
import com.randomenterprisesolutions.fhir.term.graph.loader.impl.CodeSystemTermGraphLoader;
import com.randomenterprisesolutions.fhir.term.graph.provider.GraphTermServiceProvider;
import com.randomenterprisesolutions.fhir.term.service.provider.test.FHIRTermServiceProviderTest;
import com.randomenterprisesolutions.fhir.term.spi.FHIRTermServiceProvider;

public class GraphTermServiceProviderTest extends FHIRTermServiceProviderTest {
    private FHIRTermGraph graph = null;

    @Override
    public FHIRTermServiceProvider createProvider() throws Exception {
        FileBasedConfigurationBuilder<PropertiesConfiguration> builder = new FileBasedConfigurationBuilder<>(PropertiesConfiguration.class)
                .configure(new Parameters().properties().setFileName("conf/janusgraph-berkeleyje-lucene.properties"));
        graph = FHIRTermGraphFactory.open(builder.getConfiguration());
        graph.dropAllVertices();

        FHIRTermGraphLoader loader = new CodeSystemTermGraphLoader(graph, codeSystem);
        loader.load();

        return new GraphTermServiceProvider(graph);
    }

    @AfterClass
    public void afterClass() {
        if (graph != null) {
            graph.close();
        }
    }
}
