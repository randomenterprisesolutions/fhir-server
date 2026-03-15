/*
 * (C) Copyright Random Enterprise Solutions 2026
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package org.linuxforhealth.fhir.server.test;

import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertTrue;

import javax.ws.rs.client.Entity;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.Response;

import org.testng.annotations.Test;

import org.linuxforhealth.fhir.core.FHIRMediaType;
import org.linuxforhealth.fhir.model.resource.Bundle;
import org.linuxforhealth.fhir.model.resource.Patient;
import org.linuxforhealth.fhir.model.test.TestUtil;

/**
 * Integration tests for Patient name search with Unicode characters.
 *
 * Covers:
 *  - Names with Latin diacritics (e.g. "Müller" must match when searched as "Muller")
 *  - Names with CJK characters (e.g. "田中 太郎")
 *
 * The write path normalises string values using SearchHelper.normalizeForSearch()
 * (NFD decomposition + diacritic stripping + toLowerCase) so that the stored
 * index matches what the search query renderer produces for the same input.
 */
public class SearchUnicodeTest extends FHIRServerTestBase {

    private String patientId;

    // -----------------------------------------------------------------------
    // Create
    // -----------------------------------------------------------------------

    @Test(groups = { "server-search" })
    public void testCreateUnicodePatient() throws Exception {
        WebTarget target = getWebTarget();

        Patient patient = TestUtil.readLocalResource("Patient_UnicodeNames.json");
        Entity<Patient> entity = Entity.entity(patient, FHIRMediaType.APPLICATION_FHIR_JSON);

        Response response = target.path("Patient")
                .request()
                .post(entity, Response.class);
        assertResponse(response, Response.Status.CREATED.getStatusCode());

        patientId = getLocationLogicalId(response);
        assertNotNull(patientId);
    }

    // -----------------------------------------------------------------------
    // Token identifier — exact Unicode match
    // -----------------------------------------------------------------------

    @Test(groups = { "server-search" }, dependsOnMethods = { "testCreateUnicodePatient" })
    public void testSearchByIdentifier_exactUnicode() {
        // Token search is exact — the full mixed Unicode value must match as-is
        assertPatientFound("identifier", "MR-Müller-田中-001");
    }

    @Test(groups = { "server-search" }, dependsOnMethods = { "testCreateUnicodePatient" })
    public void testSearchByIdentifier_systemAndValue() {
        assertPatientFound("identifier", "http://example.org/mrn|MR-Müller-田中-001");
    }

    @Test(groups = { "server-search" }, dependsOnMethods = { "testCreateUnicodePatient" })
    public void testSearchByIdentifier_stripped_doesNotMatch() {
        // Unlike string search, token search does NOT strip diacritics — this must NOT match
        WebTarget target = getWebTarget();
        Response response = target.path("Patient")
                .queryParam("identifier", "MR-Muller-田中-001")
                .queryParam("_id", patientId)
                .request(FHIRMediaType.APPLICATION_FHIR_JSON)
                .get();
        assertResponse(response, Response.Status.OK.getStatusCode());
        Bundle bundle = response.readEntity(Bundle.class);
        assertNotNull(bundle);
        assertTrue(bundle.getEntry().isEmpty(),
                "Token search must NOT match 'MR-Muller-田中-001' when stored value is 'MR-Müller-田中-001'");
    }

    // -----------------------------------------------------------------------
    // Latin diacritics — Müller
    // -----------------------------------------------------------------------

    @Test(groups = { "server-search" }, dependsOnMethods = { "testCreateUnicodePatient" })
    public void testSearchByFamilyMueller_withDiacritic() {
        // Searching with the original accented form should work
        assertPatientFound("family", "Müller");
    }

    @Test(groups = { "server-search" }, dependsOnMethods = { "testCreateUnicodePatient" })
    public void testSearchByFamilyMueller_withoutDiacritic() {
        // Searching with the diacritic-stripped form must also match (the core use-case)
        assertPatientFound("family", "Muller");
    }

    @Test(groups = { "server-search" }, dependsOnMethods = { "testCreateUnicodePatient" })
    public void testSearchByFamilyMueller_caseInsensitive() {
        assertPatientFound("family", "muller");
        assertPatientFound("family", "MULLER");
    }

    @Test(groups = { "server-search" }, dependsOnMethods = { "testCreateUnicodePatient" })
    public void testSearchByGivenHans() {
        assertPatientFound("given", "Hans");
    }

    // -----------------------------------------------------------------------
    // CJK — 田中 太郎
    // -----------------------------------------------------------------------

    @Test(groups = { "server-search" }, dependsOnMethods = { "testCreateUnicodePatient" })
    public void testSearchByFamilyKanji() {
        assertPatientFound("family", "田中");
    }

    @Test(groups = { "server-search" }, dependsOnMethods = { "testCreateUnicodePatient" })
    public void testSearchByGivenKanji() {
        assertPatientFound("given", "太郎");
    }

    @Test(groups = { "server-search" }, dependsOnMethods = { "testCreateUnicodePatient" })
    public void testSearchByNameKanji_fullText() {
        // The 'name' parameter searches across the text representation "田中 太郎"
        assertPatientFound("name", "田中");
        assertPatientFound("name", "太郎");
    }

    @Test(groups = { "server-search" }, dependsOnMethods = { "testCreateUnicodePatient" })
    public void testSearchByNameKanji_nonMatching() {
        // An unrelated Japanese family name must NOT return this patient
        WebTarget target = getWebTarget();
        Response response = target.path("Patient")
                .queryParam("family", "鈴木")
                .queryParam("_id", patientId)
                .request(FHIRMediaType.APPLICATION_FHIR_JSON)
                .get();
        assertResponse(response, Response.Status.OK.getStatusCode());
        Bundle bundle = response.readEntity(Bundle.class);
        assertNotNull(bundle);
        assertTrue(bundle.getEntry().isEmpty(),
                "Patient with family '田中' should NOT be returned when searching for '鈴木'");
    }

    // -----------------------------------------------------------------------
    // Helper
    // -----------------------------------------------------------------------

    /**
     * Assert that searching Patient by {@code param=value} returns at least one
     * result that includes the patient created in this test class (matched by _id).
     */
    private void assertPatientFound(String param, String value) {
        WebTarget target = getWebTarget();
        Response response = target.path("Patient")
                .queryParam(param, value)
                .queryParam("_id", patientId)
                .request(FHIRMediaType.APPLICATION_FHIR_JSON)
                .get();
        assertResponse(response, Response.Status.OK.getStatusCode());
        Bundle bundle = response.readEntity(Bundle.class);
        assertNotNull(bundle, "Bundle must not be null for " + param + "=" + value);
        assertTrue(bundle.getEntry().size() >= 1,
                "Expected at least one result for Patient?" + param + "=" + value
                + " (patientId=" + patientId + ")");
    }
}
