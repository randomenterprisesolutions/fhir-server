/*
 * (C) Copyright Random Enterprise Solutions 2026
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package com.randomenterprisesolutions.fhir.admin.exception;

import static org.testng.Assert.assertEquals;

import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import com.randomenterprisesolutions.fhir.admin.model.ApiError;

/**
 * Unit tests for {@link AdminExceptionMapper}.
 */
public class AdminExceptionMapperTest {

    private AdminExceptionMapper mapper;

    @BeforeMethod
    public void setUp() {
        mapper = new AdminExceptionMapper();
    }

    @Test
    public void tenantNotFound_mapsto404() {
        Response response = mapper.toResponse(new TenantNotFoundException("acme"));

        assertEquals(response.getStatus(), Status.NOT_FOUND.getStatusCode());
        ApiError error = (ApiError) response.getEntity();
        assertEquals(error.getStatus(), 404);
        assertEquals(error.getError(), "Not Found");
    }

    @Test
    public void tenantAlreadyExists_mapsto409() {
        Response response = mapper.toResponse(new TenantAlreadyExistsException("acme"));

        assertEquals(response.getStatus(), Status.CONFLICT.getStatusCode());
        ApiError error = (ApiError) response.getEntity();
        assertEquals(error.getStatus(), 409);
        assertEquals(error.getError(), "Conflict");
    }

    @Test
    public void invalidRequest_mapsto400() {
        Response response = mapper.toResponse(new InvalidTenantRequestException("tenantId is required"));

        assertEquals(response.getStatus(), Status.BAD_REQUEST.getStatusCode());
        ApiError error = (ApiError) response.getEntity();
        assertEquals(error.getStatus(), 400);
        assertEquals(error.getError(), "Bad Request");
        assertEquals(error.getMessage(), "tenantId is required");
    }

    @Test
    public void unexpectedException_mapsto500() {
        Response response = mapper.toResponse(new RuntimeException("something went wrong"));

        assertEquals(response.getStatus(), Status.INTERNAL_SERVER_ERROR.getStatusCode());
        ApiError error = (ApiError) response.getEntity();
        assertEquals(error.getStatus(), 500);
        assertEquals(error.getError(), "Internal Server Error");
    }
}
