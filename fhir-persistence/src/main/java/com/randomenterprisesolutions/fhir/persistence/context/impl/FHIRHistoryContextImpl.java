/*
 * (C) Copyright IBM Corp. 2016, 2022
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package com.randomenterprisesolutions.fhir.persistence.context.impl;

import com.randomenterprisesolutions.fhir.core.context.impl.FHIRPagingContextImpl;
import com.randomenterprisesolutions.fhir.model.type.Instant;
import com.randomenterprisesolutions.fhir.persistence.context.FHIRHistoryContext;

public class FHIRHistoryContextImpl extends FHIRPagingContextImpl implements FHIRHistoryContext {
    private Instant since = null;
    
    public FHIRHistoryContextImpl() {
    }

    @Override
    public Instant getSince() {
        return since;
    }

    @Override
    public void setSince(Instant since) {
        this.since = since;
    }
}
