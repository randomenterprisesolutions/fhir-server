/*
 * (C) Copyright IBM Corp. 2019
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package com.randomenterprisesolutions.fhir.path.test;

import java.io.BufferedReader;
import java.util.ArrayList;
import java.util.List;

import org.testng.Assert;
import org.testng.annotations.Test;

import com.randomenterprisesolutions.fhir.examples.ExamplesUtil;
import com.randomenterprisesolutions.fhir.model.format.Format;
import com.randomenterprisesolutions.fhir.model.parser.FHIRParser;
import com.randomenterprisesolutions.fhir.model.resource.Patient;
import com.randomenterprisesolutions.fhir.model.resource.Resource;
import com.randomenterprisesolutions.fhir.model.type.Element;
import com.randomenterprisesolutions.fhir.model.visitor.PathAwareVisitor;
import com.randomenterprisesolutions.fhir.path.FHIRPathElementNode;
import com.randomenterprisesolutions.fhir.path.FHIRPathResourceNode;
import com.randomenterprisesolutions.fhir.path.FHIRPathTree;
import com.randomenterprisesolutions.fhir.path.visitor.FHIRPathDefaultNodeVisitor;

public class NodeVisitorTest {
    @Test
    public void testNodeVisitor() throws Exception {
        try (BufferedReader reader = new BufferedReader(ExamplesUtil.resourceReader("json/spec/patient-example.json"))) {
            Patient patient = FHIRParser.parser(Format.JSON).parse(reader);
                        
            List<String> paths = new ArrayList<>();
            patient.accept(new PathAwareVisitor() {
                @Override
                public void doVisitStart(String elementName, int elementIndex, Resource resource) {
                    paths.add(getPath());
                }
                
                @Override
                public void doVisitStart(String elementName, int elementIndex, Element element) {
                    paths.add(getPath());
                }
            });
                        
            List<String> treePaths = new ArrayList<>();
            FHIRPathTree tree = FHIRPathTree.tree(patient);
            tree.getRoot().accept(new FHIRPathDefaultNodeVisitor() {
                @Override
                public void doVisit(FHIRPathElementNode node) {
                    treePaths.add(node.path());
                }

                @Override
                public void doVisit(FHIRPathResourceNode node) {
                    treePaths.add(node.path());
                }
            });
            
            Assert.assertEquals(paths, treePaths);
            Assert.assertEquals(treePaths, paths);
        }
    }
}