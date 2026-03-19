/*
 * (C) Copyright Random Enterprise Solutions 2026
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package com.randomenterprisesolutions.fhir.admin;

import java.util.HashSet;
import java.util.Set;
import java.util.logging.Logger;

import javax.ws.rs.ApplicationPath;
import javax.ws.rs.core.Application;

import com.fasterxml.jackson.jaxrs.json.JacksonJsonProvider;
import com.randomenterprisesolutions.fhir.admin.exception.AdminExceptionMapper;
import com.randomenterprisesolutions.fhir.admin.resource.AdminTenantsResource;

/**
 * JAX-RS {@link Application} for the FHIR Admin REST API.
 *
 * <p>The application is mapped to the root path ({@code /}) in WEB-INF/web.xml;
 * the logical path prefix {@code /admin} is part of each resource's
 * {@code @Path} annotation so it is visible in the OpenAPI spec.
 *
 * <p>The admin WAR is deployed separately from the main FHIR WAR (typically on
 * port 9444). It uses HTTP Basic auth backed by the same Liberty basicRegistry
 * as the main FHIR server — the {@code fhiradmin} user must be a member of the
 * {@code FHIRAdmins} group.
 */
@ApplicationPath("/")
public class AdminApplication extends Application {

    private static final Logger log = Logger.getLogger(AdminApplication.class.getName());

    public AdminApplication() {
        log.info("FHIR Admin API starting");
    }

    @Override
    public Set<Class<?>> getClasses() {
        Set<Class<?>> classes = new HashSet<>();
        // REST resources
        classes.add(AdminTenantsResource.class);
        // Exception mapping
        classes.add(AdminExceptionMapper.class);
        // Jackson JSON provider with custom ObjectMapper (java.time support)
        classes.add(JacksonObjectMapperProvider.class);
        classes.add(JacksonJsonProvider.class);
        return classes;
    }
}
