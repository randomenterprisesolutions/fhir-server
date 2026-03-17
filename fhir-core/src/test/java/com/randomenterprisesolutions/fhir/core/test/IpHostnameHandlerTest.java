/*
 * (C) Copyright IBM Corp. 2021
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package com.randomenterprisesolutions.fhir.core.test;

import static org.testng.Assert.assertNotNull;

import org.testng.annotations.Test;

import com.randomenterprisesolutions.fhir.core.util.handler.HostnameHandler;
import com.randomenterprisesolutions.fhir.core.util.handler.IPHandler;

/**
 * Tests of the Core Handlers
 */
public class IpHostnameHandlerTest {

    @Test
    public void testHostname() {
        HostnameHandler handler = new HostnameHandler();
        assertNotNull(handler.getHostname());
    }

    @Test
    public void testIp() {
        IPHandler handler = new IPHandler();
        assertNotNull(handler.getIpAddress());
    }
}