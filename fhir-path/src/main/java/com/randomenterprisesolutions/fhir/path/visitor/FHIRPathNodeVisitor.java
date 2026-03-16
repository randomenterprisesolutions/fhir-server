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
import com.randomenterprisesolutions.fhir.path.FHIRPathQuantityNode;
import com.randomenterprisesolutions.fhir.path.FHIRPathQuantityValue;
import com.randomenterprisesolutions.fhir.path.FHIRPathResourceNode;
import com.randomenterprisesolutions.fhir.path.FHIRPathStringValue;
import com.randomenterprisesolutions.fhir.path.FHIRPathTimeValue;
import com.randomenterprisesolutions.fhir.path.FHIRPathTypeInfoNode;

public interface FHIRPathNodeVisitor {
    void visit(FHIRPathBooleanValue value);
    void visit(FHIRPathDateValue value);
    void visit(FHIRPathDateTimeValue value);
    void visit(FHIRPathDecimalValue value);
    void visit(FHIRPathElementNode node);
    void visit(FHIRPathIntegerValue value);
    void visit(FHIRPathQuantityNode node);
    void visit(FHIRPathQuantityValue value);
    void visit(FHIRPathResourceNode node);
    void visit(FHIRPathStringValue value);
    void visit(FHIRPathTimeValue value);
    void visit(FHIRPathTypeInfoNode node);
}