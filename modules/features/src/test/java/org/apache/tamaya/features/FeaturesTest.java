/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.apache.tamaya.features;

import org.junit.Test;

import static org.junit.Assert.*;

/**
 * Tests if feature are seen. Created by atsticks on 19.03.17.
 */
public class FeaturesTest {
    @Test
    public void eventsAvailable() throws Exception {
        assertTrue(Features.eventsAvailable());
    }

    @Test
    public void formatsAvailable() throws Exception {
        assertTrue(Features.formatsAvailable());
    }

    @Test
    public void tamayaCoreAvailable() throws Exception {
        assertTrue(Features.tamayaCoreAvailable());
    }

    @Test
    public void injectionCDIAvailable() throws Exception {
        assertTrue(Features.injectionCDIAvailable());
    }

    @Test
    public void injectionStandaloneAvailable() throws Exception {
        assertTrue(Features.injectionStandaloneAvailable());
    }

    @Test
    public void mutableConfigAvailable() throws Exception {
        assertTrue(Features.mutableConfigAvailable());
    }

    @Test
    public void optionalAvailable() throws Exception {
        assertTrue(Features.optionalAvailable());
    }

    @Test
    public void resolverAvailable() throws Exception {
        assertTrue(Features.resolverAvailable());
    }

    @Test
    public void resourcesAvailable() throws Exception {
        assertTrue(Features.resourcesAvailable());
    }

    @Test
    public void spiSupportAvailable() throws Exception {
        assertTrue(Features.spiSupportAvailable());
    }

    @Test
    public void filterSupportAvailable() throws Exception {
        assertTrue(Features.filterSupportAvailable());
    }

    @Test
    public void springAvailable() throws Exception {
        assertTrue(Features.springAvailable());
    }

    @Test
    public void jndiAvailable() throws Exception {
        assertTrue(Features.jndiAvailable());
    }

    @Test
    public void extSpringCoreAvailable() throws Exception {
        assertTrue(Features.extSpringCoreAvailable());
    }

    @Test
    public void extOSGIAvailable() throws Exception {
        assertTrue(Features.extOSGIAvailable());
    }

    @Test
    public void extJndiAvailable() throws Exception {
        assertTrue(Features.extJndiAvailable());
    }

    @Test
    public void checkClassIsLoadable() throws Exception {
        assertTrue(Features.checkClassIsLoadable("java.lang.String"));
        assertFalse(Features.checkClassIsLoadable("foo.Bar"));
    }

}