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
import com.randomenterprisesolutions.fhir.model.type.Code;
import com.randomenterprisesolutions.fhir.model.resource.Resource;
import com.randomenterprisesolutions.fhir.path.exception.FHIRPathException;
import com.randomenterprisesolutions.fhir.path.util.FHIRPathUtil;

class FHIRPathPatchDelete extends FHIRPathPatchOperation {
    String fhirPath;

    public FHIRPathPatchDelete(String fhirPath) {
        this.fhirPath = Objects.requireNonNull(fhirPath);
    }

    @Override
    public <T extends Resource> T apply(T resource) throws FHIRPatchException {
        try {
            return FHIRPathUtil.delete(resource, fhirPath);
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
                    .value(Code.of(FHIRPathPatchType.DELETE.value()))
                    .build())
                .part(Parameter.builder()
                    .name(string(PATH))
                    .value(string(fhirPath))
                    .build())
                .build();
    }
}