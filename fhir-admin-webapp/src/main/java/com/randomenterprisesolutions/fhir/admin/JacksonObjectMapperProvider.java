/*
 * (C) Copyright Random Enterprise Solutions 2026
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package com.randomenterprisesolutions.fhir.admin;

import javax.ws.rs.ext.ContextResolver;
import javax.ws.rs.ext.Provider;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

/**
 * JAX-RS {@link ContextResolver} that supplies a shared, configured
 * {@link ObjectMapper} to Jackson's JAX-RS provider.
 *
 * <p>Configuration applied:
 * <ul>
 *   <li>Java 8 date/time types ({@code Instant}, {@code LocalDate}, …) are
 *       serialised as ISO-8601 strings (not epoch-seconds arrays).</li>
 *   <li>{@link SerializationFeature#FAIL_ON_EMPTY_BEANS} is disabled so that
 *       plain DTO objects without custom serialisers work out-of-the-box.</li>
 * </ul>
 */
@Provider
public class JacksonObjectMapperProvider implements ContextResolver<ObjectMapper> {

    private static final ObjectMapper MAPPER = createMapper();

    private static ObjectMapper createMapper() {
        return new ObjectMapper()
                .registerModule(new JavaTimeModule())
                .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)
                .disable(SerializationFeature.FAIL_ON_EMPTY_BEANS);
    }

    @Override
    public ObjectMapper getContext(Class<?> type) {
        return MAPPER;
    }
}
