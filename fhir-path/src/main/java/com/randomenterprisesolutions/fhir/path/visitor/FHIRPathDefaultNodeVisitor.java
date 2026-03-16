/*
 * (C) Copyright IBM Corp. 2019
 * 
 * SPDX-License-Identifier: Apache-2.0
 */

package com.randomenterprisesolutions.fhir.path.visitor;

import com.randomenterprisesolutions.fhir.path.FHIRPathBooleanValue;
import com.randomenterprisesolutions.fhir.path.FHIRPathDateTimeValue;
import com.randomenterprisesolutions.fhir.path.FHIRPathDateValue;
import com.randomenterprisesolutions.fhir.path.FHIRPathDecimalValue;
import com.randomenterprisesolutions.fhir.path.FHIRPathElementNode;
import com.randomenterprisesolutions.fhir.path.FHIRPathIntegerValue;
import com.randomenterprisesolutions.fhir.path.FHIRPathNode;
import com.randomenterprisesolutions.fhir.path.FHIRPathQuantityNode;
import com.randomenterprisesolutions.fhir.path.FHIRPathQuantityValue;
import com.randomenterprisesolutions.fhir.path.FHIRPathResourceNode;
import com.randomenterprisesolutions.fhir.path.FHIRPathStringValue;
import com.randomenterprisesolutions.fhir.path.FHIRPathTimeValue;
import com.randomenterprisesolutions.fhir.path.FHIRPathTypeInfoNode;

public class FHIRPathDefaultNodeVisitor implements FHIRPathNodeVisitor {
    protected void doVisit(FHIRPathResourceNode node) {
        // do nothing
    }
    
    protected void doVisit(FHIRPathElementNode node) {
        // do nothing
    }
    
    protected void visitChildren(FHIRPathNode node) {
        for (FHIRPathNode child : node.children()) {
            child.accept(this);
        }
    }
    
    @Override
    public void visit(FHIRPathBooleanValue value) {
        // do nothing
    }

    @Override
    public void visit(FHIRPathDateValue value) {
        // do nothing
    }

    @Override
    public void visit(FHIRPathDateTimeValue value) {
        // do nothing
    }

    @Override
    public void visit(FHIRPathDecimalValue value) {
        // do nothing
    }

    @Override
    public final void visit(FHIRPathElementNode node) {
        doVisit(node);
        visitChildren(node);
    }

    @Override
    public void visit(FHIRPathIntegerValue value) {
        // do nothing
    }

    @Override
    public void visit(FHIRPathQuantityValue value) {
        // do nothing
    }
    
    @Override
    public void visit(FHIRPathQuantityNode node) {
        visit((FHIRPathElementNode) node);
    }

    @Override
    public final void visit(FHIRPathResourceNode node) {
        doVisit(node);
        visitChildren(node);
    }

    @Override
    public void visit(FHIRPathStringValue value) {
        // do nothing
    }

    @Override
    public void visit(FHIRPathTimeValue value) {
        // do nothing
    }

    @Override
    public void visit(FHIRPathTypeInfoNode node) {
        // do nothing
    }
}