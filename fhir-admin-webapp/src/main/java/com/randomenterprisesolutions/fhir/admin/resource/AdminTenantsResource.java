/*
 * (C) Copyright Random Enterprise Solutions 2026
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package com.randomenterprisesolutions.fhir.admin.resource;

import java.net.URI;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.annotation.security.RolesAllowed;
import javax.enterprise.context.RequestScoped;
import javax.inject.Inject;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.core.UriInfo;

import com.randomenterprisesolutions.fhir.admin.model.CreateTenantRequest;
import com.randomenterprisesolutions.fhir.admin.model.TenantConfig;
import com.randomenterprisesolutions.fhir.admin.model.UpdateTenantRequest;
import com.randomenterprisesolutions.fhir.admin.service.TenantService;

/**
 * JAX-RS resource implementing the admin tenant management API.
 *
 * <pre>
 * POST   /admin/tenants                      Create tenant
 * GET    /admin/tenants                      List all tenants
 * GET    /admin/tenants/{tenantId}           Get tenant config
 * PUT    /admin/tenants/{tenantId}           Update tenant config
 * DELETE /admin/tenants/{tenantId}           Delete / disable tenant
 * POST   /admin/tenants/{tenantId}/reload    Force cache reload
 * </pre>
 *
 * <p>All endpoints require the caller to hold the {@code FHIRAdmins} role,
 * enforced via HTTP Basic authentication. In Liberty, the {@code fhiradmin}
 * user (member of the {@code FHIRAdmins} group in the basicRegistry) is the
 * default admin credential — identical to the pattern used by the main FHIR server.
 */
@Path("/admin/tenants")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
@RolesAllowed("FHIRAdmins")
@RequestScoped
public class AdminTenantsResource {

    private static final Logger log = Logger.getLogger(AdminTenantsResource.class.getName());

    @Inject
    private TenantService tenantService;

    @Context
    private UriInfo uriInfo;

    // ── POST /admin/tenants ──────────────────────────────────────────────────

    /**
     * Create a new tenant.
     *
     * <p>On success, returns {@code 201 Created} with the created
     * {@link TenantConfig} in the body and a {@code Location} header
     * pointing to the new resource.
     *
     * @param request the creation request; {@code tenantId} and
     *                {@code datastoreType} are required
     * @return {@code 201} or an error response
     */
    @POST
    public Response createTenant(CreateTenantRequest request) {
        log.entering(getClass().getName(), "createTenant");
        try {
            TenantConfig created = tenantService.createTenant(request);
            URI location = uriInfo.getAbsolutePathBuilder()
                    .path(created.getTenantId())
                    .build();
            return Response.created(location).entity(created).build();
        } finally {
            log.exiting(getClass().getName(), "createTenant");
        }
    }

    // ── GET /admin/tenants ───────────────────────────────────────────────────

    /**
     * List all registered tenants.
     *
     * @return {@code 200} with a JSON array of {@link TenantConfig} objects
     */
    @GET
    public Response listTenants() {
        log.entering(getClass().getName(), "listTenants");
        try {
            List<TenantConfig> tenants = tenantService.listTenants();
            return Response.ok(tenants).build();
        } finally {
            log.exiting(getClass().getName(), "listTenants");
        }
    }

    // ── GET /admin/tenants/{tenantId} ────────────────────────────────────────

    /**
     * Retrieve a single tenant by ID.
     *
     * @param tenantId the tenant identifier
     * @return {@code 200} with the {@link TenantConfig}, or {@code 404}
     */
    @GET
    @Path("{tenantId}")
    public Response getTenant(@PathParam("tenantId") String tenantId) {
        log.entering(getClass().getName(), "getTenant");
        try {
            TenantConfig config = tenantService.getTenant(tenantId);
            return Response.ok(config).build();
        } finally {
            log.exiting(getClass().getName(), "getTenant");
        }
    }

    // ── PUT /admin/tenants/{tenantId} ────────────────────────────────────────

    /**
     * Update an existing tenant.
     *
     * <p>Only fields present (non-null) in the request body are applied.
     *
     * @param tenantId the tenant identifier
     * @param request  partial or full update payload
     * @return {@code 200} with the updated {@link TenantConfig}, or {@code 404}
     */
    @PUT
    @Path("{tenantId}")
    public Response updateTenant(
            @PathParam("tenantId") String tenantId,
            UpdateTenantRequest request) {
        log.entering(getClass().getName(), "updateTenant");
        try {
            TenantConfig updated = tenantService.updateTenant(tenantId, request);
            return Response.ok(updated).build();
        } finally {
            log.exiting(getClass().getName(), "updateTenant");
        }
    }

    // ── DELETE /admin/tenants/{tenantId} ─────────────────────────────────────

    /**
     * Delete (remove) a tenant.
     *
     * @param tenantId the tenant identifier
     * @return {@code 204 No Content} on success, or {@code 404}
     */
    @DELETE
    @Path("{tenantId}")
    public Response deleteTenant(@PathParam("tenantId") String tenantId) {
        log.entering(getClass().getName(), "deleteTenant");
        try {
            tenantService.deleteTenant(tenantId);
            return Response.noContent().build();
        } finally {
            log.exiting(getClass().getName(), "deleteTenant");
        }
    }

    // ── POST /admin/tenants/{tenantId}/reload ────────────────────────────────

    /**
     * Force an immediate config-cache reload for the given tenant.
     *
     * <p>This is a lightweight signal endpoint; the actual invalidation work
     * is performed asynchronously across cluster nodes.
     *
     * @param tenantId the tenant identifier
     * @return {@code 202 Accepted}, or {@code 404} if the tenant does not exist
     */
    @POST
    @Path("{tenantId}/reload")
    public Response reloadTenant(@PathParam("tenantId") String tenantId) {
        log.entering(getClass().getName(), "reloadTenant");
        try {
            tenantService.reloadTenant(tenantId);
            return Response.status(Status.ACCEPTED).build();
        } catch (Exception ex) {
            log.log(Level.WARNING, "Error during reload of tenant " + tenantId, ex);
            throw ex;
        } finally {
            log.exiting(getClass().getName(), "reloadTenant");
        }
    }
}
