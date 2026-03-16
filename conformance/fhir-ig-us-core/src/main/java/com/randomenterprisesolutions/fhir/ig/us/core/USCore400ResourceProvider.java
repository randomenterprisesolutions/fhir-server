/*
 * (C) Copyright IBM Corp. 2021
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package com.randomenterprisesolutions.fhir.ig.us.core;

import com.randomenterprisesolutions.fhir.registry.util.PackageRegistryResourceProvider;

public class USCore400ResourceProvider extends PackageRegistryResourceProvider {
    @Override
    public String getPackageId() {
        return "hl7.fhir.us.core.400";
    }
}
