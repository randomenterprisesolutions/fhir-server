/*
 * (C) Copyright IBM Corp. 2020
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package com.randomenterprisesolutions.fhir.search.group;

/**
 * Manages access to the GroupSearchCompiler implementation.
 *
 * @implNote This is an EXPERIMENTAL feature.
 */
public class GroupSearchCompilerFactory {
    private GroupSearchCompilerFactory() {
        // No Operation
    }

    /**
     * creates an instance of the GroupSearchCompiler
     * @return
     */
    public static GroupSearchCompiler getInstance() {
        return new GroupSearchCompilerImpl();
    }
}