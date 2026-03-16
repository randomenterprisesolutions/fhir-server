/*
 * (C) Copyright IBM Corp. 2019, 2022
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package com.randomenterprisesolutions.fhir.server.test.app;

import com.randomenterprisesolutions.fhir.model.resource.Resource;
import com.randomenterprisesolutions.fhir.model.spec.test.IExampleProcessor;

/**
 * An implementation of IExampleProcessor which does nothing. On purpose.
 */
public class NopProcessor implements IExampleProcessor {
    @Override
    public void process(String jsonFile, Resource resource) throws Exception {
        // NOP
    }
}
