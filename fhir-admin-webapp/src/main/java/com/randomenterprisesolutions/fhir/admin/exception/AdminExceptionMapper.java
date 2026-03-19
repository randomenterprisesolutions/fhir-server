/*
 * (C) Copyright Random Enterprise Solutions 2026
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package com.randomenterprisesolutions.fhir.admin.exception;

import java.util.logging.Level;
import java.util.logging.Logger;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;

import com.randomenterprisesolutions.fhir.admin.model.ApiError;

/**
 * JAX-RS {@link ExceptionMapper} that converts domain exceptions thrown by the
 * admin service layer into well-formed JSON error responses.
 *
 * <table border="1">
 *   <tr><th>Exception</th><th>HTTP status</th></tr>
 *   <tr><td>{@link TenantNotFoundException}</td><td>404 Not Found</td></tr>
 *   <tr><td>{@link TenantAlreadyExistsException}</td><td>409 Conflict</td></tr>
 *   <tr><td>{@link InvalidTenantRequestException}</td><td>400 Bad Request</td></tr>
 *   <tr><td>Any other {@link RuntimeException}</td><td>500 Internal Server Error</td></tr>
 * </table>
 */
@Provider
public class AdminExceptionMapper implements ExceptionMapper<RuntimeException> {

    private static final Logger log = Logger.getLogger(AdminExceptionMapper.class.getName());

    @Override
    public Response toResponse(RuntimeException ex) {
        if (ex instanceof TenantNotFoundException) {
            return buildResponse(Status.NOT_FOUND, "Not Found", ex.getMessage());
        }
        if (ex instanceof TenantAlreadyExistsException) {
            return buildResponse(Status.CONFLICT, "Conflict", ex.getMessage());
        }
        if (ex instanceof InvalidTenantRequestException) {
            return buildResponse(Status.BAD_REQUEST, "Bad Request", ex.getMessage());
        }
        // Unexpected errors — log with full stack trace
        log.log(Level.SEVERE, "Unexpected error in admin endpoint", ex);
        return buildResponse(Status.INTERNAL_SERVER_ERROR, "Internal Server Error",
                "An unexpected error occurred; see server logs for details");
    }

    private Response buildResponse(Status status, String error, String message) {
        ApiError body = new ApiError(status.getStatusCode(), error, message);
        return Response.status(status)
                .type(MediaType.APPLICATION_JSON)
                .entity(body)
                .build();
    }
}
