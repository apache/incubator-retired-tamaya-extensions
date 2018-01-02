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

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.net.URL;
import java.net.URLClassLoader;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * Tests if feature are not seen, using an empty class loader. Created by atsticks on 19.03.17.
 */
public class FeaturesTestNoOnly {

    ClassLoader classloader;

    @Before
    public void setup(){
        this.classloader = Thread.currentThread().getContextClassLoader();
        Thread.currentThread().setContextClassLoader(new URLClassLoader(new URL[]{}, null));
    }

    @After
    public void cleanup(){
        Thread.currentThread().setContextClassLoader(this.classloader);
    }

    @Test
    public void eventsAvailable() throws Exception {
        assertFalse(Features.eventsAvailable());
    }

    @Test
    public void formatsAvailable() throws Exception {
        assertFalse(Features.formatsAvailable());
    }

    @Test
    public void tamayaCoreAvailable() throws Exception {
        assertFalse(Features.tamayaCoreAvailable());
    }

    @Test
    public void injectionCDIAvailable() throws Exception {
        assertFalse(Features.injectionCDIAvailable());
    }

    @Test
    public void injectionStandaloneAvailable() throws Exception {
        assertFalse(Features.injectionStandaloneAvailable());
    }

    @Test
    public void mutableConfigAvailable() throws Exception {
        assertFalse(Features.mutableConfigAvailable());
    }

    @Test
    public void optionalAvailable() throws Exception {
        assertFalse(Features.optionalAvailable());
    }

    @Test
    public void resolverAvailable() throws Exception {
        assertFalse(Features.resolverAvailable());
    }

    @Test
    public void resourcesAvailable() throws Exception {
        assertFalse(Features.resourcesAvailable());
    }

    @Test
    public void baseSupportAvailable() throws Exception {
        assertFalse(Features.baseSupportAvailable());
    }

    @Test
    public void filterSupportAvailable() throws Exception {
        assertFalse(Features.filterSupportAvailable());
    }

    @Test
    public void springAvailable() throws Exception {
        assertFalse(Features.springAvailable());
    }

    @Test
    public void jndiAvailable() throws Exception {
        assertFalse(Features.jndiAvailable());
    }

    @Test
    public void extSpringCoreAvailable() throws Exception {
        assertFalse(Features.extSpringCoreAvailable());
    }

    @Test
    public void extOSGIAvailable() throws Exception {
        assertFalse(Features.extOSGIAvailable());
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