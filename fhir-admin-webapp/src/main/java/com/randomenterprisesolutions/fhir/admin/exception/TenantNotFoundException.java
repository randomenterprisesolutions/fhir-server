/*
 * (C) Copyright Random Enterprise Solutions 2026
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package com.randomenterprisesolutions.fhir.admin.exception;

/**
 * Thrown when an operation targets a tenant that does not exist in the store.
 */
public class TenantNotFoundException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    private final String tenantId;

    public TenantNotFoundException(String tenantId) {
        super("Tenant '" + tenantId + "' does not exist");
        this.tenantId = tenantId;
    }

    public String getTenantId() {
        return tenantId;
    }
}
