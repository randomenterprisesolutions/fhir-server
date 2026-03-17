/*
 * (C) Copyright IBM Corp. 2019, 2022
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package com.randomenterprisesolutions.fhir.operation.bulkdata;

import java.util.List;
import java.util.Set;

import javax.ws.rs.core.MediaType;

import com.randomenterprisesolutions.fhir.core.FHIRMediaType;
import com.randomenterprisesolutions.fhir.core.FHIRVersionParam;
import com.randomenterprisesolutions.fhir.exception.FHIROperationException;
import com.randomenterprisesolutions.fhir.model.resource.OperationDefinition;
import com.randomenterprisesolutions.fhir.model.resource.Parameters;
import com.randomenterprisesolutions.fhir.model.resource.Resource;
import com.randomenterprisesolutions.fhir.model.type.Instant;
import com.randomenterprisesolutions.fhir.model.type.code.IssueType;
import com.randomenterprisesolutions.fhir.operation.bulkdata.OperationConstants.ExportType;
import com.randomenterprisesolutions.fhir.operation.bulkdata.config.preflight.Preflight;
import com.randomenterprisesolutions.fhir.operation.bulkdata.config.preflight.PreflightFactory;
import com.randomenterprisesolutions.fhir.operation.bulkdata.processor.BulkDataFactory;
import com.randomenterprisesolutions.fhir.operation.bulkdata.util.BulkDataExportUtil;
import com.randomenterprisesolutions.fhir.operation.bulkdata.util.CommonUtil;
import com.randomenterprisesolutions.fhir.operation.bulkdata.util.CommonUtil.Type;
import com.randomenterprisesolutions.fhir.registry.FHIRRegistry;
import com.randomenterprisesolutions.fhir.search.util.SearchHelper;
import com.randomenterprisesolutions.fhir.server.spi.operation.AbstractOperation;
import com.randomenterprisesolutions.fhir.server.spi.operation.FHIROperationContext;
import com.randomenterprisesolutions.fhir.server.spi.operation.FHIRResourceHelpers;

/**
 * Creates a System Export of FHIR Data to NDJSON format
 * @see https://hl7.org/Fhir/uv/bulkdata/OperationDefinition-export.html
 */
public class ExportOperation extends AbstractOperation {

    private static final CommonUtil COMMON = new CommonUtil(Type.EXPORT);
    private static final BulkDataExportUtil export = new BulkDataExportUtil();

    public ExportOperation() {
        super();
    }

    @Override
    protected OperationDefinition buildOperationDefinition() {
        return FHIRRegistry.getInstance().getResource("http://hl7.org/fhir/uv/bulkdata/OperationDefinition/export", OperationDefinition.class);
    }

    @Override
    protected Parameters doInvoke(FHIROperationContext operationContext, Class<? extends Resource> resourceType,
            String logicalId, String versionId, Parameters parameters, FHIRResourceHelpers resourceHelper, SearchHelper searchHelper)
            throws FHIROperationException {
        COMMON.checkEnabled();
        COMMON.checkAllowed(operationContext, false);

        MediaType outputFormat = export.checkAndConvertToMediaType(parameters);

        Instant since = export.checkAndExtractSince(parameters);

        List<String> typeFilters = export.checkAndValidateTypeFilters(parameters);

        // If Patient - Export Patient Filter Resources
        Parameters response = null;
        OperationConstants.ExportType exportType = export.checkExportType(operationContext.getType(), resourceType);

        FHIRVersionParam fhirVersion = (FHIRVersionParam) operationContext.getProperty(FHIROperationContext.PROPNAME_FHIR_VERSION);
        Set<String> types = export.checkAndValidateTypes(exportType, fhirVersion, getParameters(parameters, OperationConstants.PARAM_TYPE));

        if (!ExportType.INVALID.equals(exportType)) {

            // Early detection of potential issues.
            Preflight preflight =  PreflightFactory.getInstance(operationContext, null, exportType, outputFormat.toString());
            preflight.preflight();

            // Warning that Parquet is deprecated.
            if (FHIRMediaType.SUBTYPE_FHIR_PARQUET.equals(outputFormat.getSubtype())) {
                throw buildExceptionWithIssue("Export to parquet is no longer supported; try 'application/fhir+ndjson'", IssueType.INVALID);
            }

            response = BulkDataFactory.getInstance(operationContext)
                        .export(logicalId, exportType, outputFormat, since, types, typeFilters, operationContext);
        } else {
            // Unsupported on instance, specific types other than group/patient/system
            throw buildExceptionWithIssue(
                    "Invalid call $export operation call to '" + resourceType.getSimpleName() + "'", IssueType.INVALID);
        }
        return response;
    }
}