/*
 * (C) Copyright IBM Corp. 2016, 2022
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package com.randomenterprisesolutions.fhir.persistence.context;

import com.randomenterprisesolutions.fhir.core.context.FHIRPagingContext;
import com.randomenterprisesolutions.fhir.model.type.Instant;

public interface FHIRHistoryContext extends FHIRPagingContext {
    
    Instant getSince();
    void setSince(Instant since);
}
