/*
 * (C) Copyright IBM Corp. 2021
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.randomenterprisesolutions.fhir.bulkdata.load.partition.transformer;

import java.util.List;

import com.randomenterprisesolutions.fhir.exception.FHIRException;
import com.randomenterprisesolutions.fhir.operation.bulkdata.model.type.BulkDataSource;

/**
 * Partitioning Sources
 */
public interface PartitionSourceTransformer {
    /**
     * Converts the Source-Type and Location to multiple BulkDataSources for partitioning
     *
     * @param source
     * @param type
     * @param location
     * @return
     * @throws FHIRException
     */
    public List<BulkDataSource> transformToDataSources(String source, String type, String location) throws FHIRException;
}
