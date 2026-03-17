/*
 * (C) Copyright IBM Corp. 2019
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package com.randomenterprisesolutions.fhir.model.spec.test;

import com.randomenterprisesolutions.fhir.model.resource.Resource;

/**
 * Processes the examples
 */
public interface IExampleProcessor {

    /**
     * Process the given resource example
     * @param jsonFile
     * @param resource
     * @throws Exception
     */
    void process(String jsonFile, Resource resource) throws Exception;
}
