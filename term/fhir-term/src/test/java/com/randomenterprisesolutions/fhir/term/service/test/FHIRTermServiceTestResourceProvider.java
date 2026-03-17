/*
 * (C) Copyright IBM Corp. 2019, 2020
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package com.randomenterprisesolutions.fhir.term.service.test;

import com.randomenterprisesolutions.fhir.registry.util.PackageRegistryResourceProvider;

public class FHIRTermServiceTestResourceProvider extends PackageRegistryResourceProvider {
    @Override
    public String getPackageId() {
        return "fhir.term.service.test";
    }
}
