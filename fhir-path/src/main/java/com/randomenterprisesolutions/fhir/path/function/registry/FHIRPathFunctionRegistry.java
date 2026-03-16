/*
 * (C) Copyright IBM Corp. 2019, 2020
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package com.randomenterprisesolutions.fhir.path.function.registry;

import java.util.Collections;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import com.randomenterprisesolutions.fhir.path.function.AllFalseFunction;
import com.randomenterprisesolutions.fhir.path.function.AllTrueFunction;
import com.randomenterprisesolutions.fhir.path.function.AnyFalseFunction;
import com.randomenterprisesolutions.fhir.path.function.AnyTrueFunction;
import com.randomenterprisesolutions.fhir.path.function.BetweenFunction;
import com.randomenterprisesolutions.fhir.path.function.CheckModifiersFunction;
import com.randomenterprisesolutions.fhir.path.function.ChildrenFunction;
import com.randomenterprisesolutions.fhir.path.function.CombineFunction;
import com.randomenterprisesolutions.fhir.path.function.ConformsToFunction;
import com.randomenterprisesolutions.fhir.path.function.ContainsFunction;
import com.randomenterprisesolutions.fhir.path.function.ConvertsToBooleanFunction;
import com.randomenterprisesolutions.fhir.path.function.ConvertsToDateFunction;
import com.randomenterprisesolutions.fhir.path.function.ConvertsToDateTimeFunction;
import com.randomenterprisesolutions.fhir.path.function.ConvertsToDecimalFunction;
import com.randomenterprisesolutions.fhir.path.function.ConvertsToIntegerFunction;
import com.randomenterprisesolutions.fhir.path.function.ConvertsToQuantityFunction;
import com.randomenterprisesolutions.fhir.path.function.ConvertsToStringFunction;
import com.randomenterprisesolutions.fhir.path.function.ConvertsToTimeFunction;
import com.randomenterprisesolutions.fhir.path.function.CountFunction;
import com.randomenterprisesolutions.fhir.path.function.DescendantsFunction;
import com.randomenterprisesolutions.fhir.path.function.DistinctFunction;
import com.randomenterprisesolutions.fhir.path.function.EmptyFunction;
import com.randomenterprisesolutions.fhir.path.function.EndsWithFunction;
import com.randomenterprisesolutions.fhir.path.function.ExcludeFunction;
import com.randomenterprisesolutions.fhir.path.function.ExpandFunction;
import com.randomenterprisesolutions.fhir.path.function.ExtensionFunction;
import com.randomenterprisesolutions.fhir.path.function.FHIRPathFunction;
import com.randomenterprisesolutions.fhir.path.function.FirstFunction;
import com.randomenterprisesolutions.fhir.path.function.GetValueFunction;
import com.randomenterprisesolutions.fhir.path.function.HasValueFunction;
import com.randomenterprisesolutions.fhir.path.function.HtmlChecksFunction;
import com.randomenterprisesolutions.fhir.path.function.IndexOfFunction;
import com.randomenterprisesolutions.fhir.path.function.IntersectFunction;
import com.randomenterprisesolutions.fhir.path.function.IsDistinctFunction;
import com.randomenterprisesolutions.fhir.path.function.ItemFunction;
import com.randomenterprisesolutions.fhir.path.function.LastFunction;
import com.randomenterprisesolutions.fhir.path.function.LengthFunction;
import com.randomenterprisesolutions.fhir.path.function.LookupFunction;
import com.randomenterprisesolutions.fhir.path.function.LowerFunction;
import com.randomenterprisesolutions.fhir.path.function.MatchesFunction;
import com.randomenterprisesolutions.fhir.path.function.MemberOfFunction;
import com.randomenterprisesolutions.fhir.path.function.NotFunction;
import com.randomenterprisesolutions.fhir.path.function.NowFunction;
import com.randomenterprisesolutions.fhir.path.function.ReplaceFunction;
import com.randomenterprisesolutions.fhir.path.function.ReplaceMatchesFunction;
import com.randomenterprisesolutions.fhir.path.function.ResolveFunction;
import com.randomenterprisesolutions.fhir.path.function.SingleFunction;
import com.randomenterprisesolutions.fhir.path.function.SkipFunction;
import com.randomenterprisesolutions.fhir.path.function.SliceFunction;
import com.randomenterprisesolutions.fhir.path.function.StartsWithFunction;
import com.randomenterprisesolutions.fhir.path.function.SubsetOfFunction;
import com.randomenterprisesolutions.fhir.path.function.SubstringFunction;
import com.randomenterprisesolutions.fhir.path.function.SubsumedByFunction;
import com.randomenterprisesolutions.fhir.path.function.SubsumesFunction;
import com.randomenterprisesolutions.fhir.path.function.SupersetOfFunction;
import com.randomenterprisesolutions.fhir.path.function.TailFunction;
import com.randomenterprisesolutions.fhir.path.function.TakeFunction;
import com.randomenterprisesolutions.fhir.path.function.TimeOfDayFunction;
import com.randomenterprisesolutions.fhir.path.function.ToBooleanFunction;
import com.randomenterprisesolutions.fhir.path.function.ToCharsFunction;
import com.randomenterprisesolutions.fhir.path.function.ToDateFunction;
import com.randomenterprisesolutions.fhir.path.function.ToDateTimeFunction;
import com.randomenterprisesolutions.fhir.path.function.ToDecimalFunction;
import com.randomenterprisesolutions.fhir.path.function.ToIntegerFunction;
import com.randomenterprisesolutions.fhir.path.function.ToQuantityFunction;
import com.randomenterprisesolutions.fhir.path.function.ToStringFunction;
import com.randomenterprisesolutions.fhir.path.function.ToTimeFunction;
import com.randomenterprisesolutions.fhir.path.function.TodayFunction;
import com.randomenterprisesolutions.fhir.path.function.TranslateFunction;
import com.randomenterprisesolutions.fhir.path.function.TypeFunction;
import com.randomenterprisesolutions.fhir.path.function.UnionFunction;
import com.randomenterprisesolutions.fhir.path.function.UpperFunction;
import com.randomenterprisesolutions.fhir.path.function.ValidateCSFunction;
import com.randomenterprisesolutions.fhir.path.function.ValidateVSFunction;

public final class FHIRPathFunctionRegistry {
    private static final FHIRPathFunctionRegistry INSTANCE = new FHIRPathFunctionRegistry();
    private Map<String, FHIRPathFunction> functionMap = new ConcurrentHashMap<>();

    private FHIRPathFunctionRegistry() {
        registerFunctions();
    }

    public static FHIRPathFunctionRegistry getInstance() {
        return INSTANCE;
    }

    public void register(FHIRPathFunction function) {
        functionMap.put(function.getName(), function);
    }

    public FHIRPathFunction getFunction(String functionName) {
        return functionMap.get(functionName);
    }

    public Set<String> getFunctionNames() {
        return Collections.unmodifiableSet(functionMap.keySet());
    }

    private void registerFunctions() {
        register(new AllFalseFunction());
        register(new AllTrueFunction());
        register(new AnyFalseFunction());
        register(new AnyTrueFunction());
        register(new BetweenFunction());
        register(new CheckModifiersFunction());
        register(new ChildrenFunction());
        register(new CombineFunction());
        register(new ConformsToFunction());
        register(new ContainsFunction());
        register(new ConvertsToBooleanFunction());
        register(new ConvertsToDateFunction());
        register(new ConvertsToDateTimeFunction());
        register(new ConvertsToDecimalFunction());
        register(new ConvertsToIntegerFunction());
        register(new ConvertsToQuantityFunction());
        register(new ConvertsToStringFunction());
        register(new ConvertsToTimeFunction());
        register(new CountFunction());
        register(new DescendantsFunction());
        register(new DistinctFunction());
        register(new EmptyFunction());
        register(new EndsWithFunction());
        register(new ExcludeFunction());
        register(new ExtensionFunction());
        register(new FirstFunction());
        register(new GetValueFunction());
        register(new HasValueFunction());
        register(new HtmlChecksFunction());
        register(new IndexOfFunction());
        register(new IntersectFunction());
        register(new IsDistinctFunction());
        register(new ItemFunction());
        register(new LastFunction());
        register(new LengthFunction());
        register(new LowerFunction());
        register(new MatchesFunction());
        register(new MemberOfFunction());
        register(new NotFunction());
        register(new NowFunction());
        register(new ReplaceFunction());
        register(new ReplaceMatchesFunction());
        register(new ResolveFunction());
        register(new SingleFunction());
        register(new SkipFunction());
        register(new SliceFunction());
        register(new StartsWithFunction());
        register(new SubsetOfFunction());
        register(new SubstringFunction());
        register(new SupersetOfFunction());
        register(new TailFunction());
        register(new TakeFunction());
        register(new TimeOfDayFunction());
        register(new ToBooleanFunction());
        register(new ToCharsFunction());
        register(new ToDateFunction());
        register(new ToDateTimeFunction());
        register(new ToDecimalFunction());
        register(new ToIntegerFunction());
        register(new ToQuantityFunction());
        register(new ToStringFunction());
        register(new ToTimeFunction());
        register(new TodayFunction());
        register(new TypeFunction());
        register(new UnionFunction());
        register(new UpperFunction());

        // register terminology functions
        register(new ExpandFunction());
        register(new LookupFunction());
        register(new SubsumedByFunction());
        register(new SubsumesFunction());
        register(new TranslateFunction());
        register(new ValidateCSFunction());
        register(new ValidateVSFunction());
    }
}