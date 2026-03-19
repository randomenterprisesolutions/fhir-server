/*
 * (C) Copyright Random Enterprise Solutions 2026
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package com.randomenterprisesolutions.fhir.admin.exception;

/**
 * Thrown when a client tries to create a tenant with an ID that is already registered.
 */
public class TenantAlreadyExistsException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    private final String tenantId;

    public TenantAlreadyExistsException(String tenantId) {
        super("Tenant '" + tenantId + "' already exists");
        this.tenantId = tenantId;
    }

    public String getTenantId() {
        return tenantId;
    }
}
