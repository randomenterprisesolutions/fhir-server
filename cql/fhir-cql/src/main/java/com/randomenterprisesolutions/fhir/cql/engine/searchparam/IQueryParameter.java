/*
 * (C) Copyright IBM Corp. 2021
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.randomenterprisesolutions.fhir.cql.engine.searchparam;

import com.randomenterprisesolutions.fhir.search.SearchConstants.Modifier;

public interface IQueryParameter {

    Boolean getMissing();

    Modifier getModifier();

    String getParameterValue();
}
