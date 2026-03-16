/*
 * (C) Copyright IBM Corp. 2019, 2020
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package com.randomenterprisesolutions.fhir.validation.test;

import com.randomenterprisesolutions.fhir.registry.util.PackageRegistryResourceProvider;

public class FHIRValidationTestResourceProvider extends PackageRegistryResourceProvider {
    @Override
    public String getPackageId() {
        return "fhir.validation.test";
    }
}
