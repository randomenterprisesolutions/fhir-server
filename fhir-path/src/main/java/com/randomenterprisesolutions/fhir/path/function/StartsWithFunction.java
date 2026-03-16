/*
 * (C) Copyright IBM Corp. 2019
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package com.randomenterprisesolutions.fhir.path.function;

import static com.randomenterprisesolutions.fhir.path.evaluator.FHIRPathEvaluator.SINGLETON_FALSE;
import static com.randomenterprisesolutions.fhir.path.evaluator.FHIRPathEvaluator.SINGLETON_TRUE;
import static com.randomenterprisesolutions.fhir.path.util.FHIRPathUtil.getStringValue;
import java.util.Collection;
import java.util.List;

import com.randomenterprisesolutions.fhir.path.FHIRPathNode;
import com.randomenterprisesolutions.fhir.path.evaluator.FHIRPathEvaluator.EvaluationContext;

public class StartsWithFunction extends FHIRPathStringAbstractFunction {
    @Override
    public String getName() {
        return "startsWith";
    }

    @Override
    public int getMinArity() {
        return 1;
    }

    @Override
    public int getMaxArity() {
        return 1;
    }
    

    @Override
    protected Collection<FHIRPathNode> doApply(EvaluationContext evaluationContext, Collection<FHIRPathNode> context, List<Collection<FHIRPathNode>> arguments) {
        
        return getStringValue(context).startsWith(getStringValue(arguments.get(0))) ? SINGLETON_TRUE : SINGLETON_FALSE;
    }
}
