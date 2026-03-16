/*
 * (C) Copyright IBM Corp. 2020
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package com.randomenterprisesolutions.fhir.path.patch;

import static com.randomenterprisesolutions.fhir.model.type.String.string;

import java.util.Objects;

import com.randomenterprisesolutions.fhir.model.patch.exception.FHIRPatchException;
import com.randomenterprisesolutions.fhir.model.resource.Parameters.Parameter;
import com.randomenterprisesolutions.fhir.model.resource.Resource;
import com.randomenterprisesolutions.fhir.model.type.Code;
import com.randomenterprisesolutions.fhir.model.type.Element;
import com.randomenterprisesolutions.fhir.path.exception.FHIRPathException;
import com.randomenterprisesolutions.fhir.path.util.FHIRPathUtil;

class FHIRPathPatchAdd extends FHIRPathPatchOperation {
    String fhirPath;
    String name;
    Element value;

    public FHIRPathPatchAdd(String fhirPath, String name, Element value) {
        this.fhirPath = Objects.requireNonNull(fhirPath);
        this.name = Objects.requireNonNull(name);
        this.value = Objects.requireNonNull(value);
    }

    @Override
    public <T extends Resource> T apply(T resource) throws FHIRPatchException {
        try {
            return FHIRPathUtil.add(resource, fhirPath, name, value);
        } catch (FHIRPathException e) {
            throw new FHIRPatchException("Error executing fhirPath", fhirPath);
        }
    }

    @Override
    public Parameter toParameter() {
        return Parameter.builder()
                .name(string(OPERATION))
                .part(Parameter.builder()
                    .name(string(TYPE))
                    .value(Code.of(FHIRPathPatchType.ADD.value()))
                    .build())
                .part(Parameter.builder()
                    .name(string(PATH))
                    .value(string(fhirPath))
                    .build())
                .part(Parameter.builder()
                    .name(string(NAME))
                    .value(string(name))
                    .build())
                .part(Parameter.builder()
                    .name(string(VALUE))
                    .value(value)
                    .build())
                .build();
    }
}