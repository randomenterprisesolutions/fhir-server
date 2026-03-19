/*
 * (C) Copyright Random Enterprise Solutions 2026
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package com.randomenterprisesolutions.fhir.admin.exception;

/**
 * Thrown when a request contains data that fails basic validation
 * (e.g. a null or blank required field).
 */
public class InvalidTenantRequestException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    public InvalidTenantRequestException(String message) {
        super(message);
    }
}
