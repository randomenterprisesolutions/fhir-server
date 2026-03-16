/*
 * (C) Copyright IBM Corp. 2021
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package com.randomenterprisesolutions.fhir.bucket.reindex;

/**
 * Drives the $reindex custom operation in parallel.
 */
public abstract class DriveReindexOperation {

    /**
     * Start the main loop.
     */
    public abstract void init();

    /**
     * Program is stopping, so tell the threads they can stop, too.
     */
    public abstract void signalStop();

    /**
     * Wait until things are stopped.
     */
    public abstract void waitForStop();

    /**
     * Wrapper for strings.
     * @param str the string
     * @return the string
     */
    protected static com.randomenterprisesolutions.fhir.model.type.String str(String str) {
        return com.randomenterprisesolutions.fhir.model.type.String.of(str);
    }

    /**
     * Wrapper for integers.
     * @param val the integer
     * @return the integer
     */
    protected static com.randomenterprisesolutions.fhir.model.type.Integer intValue(int val) {
        return com.randomenterprisesolutions.fhir.model.type.Integer.of(val);
    }
}
