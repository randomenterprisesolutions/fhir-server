/*
 * (C) Copyright Random Enterprise Solutions 2026
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package com.randomenterprisesolutions.fhir.admin.schema;

/**
 * Thrown when the FHIR persistence schema cannot be provisioned for a new tenant.
 */
public class SchemaProvisioningException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    public SchemaProvisioningException(String message) {
        super(message);
    }

    public SchemaProvisioningException(String message, Throwable cause) {
        super(message, cause);
    }
}
