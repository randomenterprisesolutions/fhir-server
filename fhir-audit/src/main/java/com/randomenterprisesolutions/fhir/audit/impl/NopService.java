/*
 * (C) Copyright IBM Corp. 2016, 2020
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package com.randomenterprisesolutions.fhir.audit.impl;

import com.randomenterprisesolutions.fhir.audit.AuditLogService;
import com.randomenterprisesolutions.fhir.audit.beans.AuditLogEntry;
import com.randomenterprisesolutions.fhir.config.PropertyGroup;

/**
 * This class serves as a no-op audit service logger.
 * It simply allows audit logging to be effectively disabled.
 */
public class NopService implements AuditLogService {

    public NopService() {
        super();
    }

    @Override
    public boolean isEnabled() {
        return false;
    }

    @Override
    public void initialize(PropertyGroup auditLogProperties) throws Exception {
        // No Operation
    }

    @Override
    public void stop(PropertyGroup auditLogProperties) throws Exception {
        // No Operation
    }

    @Override
    public void logEntry(AuditLogEntry logEntry) {
        // No Operation
    }
}