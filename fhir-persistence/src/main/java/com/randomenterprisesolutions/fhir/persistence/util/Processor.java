/*
 * (C) Copyright IBM Corp. 2016,2019
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package com.randomenterprisesolutions.fhir.persistence.util;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.Year;
import java.time.YearMonth;
import java.time.ZonedDateTime;

import com.randomenterprisesolutions.fhir.model.resource.Location;
import com.randomenterprisesolutions.fhir.model.resource.SearchParameter;
import com.randomenterprisesolutions.fhir.model.type.Address;
import com.randomenterprisesolutions.fhir.model.type.Annotation;
import com.randomenterprisesolutions.fhir.model.type.Attachment;
import com.randomenterprisesolutions.fhir.model.type.BackboneElement;
import com.randomenterprisesolutions.fhir.model.type.Base64Binary;
import com.randomenterprisesolutions.fhir.model.type.Canonical;
import com.randomenterprisesolutions.fhir.model.type.Code;
import com.randomenterprisesolutions.fhir.model.type.CodeableConcept;
import com.randomenterprisesolutions.fhir.model.type.Coding;
import com.randomenterprisesolutions.fhir.model.type.ContactDetail;
import com.randomenterprisesolutions.fhir.model.type.ContactPoint;
import com.randomenterprisesolutions.fhir.model.type.Contributor;
import com.randomenterprisesolutions.fhir.model.type.DataRequirement;
import com.randomenterprisesolutions.fhir.model.type.Date;
import com.randomenterprisesolutions.fhir.model.type.DateTime;
import com.randomenterprisesolutions.fhir.model.type.Decimal;
import com.randomenterprisesolutions.fhir.model.type.Expression;
import com.randomenterprisesolutions.fhir.model.type.HumanName;
import com.randomenterprisesolutions.fhir.model.type.Id;
import com.randomenterprisesolutions.fhir.model.type.Identifier;
import com.randomenterprisesolutions.fhir.model.type.Instant;
import com.randomenterprisesolutions.fhir.model.type.Markdown;
import com.randomenterprisesolutions.fhir.model.type.Meta;
import com.randomenterprisesolutions.fhir.model.type.Money;
import com.randomenterprisesolutions.fhir.model.type.Narrative;
import com.randomenterprisesolutions.fhir.model.type.Oid;
import com.randomenterprisesolutions.fhir.model.type.ParameterDefinition;
import com.randomenterprisesolutions.fhir.model.type.Period;
import com.randomenterprisesolutions.fhir.model.type.PositiveInt;
import com.randomenterprisesolutions.fhir.model.type.Quantity;
import com.randomenterprisesolutions.fhir.model.type.Range;
import com.randomenterprisesolutions.fhir.model.type.Ratio;
import com.randomenterprisesolutions.fhir.model.type.Reference;
import com.randomenterprisesolutions.fhir.model.type.RelatedArtifact;
import com.randomenterprisesolutions.fhir.model.type.SampledData;
import com.randomenterprisesolutions.fhir.model.type.Signature;
import com.randomenterprisesolutions.fhir.model.type.Time;
import com.randomenterprisesolutions.fhir.model.type.Timing;
import com.randomenterprisesolutions.fhir.model.type.TriggerDefinition;
import com.randomenterprisesolutions.fhir.model.type.UnsignedInt;
import com.randomenterprisesolutions.fhir.model.type.Uri;
import com.randomenterprisesolutions.fhir.model.type.Url;
import com.randomenterprisesolutions.fhir.model.type.UsageContext;
import com.randomenterprisesolutions.fhir.model.type.Uuid;
import com.randomenterprisesolutions.fhir.path.FHIRPathAbstractNode;
import com.randomenterprisesolutions.fhir.path.FHIRPathBooleanValue;
import com.randomenterprisesolutions.fhir.path.FHIRPathDateTimeValue;
import com.randomenterprisesolutions.fhir.path.FHIRPathDecimalValue;
import com.randomenterprisesolutions.fhir.path.FHIRPathElementNode;
import com.randomenterprisesolutions.fhir.path.FHIRPathIntegerValue;
import com.randomenterprisesolutions.fhir.path.FHIRPathQuantityValue;
import com.randomenterprisesolutions.fhir.path.FHIRPathResourceNode;
import com.randomenterprisesolutions.fhir.path.FHIRPathStringValue;
import com.randomenterprisesolutions.fhir.path.FHIRPathTimeValue;
import com.randomenterprisesolutions.fhir.persistence.exception.FHIRPersistenceProcessorException;

@Deprecated
public interface Processor<T> {
    T process(SearchParameter parameter, Object value) throws FHIRPersistenceProcessorException;

    T process(SearchParameter parameter, java.lang.String value) throws FHIRPersistenceProcessorException;

    T process(SearchParameter parameter, com.randomenterprisesolutions.fhir.model.type.String value) throws FHIRPersistenceProcessorException;

    T process(SearchParameter parameter, Address value) throws FHIRPersistenceProcessorException;

    T process(SearchParameter parameter, Annotation value) throws FHIRPersistenceProcessorException;

    T process(SearchParameter parameter, Attachment value) throws FHIRPersistenceProcessorException;

    T process(SearchParameter parameter, BackboneElement value) throws FHIRPersistenceProcessorException;

    T process(SearchParameter parameter, Base64Binary value) throws FHIRPersistenceProcessorException;

    T process(SearchParameter parameter, java.lang.Boolean value) throws FHIRPersistenceProcessorException;

    T process(SearchParameter parameter, com.randomenterprisesolutions.fhir.model.type.Boolean value) throws FHIRPersistenceProcessorException;

    T process(SearchParameter parameter, Canonical value) throws FHIRPersistenceProcessorException;

    T process(SearchParameter parameter, Code value) throws FHIRPersistenceProcessorException;

    T process(SearchParameter parameter, CodeableConcept value) throws FHIRPersistenceProcessorException;

    T process(SearchParameter parameter, Coding value) throws FHIRPersistenceProcessorException;

    T process(SearchParameter parameter, ContactDetail value) throws FHIRPersistenceProcessorException;

    T process(SearchParameter parameter, ContactPoint value) throws FHIRPersistenceProcessorException;

    T process(SearchParameter parameter, Contributor value) throws FHIRPersistenceProcessorException;

    T process(SearchParameter parameter, Date value) throws FHIRPersistenceProcessorException;

    T process(SearchParameter parameter, DateTime value) throws FHIRPersistenceProcessorException;

    T process(SearchParameter parameter, DataRequirement value) throws FHIRPersistenceProcessorException;

    T process(SearchParameter parameter, Decimal value) throws FHIRPersistenceProcessorException;

    T process(SearchParameter parameter, Expression value) throws FHIRPersistenceProcessorException;

    T process(SearchParameter parameter, HumanName value) throws FHIRPersistenceProcessorException;

    T process(SearchParameter parameter, Id value) throws FHIRPersistenceProcessorException;

    T process(SearchParameter parameter, Identifier value) throws FHIRPersistenceProcessorException;

    T process(SearchParameter parameter, Instant value) throws FHIRPersistenceProcessorException;

    T process(SearchParameter parameter, java.lang.Integer value) throws FHIRPersistenceProcessorException;

    T process(SearchParameter parameter, com.randomenterprisesolutions.fhir.model.type.Integer value) throws FHIRPersistenceProcessorException;

    T process(SearchParameter parameter, Markdown value) throws FHIRPersistenceProcessorException;

    T process(SearchParameter parameter, Meta value) throws FHIRPersistenceProcessorException;

    T process(SearchParameter parameter, Money value) throws FHIRPersistenceProcessorException;

    T process(SearchParameter parameter, Narrative value) throws FHIRPersistenceProcessorException;

    T process(SearchParameter parameter, Oid value) throws FHIRPersistenceProcessorException;

    T process(SearchParameter parameter, ParameterDefinition value) throws FHIRPersistenceProcessorException;

    T process(SearchParameter parameter, Period value) throws FHIRPersistenceProcessorException;

    T process(SearchParameter parameter, PositiveInt value) throws FHIRPersistenceProcessorException;

    T process(SearchParameter parameter, Quantity value) throws FHIRPersistenceProcessorException;

    T process(SearchParameter parameter, Range value) throws FHIRPersistenceProcessorException;

    T process(SearchParameter parameter, Ratio value) throws FHIRPersistenceProcessorException;

    T process(SearchParameter parameter, Reference value) throws FHIRPersistenceProcessorException;

    T process(SearchParameter parameter, RelatedArtifact value) throws FHIRPersistenceProcessorException;

    T process(SearchParameter parameter, SampledData value) throws FHIRPersistenceProcessorException;

    T process(SearchParameter parameter, Signature value) throws FHIRPersistenceProcessorException;

    T process(SearchParameter parameter, Time value) throws FHIRPersistenceProcessorException;

    T process(SearchParameter parameter, Timing value) throws FHIRPersistenceProcessorException;

    T process(SearchParameter parameter, TriggerDefinition value) throws FHIRPersistenceProcessorException;

    T process(SearchParameter parameter, UnsignedInt value) throws FHIRPersistenceProcessorException;

    T process(SearchParameter parameter, Uri value) throws FHIRPersistenceProcessorException;

    T process(SearchParameter parameter, Url value) throws FHIRPersistenceProcessorException;

    T process(SearchParameter parameter, UsageContext value) throws FHIRPersistenceProcessorException;

    T process(SearchParameter parameter, Uuid value) throws FHIRPersistenceProcessorException;

    T process(SearchParameter parameter, Location.Position value) throws FHIRPersistenceProcessorException;

    T process(SearchParameter parameter, FHIRPathAbstractNode value) throws FHIRPersistenceProcessorException;

    T process(SearchParameter parameter, FHIRPathElementNode value) throws FHIRPersistenceProcessorException;

    T process(SearchParameter parameter, FHIRPathDateTimeValue value) throws FHIRPersistenceProcessorException;

    T process(SearchParameter parameter, FHIRPathStringValue value) throws FHIRPersistenceProcessorException;

    T process(SearchParameter parameter, FHIRPathTimeValue value) throws FHIRPersistenceProcessorException;

    T process(SearchParameter parameter, FHIRPathResourceNode value) throws FHIRPersistenceProcessorException;

    T process(SearchParameter parameter, FHIRPathIntegerValue value) throws FHIRPersistenceProcessorException;

    T process(SearchParameter parameter, FHIRPathDecimalValue value) throws FHIRPersistenceProcessorException;

    T process(SearchParameter parameter, FHIRPathBooleanValue value) throws FHIRPersistenceProcessorException;

    T process(SearchParameter parameter, FHIRPathQuantityValue value) throws FHIRPersistenceProcessorException;

    T process(SearchParameter parameter, ZonedDateTime value) throws FHIRPersistenceProcessorException;

    T process(SearchParameter parameter, LocalDate value) throws FHIRPersistenceProcessorException;

    T process(SearchParameter parameter, YearMonth value) throws FHIRPersistenceProcessorException;

    T process(SearchParameter parameter, Year value) throws FHIRPersistenceProcessorException;

    T process(SearchParameter parameter, BigDecimal value) throws FHIRPersistenceProcessorException;
}
