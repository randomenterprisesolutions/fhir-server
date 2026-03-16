/*
 * (C) Copyright IBM Corp. 2019
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package com.randomenterprisesolutions.fhir.path.function;

import static com.randomenterprisesolutions.fhir.path.FHIRPathIntegerValue.integerValue;
import static com.randomenterprisesolutions.fhir.path.util.FHIRPathUtil.getStringValue;
import static com.randomenterprisesolutions.fhir.path.util.FHIRPathUtil.singleton;

import java.util.Collection;
import java.util.List;

import com.randomenterprisesolutions.fhir.path.FHIRPathNode;
import com.randomenterprisesolutions.fhir.path.evaluator.FHIRPathEvaluator.EvaluationContext;

public class LengthFunction extends FHIRPathStringAbstractFunction {
    @Override
    public String getName() {
        return "length";
    }

    @Override
    public int getMinArity() {
        return 0;
    }

    @Override
    public int getMaxArity() {
        return 0;
    }

    @Override
    public Collection<FHIRPathNode> doApply(EvaluationContext evaluationContext, Collection<FHIRPathNode> context, List<Collection<FHIRPathNode>> arguments) {
       
        return singleton(integerValue(getStringValue(context).length()));
    }
}
