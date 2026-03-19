/*
 * (C) Copyright Random Enterprise Solutions 2026
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package com.randomenterprisesolutions.fhir.admin.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

/**
 * Generic error payload returned by the admin API when a request cannot be fulfilled.
 *
 * <pre>
 * {
 *   "status": 404,
 *   "error": "Not Found",
 *   "message": "Tenant 'acme' does not exist"
 * }
 * </pre>
 */
@JsonInclude(Include.NON_NULL)
public class ApiError {

    /** HTTP status code (mirrored in the payload for client convenience). */
    private int status;

    /** Short error category label (e.g. "Not Found", "Conflict"). */
    private String error;

    /** Human-readable explanation of the specific problem. */
    private String message;

    // ── constructors ──────────────────────────────────────────────────────────

    public ApiError() {
    }

    public ApiError(int status, String error, String message) {
        this.status = status;
        this.error = error;
        this.message = message;
    }

    // ── getters / setters ─────────────────────────────────────────────────────

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public String getError() {
        return error;
    }

    public void setError(String error) {
        this.error = error;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}
