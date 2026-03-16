/*
 * (C) Copyright IBM Corp. 2021, 2022
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.randomenterprisesolutions.fhir.cql.engine.server.retrieve;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.ws.rs.core.MultivaluedHashMap;
import javax.ws.rs.core.MultivaluedMap;

import com.randomenterprisesolutions.fhir.cql.engine.retrieve.SearchParameterFHIRRetrieveProvider;
import com.randomenterprisesolutions.fhir.cql.engine.retrieve.SearchParameterMap;
import com.randomenterprisesolutions.fhir.cql.engine.searchparam.IQueryParameter;
import com.randomenterprisesolutions.fhir.cql.engine.searchparam.SearchParameterResolver;
import com.randomenterprisesolutions.fhir.cql.helpers.FHIRBundleCursor;
import com.randomenterprisesolutions.fhir.model.resource.Bundle;
import com.randomenterprisesolutions.fhir.model.resource.Resource;
import com.randomenterprisesolutions.fhir.persistence.SingleResourceResult;
import com.randomenterprisesolutions.fhir.search.SearchConstants;
import com.randomenterprisesolutions.fhir.server.spi.operation.FHIRResourceHelpers;

/**
 * This is an implementation of a retrieve provider for the CQL Engine that uses
 * the IBM FHIR Server FHIRResourceHelpers API to access the data. This passes
 * through directly to the persistence APIs.
 */
public class ServerFHIRRetrieveProvider extends SearchParameterFHIRRetrieveProvider {

    private static final String DUMMY_REQUEST_URI = "https://localhost:9443/fhir-server/api/v4";

    private static Logger logger = Logger.getLogger(ServerFHIRRetrieveProvider.class.getName());

    private FHIRResourceHelpers resourceHelpers;

    public ServerFHIRRetrieveProvider(FHIRResourceHelpers resourceHelpers, SearchParameterResolver searchParameterResolver) {
        super(searchParameterResolver);
        this.resourceHelpers = resourceHelpers;
    }

    @Override
    protected Iterable<Object> executeQueries(String dataType, List<SearchParameterMap> queries) throws Exception {
        List<Object> results = new ArrayList<>();

        for (SearchParameterMap map : queries) {
            if( logger.isLoggable(Level.FINE) ) {
                logger.fine(String.format("Executing query %s?%s", dataType, map.toString()));
            }

            MultivaluedMap<String, String> queryParameters = getQueryParameters(map);

            // _total=none instructs the server to skip the count(*) query which improves performance
            queryParameters.putSingle(SearchConstants.TOTAL, "none");

            Resource resource = executeQuery(dataType, queryParameters);
            if (resource instanceof Bundle) {
                final String requestUri = DUMMY_REQUEST_URI + "/" + dataType;
                final AtomicInteger pageNumber = new AtomicInteger(1);
                FHIRBundleCursor cursor = new FHIRBundleCursor(url -> {
                    try {
                        int nextPage = pageNumber.incrementAndGet();
                        if( logger.isLoggable(Level.FINE) ) {
                            logger.fine(String.format("Retrieving page %d / %s", nextPage, url));
                        }
                        queryParameters.putSingle(SearchConstants.PAGE, String.valueOf(nextPage));
                        return resourceHelpers.doSearch(dataType, /*compartment=*/null, /*compartmentId=*/null, queryParameters, requestUri);
                    } catch (Exception ex) {
                        throw new RuntimeException(ex);
                    }
                }, (Bundle) resource);

                Iterator<?> it = cursor.iterator();
                while(it.hasNext()) {
                    results.add(it.next());
                }
            } else if (resource != null) {
                results.add(resource);
            }
        }

        return results;
    }

    private Resource executeQuery(String dataType, MultivaluedMap<String, String> queryParameters) {
        Resource resource = null;

        try {
            if (queryParameters.containsKey("_id")) {
                String id = queryParameters.getFirst("_id");
                SingleResourceResult<?> result = resourceHelpers.doRead(dataType, id);
                if (result.isSuccess()) {
                    resource = result.getResource();
                } else {
                    // For backwards-compatibility with our old behavior
                    throw new RuntimeException("Resource '" + dataType + "/" + id + "' not found.");
                }
            } else {
                resource = resourceHelpers.doSearch(dataType, /*compartment=*/null, /*compartmentId=*/null, queryParameters, DUMMY_REQUEST_URI);
            }
        } catch (RuntimeException rex) {
            throw rex;
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }

        return resource;
    }

    private MultivaluedMap<String, String> getQueryParameters(SearchParameterMap map) {

        MultivaluedMap<String, String> parameters = new MultivaluedHashMap<>();
        for (Map.Entry<String, List<List<? extends IQueryParameter>>> entry : map.entrySet()) {
            for (List<? extends IQueryParameter> sublist : entry.getValue()) {
                for (IQueryParameter p : sublist) {
                    String name = getModifiedName(entry.getKey(), p);

                    List<String> values = parameters.computeIfAbsent(name, k -> new ArrayList<>());
                    values.add(p.getParameterValue());
                }
            }
        }
        if( map.count() != null ) {
            parameters.putSingle(SearchConstants.COUNT, String.valueOf(map.count()));
        }
        return parameters;
    }

}
