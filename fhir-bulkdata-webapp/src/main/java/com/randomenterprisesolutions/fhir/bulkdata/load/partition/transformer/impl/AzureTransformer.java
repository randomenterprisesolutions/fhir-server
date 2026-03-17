/*
 * (C) Copyright IBM Corp. 2021
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package com.randomenterprisesolutions.fhir.bulkdata.load.partition.transformer.impl;

import java.util.Arrays;
import java.util.List;

import com.randomenterprisesolutions.fhir.bulkdata.load.partition.transformer.PartitionSourceTransformer;
import com.randomenterprisesolutions.fhir.bulkdata.provider.impl.AzureProvider;
import com.randomenterprisesolutions.fhir.exception.FHIRException;
import com.randomenterprisesolutions.fhir.operation.bulkdata.model.type.BulkDataSource;

/**
 * At most one BulkDataSource is created in a List, but throws when Azure doesn't exist.
 */
public class AzureTransformer implements PartitionSourceTransformer {
    @Override
    public List<BulkDataSource> transformToDataSources(String source, String type, String location) throws FHIRException {
        AzureProvider provider = new AzureProvider(source);
        // We want to get the Provider
        provider.getSize(location);
        return Arrays.asList(new BulkDataSource(type, location));
    }
}