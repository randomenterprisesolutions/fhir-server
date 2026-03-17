/*
 * (C) Copyright IBM Corp. 2019, 2020
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package com.randomenterprisesolutions.fhir.path.function;

import static com.randomenterprisesolutions.fhir.path.util.FHIRPathUtil.getStringValue;
import static com.randomenterprisesolutions.fhir.path.util.FHIRPathUtil.hasStringValue;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import com.randomenterprisesolutions.fhir.model.type.Extension;
import com.randomenterprisesolutions.fhir.path.FHIRPathNode;
import com.randomenterprisesolutions.fhir.path.evaluator.FHIRPathEvaluator.EvaluationContext;

public class ExtensionFunction extends FHIRPathAbstractFunction {
    @Override
    public String getName() {
        return "extension";
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
    public Collection<FHIRPathNode> apply(EvaluationContext evaluationContext, Collection<FHIRPathNode> context, List<Collection<FHIRPathNode>> arguments) {
        List<FHIRPathNode> result = new ArrayList<>();
        if (hasStringValue(arguments.get(0))) {
            String url = getStringValue(arguments.get(0)).string();
            for (FHIRPathNode node : context) {
                for (FHIRPathNode child : node.children()) {
                    if (child.isElementNode() && child.asElementNode().element().is(Extension.class)) {
                        Extension extension = child.asElementNode().element().as(Extension.class);
                        if (extension.getUrl().equals(url)) {
                            result.add(child);
                        }
                    }
                }
            }
        }
        return result;
    }
}
